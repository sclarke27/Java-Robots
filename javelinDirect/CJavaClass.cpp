/*
 *  Javelin Stamp Direct Interface
 *
 *  Copyright (c) 1997-2005 Christopher Waters. All rights reserved.
 *  http://www.parallax.com/javelin/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "CJavaClass.h"
#include "CJEMLinker.h"
#include "Utils.h"
#include "Errors.h"
#include "RTTI.h"
#include "OptionVar.h"
#include <string.h>
#include <stdlib.h>

#include "Java.h"
#ifdef WIN32
#include <winsock.h>
#else
#include <netinet/in.h>
#endif

CJavaClass::CJavaClass( char *aClassName, char *aClassPath ) {
  objectClass = kCJavaClass;

  isLoaded = FALSE;
  classFile = NULL;
  constantPool = NULL;
  filePath = NULL;
  sourcePath = NULL;
  constantPoolCount = 0;
  interfaces = NULL;
  interfacesCount = 0;
  fields = NULL;
  fieldsCount = 0;
  methods = NULL;
  methodsCount = 0;
  sOldIndex = -1;
  sOldMethod = -1;
  sCount = 0;
  linkedMethods = NULL;
  linkedFields = NULL;
  numLinkedMethods = 0;
  numLinkedFields = 0;
  linked = false;

  className = strdup( aClassName );
  FailNULL( className );

  javaClassName = strdup( aClassName );
  FailNULL( javaClassName );
  for ( int i = 0; i < strlen(javaClassName); i++ )
  	if ( javaClassName[i] == '/' )
    	javaClassName[i] = '.';

  classPath = strdup( aClassPath );
  FailNULL( classPath );
}

CJavaClass::~CJavaClass() {
  if ( isLoaded )
    FlushClass();

  if ( className )
    free( className );
  if ( javaClassName )
    free( javaClassName );
  if ( classPath )
  	free( classPath );
  if ( filePath )
  	free( filePath );

  if ( sourcePath )
    free( sourcePath );

  // Free fields.
  if ( fields )
    free( fields );

  if ( linkedFields )
    free( linkedFields );
  if ( linkedMethods )
    free( linkedMethods );
}

char *CJavaClass::GetSourcePath() {
  if ( !filePath )
    return "";
  if ( sourcePath )
    return sourcePath;

  sourcePath = strdup(filePath);
  FailNULL(sourcePath);

  /* Nested classes delimit the file name with a $ */
  char *fileEnd = strrchr(sourcePath, '$');
  if ( !fileEnd ) {
    int len = strlen(sourcePath);
    fileEnd = sourcePath + len - 6;
  }
  *fileEnd++ = '.';
  *fileEnd++ = 'j';
  *fileEnd++ = 'a';
  *fileEnd++ = 'v';
  *fileEnd++ = 'a';
  *fileEnd++ = '\0';

  return sourcePath;
}

void CJavaClass::LoadClass() {
  char path[1224], *s, *p, *d;

  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: Loading class file `%s'\n", className );

  // Loop through each directory in the class path.
  s = classPath;
  classFile = NULL;
  while ( (p = strtok(s,";")) != NULL ) {
  	s = NULL;
  	strcpy(path, p);
    if ( path[strlen(path)-1] != '\\' && path[strlen(path)-1] != '/' )
      strcat(path, "/" );
    strcat(path, className );
    strcat(path,".class");

#ifdef WIN32
    // Ensure that only dos type directory characters are used.
    while ( (d = strchr( path, '/' )) != NULL )
    	*d = '\\';
#endif

  if ( gOptions.debugClassLoader )
  		StatusDebug("ClassLoader: Trying file `%s'\n", path );

  	// Try to open the file.
  	classFile = fopen( path, "rb" );
  	if ( classFile )
    	break;

  }
  if ( !classFile )
  	throw EError( kJVM_ERROR, 17, className, path );

  filePath = strdup(path);
  FailNULL( filePath );

  try {

    LoadHeader();
    LoadConstantPool();
    LoadClassInfo();
    LoadInterfaces();
    LoadFields();
    LoadMethods();
    LoadAttributes();
  }
  catch (...) {
    fclose( classFile );
    FlushClass();

    throw;
  }
  fclose( classFile );

  isLoaded = TRUE;

}

void CJavaClass::FlushClass() {
  isLoaded = FALSE;
  if ( constantPool )
  	FlushConstantPool();
  if ( interfaces )
    free( interfaces );
  interfaces = NULL;
  interfacesCount = 0;
  if ( fields )
    free( fields );
  fields = NULL;
  fieldsCount = 0;
  for ( int i = 0; i < methodsCount; i++ ) {
    if ( methods[i].code_attribute.code )
      free( methods[i].code_attribute.code );
    if ( methods[i].code_attribute.exception_table )
      free(  methods[i].code_attribute.exception_table );
    if ( methods[i].exceptions_attribute.exception_index_table )
      free( methods[i].exceptions_attribute.exception_index_table );
  }
  if ( methods )
    free( methods );
  methods = NULL;
  methodsCount = 0;
}

bool CJavaClass::CheckVersion( u2 majorVersion, u2 minorVersion ) {
  if ( majorVersion == CONSTANT_Class_Major 
       && minorVersion <= CONSTANT_Class_Minor )
    return TRUE;	
  else
    return FALSE;
}

void CJavaClass::StaticLink(CJEMLinker *linker) {
  // Don't do it again if already done for this class.
  if ( linked )
    return;

  CJavaClass *super = NULL;

  if ( super_class != 0 ) {
    super = linker->GetClass(GetSuperClassName());
    // Ensure that the superclass is linked first.
    super->StaticLink(linker);
  }

  // Compute the number of non-final, non-static fields in this class.
  if ( super )
    numLinkedFields = NumNonStaticFields() + super->numLinkedFields;
  else
    numLinkedFields = NumNonStaticFields();

  if ( numLinkedFields > 0 ) {
    linkedFields = (linked_field_info*) malloc( sizeof(linked_field_info) * numLinkedFields );
    FailNULL( linkedFields );
  }
  else
    linkedFields = NULL;

  int fieldCount = 0;
  u2 nextOffset = 0;
  if ( super ) {
    for ( int i = 0; i < super->numLinkedFields; i++ ) {
      if ( fieldCount >= numLinkedFields )
        throw EError(kIDE_ERROR, 48);
      linkedFields[fieldCount].className = super->linkedFields[i].className;
      linkedFields[fieldCount].fieldName = super->linkedFields[i].fieldName;
      linkedFields[fieldCount].fieldType = super->linkedFields[i].fieldType;
      linkedFields[fieldCount].fieldIndex = super->linkedFields[i].fieldIndex;
      linkedFields[fieldCount].fieldOffset = super->linkedFields[i].fieldOffset;
      linkedFields[fieldCount].fieldWidth = super->linkedFields[i].fieldWidth;
      nextOffset = linkedFields[fieldCount].fieldOffset + super->linkedFields[i].fieldWidth;
      fieldCount++;
    }
  }
  for ( int i = 0; i < fieldsCount; i++ ) {
    if ( !FieldIsStatic(i) && !FieldIsFinal(i) ) {
      if ( fieldCount >= numLinkedFields )
        throw EError(kIDE_ERROR, 48);
      linkedFields[fieldCount].className = GetClassName();
      linkedFields[fieldCount].fieldName = GetFieldName(i);
      linkedFields[fieldCount].fieldType = GetFieldType(i);
      linkedFields[fieldCount].fieldIndex = GetFieldIndex(i);
      linkedFields[fieldCount].fieldWidth = GetFieldWidth(i);
      linkedFields[fieldCount].fieldOffset = nextOffset;
      nextOffset = nextOffset + GetFieldWidth(i);
      fieldCount++;
    }
  }

  // Compute the number of virtual methods in this class.
  if ( super )
    numLinkedMethods = methodsCount + super->GetNumLinkedMethods();
  else
    numLinkedMethods = methodsCount;

  if ( numLinkedMethods > 0 ) {
    linkedMethods = (linked_method_info*) malloc( sizeof(linked_method_info) * numLinkedMethods );
    FailNULL( linkedMethods );
  }
  else
    linkedMethods = NULL;
    
  int methodCount = 0;
  if ( super ) {
    for ( int i = 0; i < super->numLinkedMethods; i++ ) {
      if ( strcmp(super->linkedMethods[i].methodName, "<clinit>") != 0
           && strcmp(super->linkedMethods[i].methodName, "<init>") != 0
           && super->linkedMethods[i].isVirtual ) {
        if ( methodCount >= numLinkedMethods )
          throw EError(kIDE_ERROR, 48);
        linkedMethods[methodCount].className = super->linkedMethods[i].className;
        linkedMethods[methodCount].methodName = super->linkedMethods[i].methodName;
        linkedMethods[methodCount].methodType = super->linkedMethods[i].methodType;
        linkedMethods[methodCount].isVirtual = super->linkedMethods[i].isVirtual;
        linkedMethods[methodCount].methodClass = super->linkedMethods[i].methodClass;
        linkedMethods[methodCount].methodIndex = super->linkedMethods[i].methodIndex;
        methodCount++;
      }
    }
  }

  // Add the virtual methods in this class.
  for ( int i = 0; i < methodsCount; i++ ) {
    bool methodFound = false;

    // Only do virtual methods on the first pass.
    if ( !strcmp(GetMethodName(i), "<clinit>")
           || !strcmp(GetMethodName(i), "<init>") )
      continue;
      
    // Check if the method already exists in the table.
    for ( int m = 0; m < methodCount; m++ ) {
      if ( !strcmp( linkedMethods[m].methodName, GetMethodName(i) )
            && !strcmp( linkedMethods[m].methodType, GetMethodType(i) ) ) {
        // The method already exists. Make it point to this class.
        linkedMethods[m].className = GetClassName();
        linkedMethods[m].methodClass = this;
        linkedMethods[m].methodIndex = GetMethodByName( GetMethodName(i), GetMethodType(i));
        methodFound = true;
        break;
      }
    }
    if ( !methodFound ) {
      if ( methodCount >= numLinkedMethods )
        throw EError(kIDE_ERROR, 48);
      linkedMethods[methodCount].className = GetClassName();
      linkedMethods[methodCount].methodName = GetMethodName(i);
      linkedMethods[methodCount].methodType = GetMethodType(i);
      linkedMethods[methodCount].isVirtual = true;
      linkedMethods[methodCount].methodClass = this;
      linkedMethods[methodCount].methodIndex = i;
      methodCount++;
    }
  }

  for ( int i = 0; i < methodsCount; i++ ) {

    // Only do virtual methods on the first pass.
    if ( !strcmp(GetMethodName(i), "<clinit>")
           || !strcmp(GetMethodName(i), "<init>") ) {
      if ( methodCount >= numLinkedMethods )
        throw EError(kIDE_ERROR, 48);
      linkedMethods[methodCount].className = GetClassName();
      linkedMethods[methodCount].methodName = GetMethodName(i);
      linkedMethods[methodCount].methodType = GetMethodType(i);
      linkedMethods[methodCount].isVirtual = true;
      linkedMethods[methodCount].methodClass = this;
      linkedMethods[methodCount].methodIndex = i;
      methodCount++;
    }
  }

  numLinkedMethods = methodCount;

  linked = true;
}

u2 CJavaClass::GetLinkedFieldIndex( char *className, char *fieldName, char *fieldType ) {
  // Search the list of fields.
  for ( int i = numLinkedFields-1; i >=0 ; i-- )
    if ( !strcmp( linkedFields[i].fieldName, fieldName )
          && !strcmp( linkedFields[i].fieldType, fieldType ) )
          // && !strcmp( linkedFields[i].className, className )
      return i;

  throw EError( kJVM_ERROR, 28, className, fieldName);
}

u2 CJavaClass::GetLinkedFieldOffset( char *className, char *fieldName, char *fieldType ) {
  // Search the list of fields.
  for ( int i = numLinkedFields-1; i >=0 ; i-- )
    if ( !strcmp( linkedFields[i].fieldName, fieldName )
          && !strcmp( linkedFields[i].fieldType, fieldType ) )
          // && !strcmp( linkedFields[i].className, className )
      return linkedFields[i].fieldOffset;

  throw EError( kJVM_ERROR, 28, className, fieldName);
}

/* TODO: Optimise GetLinkedMethodOffsetByName() */
u2 CJavaClass::GetLinkedMethodOffsetByName( char *methodName, char *methodType ) {
  for ( int i = numLinkedMethods-1; i >= 0; --i ) {
    if ( !strcmp( linkedMethods[i].methodName, methodName )
          && !strcmp( linkedMethods[i].methodType, methodType ) )
      return linkedMethods[i].methodOffset;
  }

  throw EError( kJVM_ERROR, 31, GetClassName(), methodName);
}

u2 CJavaClass::GetLinkedClassMethodOffsetByName( char *methodName, char *methodType ) {
  for ( int i = numLinkedMethods-1; i >= 0; --i ) {
    if ( !strcmp( linkedMethods[i].methodName, methodName )
          && !strcmp( linkedMethods[i].methodType, methodType ) )
      return linkedMethods[i].classMethodOffset;
  }

  throw EError( kJVM_ERROR, 31, GetClassName(), methodName);
}

u2 CJavaClass::GetLinkedMethodIndexByName( char *methodName, char *methodType ) {
  for ( int i = numLinkedMethods-1; i >= 0; --i ) {
    if ( !strcmp( linkedMethods[i].methodName, methodName )
          && !strcmp( linkedMethods[i].methodType, methodType ) )
      return i;
  }

  throw EError( kJVM_ERROR, 31, GetClassName(), methodName);
}

short CJavaClass::NumVirtualMethods() {
  int count = 0;

  for ( int i = 0; i < methodsCount; i++ ) {
    if ( !MethodIsStatic(i) )
      count++;
  }

  return count;
}

short CJavaClass::NumNonVirtualMethods() {
  int count = 0;

  for ( int i = 0; i < methodsCount; i++ ) {
    if ( MethodIsStatic(i) )
      count++;
  }

  return count;
}


void CJavaClass::DisassembleMethod( u2 method_index ) {
  if ( method_index > methodsCount )
    throw EError( kJVM_ERROR, 8 );

  StatusOutput("Method %s /* %s */\n", ConstantUtf8(methods[method_index].name_index ),
	       ConstantUtf8(methods[method_index].descriptor_index ) );

  for ( u4 i = 0; i < METHOD_LENGTH(method_index); i++ ) {
    i += PrintOpcode( method_index, i );
    StatusOutput("\n");
  }
}

u2 CJavaClass::PrintOpcode( u2 method, u4 index ) {
  u2 poolIndex; // Index into the constant pool.
  u1 *opcode = & OPCODE(method,index);
  char poolVal[1024];

  StatusOutput("Don't use this method\n");
  return -1;
}

void CJavaClass::LoadHeader() {
  u4 magicNumber;
  u2 minor, major;

  // Check the magic number.
  magicNumber = ReadU4();
  if ( magicNumber != CONSTANT_Class_Magic )
    throw EError( kJVM_ERROR, 18, className );

  minor = ReadU2();
  major = ReadU2();
  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: class file version %d.%d\n", major, minor );
  if ( !CheckVersion( major, minor ) )
    throw EError( kJVM_ERROR, 19 );
}

void CJavaClass::LoadConstantPool() {
  constantPoolCount = ReadU2();
  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: %d entries in constant pool\n", constantPoolCount );
  constantPool = (tPoolEntry*) calloc( constantPoolCount, sizeof(tPoolEntry) );
  FailNULL( constantPool );
  
  // Load each entry. Entry 0 is not present in the file.
  for ( int i = 1; i < constantPoolCount; i++ ) {
    u1 tag = ReadU1();

    constantPool[i].tag = tag;
    switch ( tag ) {
    case CONSTANT_Class:
      constantPool[i].info.Class_info.name_index = ReadU2();
      break;

    case CONSTANT_Fieldref:
      constantPool[i].info.Fieldref_info.class_index = ReadU2();
      constantPool[i].info.Fieldref_info.name_and_type_index = ReadU2();
      break;

    case CONSTANT_Methodref:
      constantPool[i].info.Methodref_info.class_index = ReadU2();
      constantPool[i].info.Methodref_info.name_and_type_index = ReadU2();
      break;

    case CONSTANT_InterfaceMethodref:
      constantPool[i].info.InterfaceMethodref_info.class_index = ReadU2();
      constantPool[i].info.InterfaceMethodref_info.name_and_type_index = ReadU2();
      break;

    case CONSTANT_String:
      constantPool[i].info.String_info.string_index = ReadU2();
      break;

    case CONSTANT_Integer:
      constantPool[i].info.Integer.integer_value = ReadU4();
      break;

    case CONSTANT_Float:
      constantPool[i].info.Float.float_value = ReadU4();
      break;

    case CONSTANT_Long:
      constantPool[i].info.Long_info.high_bytes = ReadU4();
      constantPool[i].info.Long_info.low_bytes = ReadU4();
      i++;
      break;

    case CONSTANT_Double:
      constantPool[i].info.Double_info.high_bytes = ReadU4();
      constantPool[i].info.Double_info.low_bytes = ReadU4();
      i++;
      break;
      
    case CONSTANT_NameAndType:
      constantPool[i].info.NameAndType_info.name_index = ReadU2();
      constantPool[i].info.NameAndType_info.descriptor_index = ReadU2();
      break;

    case CONSTANT_Utf8:
      constantPool[i].info.Utf_info.length = ReadU2();
      constantPool[i].info.Utf_info.bytes = (char*) malloc( constantPool[i].info.Utf_info.length + 1 );
      FailNULL( constantPool[i].info.Utf_info.bytes );
      ReadBytes( constantPool[i].info.Utf_info.bytes, constantPool[i].info.Utf_info.length );
      constantPool[i].info.Utf_info.bytes[constantPool[i].info.Utf_info.length] = 0;
      break;

     default:
      throw EError( kJVM_ERROR, 21, tag, className );
    }
  }	
}

void CJavaClass::LoadClassInfo() {
  access_flags = ReadU2();
  this_class = ReadU2();
  super_class = ReadU2();
}
 
void CJavaClass::LoadInterfaces() {
  interfacesCount = ReadU2();
  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: %d interfaces\n", interfacesCount );
  if ( interfacesCount > 0 ) {
    // Interfaces are not supported so just abort.
    throw EError(kJVM_ERROR, 32 );

    interfaces = (u2*) malloc( sizeof( u2 ) * interfacesCount );
    FailNULL( interfaces );
    for ( int i = 0; i < interfacesCount; i++ )
      interfaces[i] = ReadU2();
  }
  else
    interfaces = NULL;
}
 
void CJavaClass::LoadFields() {
  int numStatic = 0, numNonStatic = 0;

  fieldsCount = ReadU2();
  if ( fieldsCount == 0 ) {
    fields = NULL;
    return;
  }
  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: %d fields\n", fieldsCount );
  fields = (field_info*) calloc( fieldsCount, sizeof(field_info) );
  FailNULL( fields );

  for ( int i = 0; i < fieldsCount; i++ ) {
    fields[i].access_flags = ReadU2();
    fields[i].name_index = ReadU2();
    fields[i].descriptor_index = ReadU2();
    fields[i].attributes_count = ReadU2();
    fields[i].constantvalue_index = 0;
    if ( !FieldIsFinal(i) ) {
      if ( fields[i].access_flags & ACC_STATIC ) {
        fields[i].fieldIndex = numStatic;
        numStatic++;
      }
      else {
        fields[i].fieldIndex = numNonStatic;
        numNonStatic++;
      }
    }

    if ( gOptions.debugClassLoader )
  		StatusDebug("ClassLoader: field: `%s' of type %s\n",
		ConstantUtf8( fields[i].name_index ),
		ConstantUtf8( fields[i].descriptor_index ) );   
    for ( int a = 0; a < fields[i].attributes_count; a++ ) {
      // Read the attributes. The only one we recognise is ConstantValue.
      u2 name_index = ReadU2(); // Get the name of the attribute;
      u4 length = ReadU4();
      if ( !strcmp( ConstantUtf8( name_index ), "ConstantValue" ) ) {
	fields[i].constantvalue_index = ReadU2();
      }
      else {
	      if ( gOptions.debugClassLoader )
  				StatusDebug("ClassLoader: skipping unknown attribute `%s'\n", ConstantUtf8( name_index ) );
	// Skip over the unknown attribute.
	for ( int j = 0; j < length; j++ ) 
	  char dummy = ReadU1();
      }
    }
  }
}

  int CJavaClass::LNTCompare(const void *a, const void *b) {
  	return ((Line_number_table*)a)->line_number - ((Line_number_table*)b)->line_number;
  }

void CJavaClass::LoadMethods() {
  int j;

  methodsCount = ReadU2();
  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: %d methods\n", methodsCount );
  if ( methodsCount == 0 )
    return;
    
  methods = (method_info*) calloc( methodsCount, sizeof(method_info) );
  FailNULL( methods );
  
  for ( int i = 0; i < methodsCount; i++ ) {
    methods[i].access_flags = ReadU2();		
    methods[i].name_index = ReadU2();		
    methods[i].descriptor_index = ReadU2();		
    u2 attributes_count = ReadU2();
  if ( gOptions.debugClassLoader )
  		StatusDebug("ClassLoader: method: `%s' of type %s\n",
		ConstantUtf8( methods[i].name_index ),
		ConstantUtf8( methods[i].descriptor_index ) );
    for ( int a = 0; a < attributes_count; a++ ) {
      // Read the attributes. 
      u2 name_index = ReadU2(); // Get the name of the attribute;
      u4 length = ReadU4();
      if ( !strcmp( ConstantUtf8( name_index ), "Code" ) ) {
				methods[i].code_attribute.max_stack = ReadU2();
				methods[i].code_attribute.max_locals = ReadU2();
				methods[i].code_attribute.code_length = ReadU4();
        methods[i].code_attribute.local_variable_table_length = 0;
        methods[i].code_attribute.line_number_table_length = 0;
        methods[i].code_attribute.exception_table_length = 0;
				methods[i].code_attribute.code = (u1*) malloc( sizeof(u1) * methods[i].code_attribute.code_length );
				FailNULL( methods[i].code_attribute.code );
				ReadBytes( (char*)methods[i].code_attribute.code, methods[i].code_attribute.code_length );
				methods[i].code_attribute.exception_table_length = ReadU2();
        if ( methods[i].code_attribute.exception_table_length > 0 ) {
				  methods[i].code_attribute.exception_table = (Exception_table*) malloc( sizeof( Exception_table ) * methods[i].code_attribute.exception_table_length );
				  FailNULL( methods[i].code_attribute.exception_table );
				  for ( int e = 0; e < methods[i].code_attribute.exception_table_length; e++ ) {
        	  methods[i].code_attribute.exception_table[e].start_pc = ReadU2();
	  			  methods[i].code_attribute.exception_table[e].end_pc = ReadU2();
	  			  methods[i].code_attribute.exception_table[e].handler_pc = ReadU2();
	  			  methods[i].code_attribute.exception_table[e].catch_type = ReadU2();
				  }
        }
        else
          methods[i].code_attribute.exception_table = NULL;

				u2 code_attrib_count = ReadU2();
				for ( int aa = 0; aa < code_attrib_count; aa++ ) {
	  			u2 n = ReadU2();
	  			u4 l = ReadU4();
          if ( !strcmp( ConstantUtf8( n ), "LocalVariableTable" ) ) {
        		methods[i].code_attribute.local_variable_table_length = ReadU2();
            if ( methods[i].code_attribute.local_variable_table_length > 0 ) {
        		  methods[i].code_attribute.local_variable_table = (Local_variable_table*)malloc(sizeof(Local_variable_table) * methods[i].code_attribute.local_variable_table_length );
        		  FailNULL( methods[i].code_attribute.local_variable_table );
        		  for ( int l = 0; l < methods[i].code_attribute.local_variable_table_length; l++ ) {
          		  methods[i].code_attribute.local_variable_table[l].start_pc = ReadU2();
          		  methods[i].code_attribute.local_variable_table[l].length = ReadU2();
          		  methods[i].code_attribute.local_variable_table[l].name_index = ReadU2();
          		  methods[i].code_attribute.local_variable_table[l].descriptor_index = ReadU2();
          		  methods[i].code_attribute.local_variable_table[l].index = ReadU2();
        		  }
            }
            else
              methods[i].code_attribute.local_variable_table = NULL;
  				}
          else if ( !strcmp( ConstantUtf8( n ), "LineNumberTable" ) ) {
        		methods[i].code_attribute.line_number_table_length = ReadU2();
            if ( methods[i].code_attribute.line_number_table_length > 0 ) {
        		  methods[i].code_attribute.line_number_table = (Line_number_table*)malloc(sizeof(Line_number_table) * methods[i].code_attribute.line_number_table_length );
        		  FailNULL( methods[i].code_attribute.line_number_table );
        		  for ( int l = 0; l < methods[i].code_attribute.line_number_table_length; l++ ) {
          		  methods[i].code_attribute.line_number_table[l].start_pc = ReadU2();
          		  methods[i].code_attribute.line_number_table[l].line_number = ReadU2();
        		  }
              // Sort the line number table for faster access later.
              qsort(methods[i].code_attribute.line_number_table,
             			methods[i].code_attribute.line_number_table_length,
                  2 * sizeof(u2), LNTCompare);
  				  }
            else
              methods[i].code_attribute.line_number_table = NULL;
          }
          else {
            if ( gOptions.debugClassLoader )
  						StatusDebug("ClassLoader: skipping unknown Code attribute `%s' of length %d\n", ConstantUtf8(n), l );
	  				for ( j = 0; j < l; j++ )
	    				char dummy = ReadU1();
          }
				}
      }
      else if ( !strcmp( ConstantUtf8( name_index ), "Exceptions" ) ) {
				methods[i].exceptions_attribute.number_of_exceptions = ReadU2();
        if ( methods[i].exceptions_attribute.number_of_exceptions > 0 ) {
				  methods[i].exceptions_attribute.exception_index_table = (u2*) malloc( sizeof( u2 ) * methods[i].exceptions_attribute.number_of_exceptions );
				  FailNULL( methods[i].exceptions_attribute.exception_index_table );
				  for ( int e = 0; e < methods[i].exceptions_attribute.number_of_exceptions; e++ )
	  			  methods[i].exceptions_attribute.exception_index_table[e] = ReadU2();
        }
        else
          methods[i].exceptions_attribute.exception_index_table = NULL;
      }
      else {
        if ( gOptions.debugClassLoader )
  				StatusDebug("ClassLoader: skipping unknown attribute `%s'\n", ConstantUtf8( name_index ) );
				// Skip over the unknown attribute.
				for ( j = 0; j < length; j++ )
	  			char dummy = ReadU1();
      }
    }
    
  }
}

void CJavaClass::LoadAttributes() {
  if ( gOptions.debugClassLoader )
  	StatusDebug("ClassLoader: ignoring any class attributes\n");
  // We don't need any attributes so just ignore the rest.
}

void CJavaClass::PrintConstantPoolEntry( u2 index, char *s, int length ) {
  switch ( constantPool[index].tag ) {
  case CONSTANT_Class:
    sprintf(s, "Class %s", ConstantUtf8(ClassName(index)) );
    break;

  case CONSTANT_Fieldref:
    sprintf(s, "Field %s.%s", ConstantUtf8(ClassName(FieldClass( index ))),
		 ConstantUtf8(FieldName( index )) );
    break;

  case CONSTANT_Methodref:
    sprintf(s, "Method %s.%s", ConstantUtf8(ClassName(MethodClass( index ))),
		 ConstantUtf8(MethodName( index )) );
    break;

  case CONSTANT_InterfaceMethodref:
    sprintf(s, "Interface %s.%s", ConstantUtf8(ClassName(InterfaceMethodClass( index ))),
		 ConstantUtf8(InterfaceMethodName( index )) );
    break;

  case CONSTANT_String:
    index = StringUtf8( index );
  case CONSTANT_Utf8:
    sprintf(s, "\"%s\"", ConstantUtf8( index ) );
    break;

  case CONSTANT_Integer:
    sprintf(s, "Integer %d", constantPool[index].info.Integer.integer_value );
    break;

  case CONSTANT_Float:
    sprintf(s, "Float %f", constantPool[index].info.Float.float_value );
    break;

  case CONSTANT_Long:
    sprintf(s, "Long %d %d", constantPool[index].info.Long_info.high_bytes,
		 constantPool[index].info.Long_info.low_bytes );
    break;

  case CONSTANT_Double:
    sprintf(s, "Double %d %d", constantPool[index].info.Double_info.high_bytes,
		 constantPool[index].info.Double_info.low_bytes );
    break;

  case CONSTANT_NameAndType:
    sprintf(s, "NameAndType %s %s", ConstantUtf8( constantPool[index].info.NameAndType_info.name_index ),
		 ConstantUtf8( constantPool[index].info.NameAndType_info.descriptor_index ) );
    break;

  default:
    throw( kJVM_ERROR, 21, constantPool[index].tag, className );
  }
}

/* TODO: Optimise GetMethodByName(). */
u2 CJavaClass::GetMethodByName( char *name, char *type ) {
  for ( int m = 0; m < NumMethods(); m++ ) {
    if ( !strcmp( ConstantUtf8(methods[m].name_index), name )
          && !strcmp( ConstantUtf8(methods[m].descriptor_index), type) )
      return m;
  }

  throw EError( kJVM_ERROR, 14, name );
}

u2 CJavaClass::GetFieldByName( char *name ) {
  for ( int f = 0; f < NumFields(); f++ ) {
    if ( !strcmp( ConstantUtf8(fields[f].name_index), name ) )
      return f;
  }

  throw EError( kJVM_ERROR, 14, name );
}

void CJavaClass::FlushConstantPool() {
  for ( int i = 1; i < constantPoolCount; i++ ) {
    if ( constantPool[i].tag == CONSTANT_Utf8 )
      free( constantPool[i].info.Utf_info.bytes );
  }	

  free( constantPool );
  constantPool = NULL;
}

void CJavaClass::ReadBytes( char *bytes, u4 length ) {
  if ( fread( bytes, sizeof( u1 ), length, classFile ) != length )
    throw EError( kJVM_ERROR, 20, className  );
}

u1 CJavaClass::ReadU1() {
  u1 data;
  
  if ( fread( &data, sizeof( u1 ), 1, classFile ) != 1 )
    throw EError( kJVM_ERROR, 20, className  );

  return data;
}

u2 CJavaClass::ReadU2() {
  u2 data;
  
  if ( fread( &data, sizeof( u2 ), 1, classFile ) != 1 )
    throw EError( kJVM_ERROR, 20, className  );

  return ntohs( data );
}
 
u4 CJavaClass::ReadU4() {
  u4 data;
  
  if ( fread( &data, sizeof( u4 ), 1, classFile ) != 1 )
    throw EError( kJVM_ERROR, 20, className );

  return ntohl(data);
}

/*
static char *GetPathPart( char *filePath ) {
  char *pathEnd;
  char *path;

  pathEnd = strrchr( filePath, '/' );
  if ( !pathEnd ) 
    pathEnd = filePath;
  path = (char*) malloc( sizeof(char) * ((pathEnd-filePath)+1) );
  FailNULL(path);
  strncpy( path, filePath, (pathEnd-filePath) );
              1
  return path;
}
*/

int CJavaClass::GetParamsSize( u2 method_index ) {
  char *params, *s;
  int numWords = 0;

  params = GetMethodType( method_index ); // Get the type descriptor.
  s = params;

  while ( *(++s)) {
    switch ( *s ) {
    case ')':
      // If the method is non-static add a word for the object reference
      if ( MethodIsStatic( method_index ) )
        return numWords;
      else
        return numWords+1;

    case 'B':
    case 'C':
    case 'F':
    case 'I':
    case 'S':
    case 'Z':
      numWords++;
      break;

    case 'L':
      while ( *(++s) != ';' ) // Skip the class name.
	;
      numWords++;
      break;

    case '[':
      // Do nothing. The array type will increment the count.
      break;

    case 'D':
    case 'J':
      numWords += 2;
      break;

    default:
      throw EError( kJVM_ERROR, 22, *s, MethodName(method_index)  );
    }
  }

  return numWords;
}


#define BYTE_VAL ((short)(((u2)(*(opcode+1))<<8)|(*(opcode+2))))

bool CJavaClass::DisassembleByte( u2 method_index, u4 index, char *s, int length ) {
  int i;
  
  // See if we are switching methods.
  if ( sOldMethod != method_index
       || sOldIndex != index - 1 ) {
    sOldMethod = method_index;
    sCount = 0;
  }
  sOldIndex = index;

  // Check that the index is valid.
  if ( index >= GetCodeLength(method_index) )
    throw EError( kJVM_ERROR, 23 );

  // Check if this is a continuing instruction.
  if ( sCount > 0 ) {
    sprintf( s, "" );
    --sCount;
    return false;
  }

  // Disassemble the instruction.

  u2 poolIndex; // Index into the constant pool.
  u1 *opcode = & OPCODE(method_index,index);
  int defaultVal, loVal, hiVal, start, npairs, offset;
  char poolVal[1024];

  // Print operands, if any.
  switch ( *opcode ) {

  case LOOKUPSWITCH:
    start = (((index+4)>>2)<<2);
    defaultVal = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
    start += 4;
    npairs = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
    start += 4;
    sprintf( s, "%4d %s %d pairs default=%d", index, gJavaOps[*opcode], npairs, defaultVal+index );
    for ( i = 0; i < npairs; i++ ) {
      char t[1024];
      int val = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
      start += 4;
      int offset = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
      start += 4;
      sprintf( t, "\n      %d: %d", val, offset+index );
      if ( strlen(s) + strlen(t) >= length )
        break;
      strcat(s,t);
    }
    sCount = (start-index)-1;
    break;

  case TABLESWITCH:
    start = (((index+4)>>2)<<2);
    defaultVal = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
    start += 4;
    loVal = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
    start += 4;
    hiVal = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
    start += 4;
    sprintf( s, "%4d %s %d to %d: default=%d", index, gJavaOps[*opcode], loVal, hiVal, defaultVal+index );
    for ( i = loVal; i <= hiVal; i++ ) {
      char t[1024];
      int val = (OPCODE(method_index, start)<<24)|(OPCODE(method_index, start+1)<<16)|(OPCODE(method_index, start+2)<<8)|(OPCODE(method_index, start+3));
      start += 4;
      sprintf( t, "\n      %d: %d", i, val+index );
      if ( strlen(s) + strlen(t) >= length )
        break;
      strcat(s,t);
    }
    sCount = (start-index)-1;
    break;

    // Constant pool reference (2 byte index).
  case INVOKESTATIC:
  case INVOKESPECIAL:
  case INVOKEVIRTUAL:
  case GETSTATIC:
  case GETFIELD:
  case PUTFIELD:
  case PUTSTATIC:
  case LDC_W:
  case LDC2_W:
  case ANEWARRAY:
  case INSTANCEOF:
  case NEW:
    poolIndex = BYTE_VAL;
    PrintConstantPoolEntry( poolIndex, poolVal, 1020 );
    sprintf( s, "%4d %s #%d <%s>", index, gJavaOps[*opcode], poolIndex, poolVal );
    sCount = 2;
    break;

    // The JVM does nothing for a checkcast instruction. Don't try
    // and load the class because it may be an array which we can't
    // handle.
  case CHECKCAST:
    sprintf( s, "%4d %s ????", index, gJavaOps[*opcode] );
    sCount = 2;
    break;

    // Constant pool reference (1 byte index).
  case LDC:
    poolIndex =  *(opcode+1);
    PrintConstantPoolEntry( poolIndex, poolVal, 1020 );
    sprintf( s, "%4d %s #%d <%s>", index, gJavaOps[*opcode], poolIndex, poolVal );
    sCount = 1;
    break;

    // Local index and increment.
  case IINC:
    sprintf( s, "%4d %s %d %d", index, gJavaOps[*opcode], (*(opcode+1)), (signed char)(*(opcode+2))  );
    sCount = 2;
    break;

    // Branch offet.
  case IFNONNULL:
  case IFNULL:
  case IF_ACMPEQ:
  case IF_ACMPNE:
  case IF_ICMPEQ:
  case IF_ICMPNE:
  case IF_ICMPLE:
  case IF_ICMPGE:
  case IF_ICMPLT:
  case IF_ICMPGT:	
  case IFEQ:
  case IFNE:	
  case IFLT:	
  case IFGT:	
  case IFGE:	
  case IFLE:	
  case GOTO:
  case JSR:
    sprintf( s, "%4d %s %d", index, gJavaOps[*opcode], index + BYTE_VAL);
    sCount = 2;
    break;

  case SIPUSH:
    sprintf( s, "%4d %s %d", index, gJavaOps[*opcode], BYTE_VAL );
    sCount = 2;
    break;

    // Single byte value.
  case BIPUSH:
    sprintf( s, "%4d %s %d", index, gJavaOps[*opcode], (*(opcode+1)) );
    sCount = 1;
    break;

        // Single byte value.
  case RET:
  case ILOAD:
  case ISTORE:
  case ALOAD:
  case ASTORE:
  case LLOAD:
  case LSTORE:
  case LALOAD:
  case LASTORE:
  case DLOAD:
  case DSTORE:
  case DALOAD:
  case DASTORE:
    sprintf( s, "%4d %s %d", index, gJavaOps[*opcode], (*(opcode+1)) );
    sCount = 1;
    break;

  case NEWARRAY:
  	char *t;
  	switch ( *(opcode+1) ) {
    case T_BOOLEAN:
    	t = "boolean";
      break;
    case T_CHAR:
    	t = "char";
      break;
    case T_FLOAT:
    	t = "float";
      break;
    case T_DOUBLE:
    	t = "double";
      break;
    case T_BYTE:
    	t = "byte";
      break;
    case T_SHORT:
    	t = "short";
      break;
    case T_INT:
    	t = "int";
      break;
    case T_LONG:
    	t = "long";
      break;
    default:
    	t = "**unknown**";
    }
    sprintf( s, "%4d %s %s", index, gJavaOps[*opcode], t );
  	sCount = 1;
    break;

    // No operands.
  default:
    sprintf( s, "%4d %s ", index, gJavaOps[*opcode] );
  }

  return true;
}


u1 CJavaClass::GetByteCodeInc( u2 method_index, u4 *offset ) {
  u1 *opcode = &OPCODE(method_index, *offset);
  // Check that the index is valid.
  if ( *offset >= GetCodeLength(method_index) )
    throw EError( kJVM_ERROR, 23 );

  *offset += CJavaClass::BytecodeLength( method_index, *offset);

  return *opcode;
}

int CJavaClass::BytecodeLength(u2 method_index, u4 offset) {
  int oldOffset = offset;
  u1 bytecode = OPCODE(method_index, offset);
  int lo, hi, npairs;
  
  switch ( bytecode ) {

  case LOOKUPSWITCH:
    offset += 1;
    offset = ((((offset)+3)>>2)<<2);
    offset += 4; // Skip the default.
    npairs = (OPCODE(method_index, offset)<<24)|(OPCODE(method_index, offset+1)<<16)|(OPCODE(method_index, offset+2)<<8)|(OPCODE(method_index, offset+3));
    offset += 4 + npairs*8;
    break;

  case TABLESWITCH:
    // This is tricky. Work out the number of pad bytes required to get
    // a 32 bit boundary. Point to the default.
    offset += 1;
    offset = ((((offset)+3)>>2)<<2);
    offset += 4; // Skip the default.
    lo = (OPCODE(method_index, offset)<<24)|(OPCODE(method_index, offset+1)<<16)|(OPCODE(method_index, offset+2)<<8)|(OPCODE(method_index, offset+3));
    hi = (OPCODE(method_index, offset+4)<<24)|(OPCODE(method_index, offset+5)<<16)|(OPCODE(method_index, offset+6)<<8)|(OPCODE(method_index, offset+7));
    offset += 8 + ((hi-lo)+1)*4;
    break;

    // Constant pool reference (2 byte index).
  case INVOKESTATIC:
  case INVOKESPECIAL:
  case INVOKEVIRTUAL:
  case GETSTATIC:
  case GETFIELD:
  case PUTFIELD:
  case PUTSTATIC:
  case CHECKCAST:
  case INSTANCEOF:
  case LDC_W:
  case LDC2_W:
  case IINC:
  case IFNONNULL:
  case IFNULL:
  case IF_ACMPEQ:
  case IF_ACMPNE:
  case IF_ICMPEQ:
  case IF_ICMPNE:
  case IF_ICMPLE:
  case IF_ICMPGE:
  case IF_ICMPLT:
  case IF_ICMPGT:
  case IFEQ:
  case IFNE:
  case IFLT:
  case IFGT:
  case IFGE:
  case IFLE:
  case GOTO:
  case SIPUSH:
  case ANEWARRAY:
  case NEW:
  case JSR:
    offset += 3;
    break;

  case BIPUSH:
  case ILOAD:
  case RET:
  case ISTORE:
  case NEWARRAY:
  case LDC:
  case ALOAD:
  case ASTORE:
    offset += 2;
    break;

    // No operands.
  default:
    offset += 1;
  }
  return offset - oldOffset;
}

void CJavaClass::SetByteCode( u2 method_index, u4 offset, u1 value ) {
  // Check that the index is valid.
  if ( offset >= GetCodeLength(method_index) )
    throw EError( kJVM_ERROR, 23 );

  methods[method_index].code_attribute.code[offset] = value;
}

short CJavaClass::NumStaticFields() {
  short numFields = 0;

  for ( int i = 0; i < fieldsCount; i++ ) 
    if ( !FieldIsFinal(i)
	 && (fields[i].access_flags & ACC_STATIC) )
      numFields++;

  return numFields;
}

short CJavaClass::NumNonStaticFields() {
  short numFields = 0;

  for ( int i = 0; i < fieldsCount; i++ )
    if ( !FieldIsFinal(i) 
	 && !(fields[i].access_flags & ACC_STATIC) )
      numFields++;

  return numFields;
}

char *CJavaClass::GetLocalVariableName( u2 method_index, u2 local_index, int pc )
{
	if ( methods[method_index].code_attribute.local_variable_table_length == 0 )
  	throw EError( kJVM_ERROR, 24, GetJavaClassName() );

  // Need to search the table since there might be multiple entries.
  for ( int i = 0; i < methods[method_index].code_attribute.local_variable_table_length; i++ ) {
    Local_variable_table *l = &methods[method_index].code_attribute.local_variable_table[i];

    if ( l->index == local_index && pc >= l->start_pc && pc <= l->start_pc + l->length )
      return ConstantUtf8(l->name_index);
  }

  throw EError( kJVM_ERROR, 27);
}

char *CJavaClass::GetLocalVariableName( u2 method_index, u2 local_index )
{
	if ( methods[method_index].code_attribute.local_variable_table_length == 0 )
  	throw EError( kJVM_ERROR, 24, GetJavaClassName() );

  // Need to search the table since there might be multiple entries.
  for ( int i = 0; i < methods[method_index].code_attribute.local_variable_table_length; i++ ) {
    Local_variable_table *l = &methods[method_index].code_attribute.local_variable_table[i];

    if ( l->index == local_index )
      return ConstantUtf8(l->name_index);
  }

  throw EError( kJVM_ERROR, 27);
}

char *CJavaClass::GetLocalVariableType( u2 method_index, u2 local_index, int pc )
{
	if ( methods[method_index].code_attribute.local_variable_table_length == 0 )
  	throw EError( kJVM_ERROR, 24, GetJavaClassName() );

  // Need to search the table since there might be multiple entries.
  for ( int i = 0; i < methods[method_index].code_attribute.local_variable_table_length; i++ ) {
    Local_variable_table *l = &methods[method_index].code_attribute.local_variable_table[i];

    if ( l->index == local_index && pc >= l->start_pc && pc <= l->start_pc + l->length )
      return ConstantUtf8(l->descriptor_index);
  }

  throw EError( kJVM_ERROR, 27);
 }

u2 CJavaClass::GetLocalVariableIndex( u2 method_index, u2 local_index, int pc )
{
	if ( methods[method_index].code_attribute.local_variable_table_length == 0 )
  	throw EError( kJVM_ERROR, 24, GetJavaClassName() );

  // Need to search the table since there might be multiple entries.
  for ( int i = 0; i < methods[method_index].code_attribute.local_variable_table_length; i++ ) {
    Local_variable_table *l = &methods[method_index].code_attribute.local_variable_table[i];

    if ( l->index == local_index && pc >= l->start_pc && pc <= l->start_pc + l->length )
      return l->index;
  }

  throw EError( kJVM_ERROR, 27);
}

bool CJavaClass::LocalVariableInScope( u2 method_index, u2 local_index, int pc ) {
  bool valid = false;

  /* The local variable table might contain multiple entries for a local variable
     if the same variable name is used more than once in a function. Thus we must
     search the local variable table, rather than using local_index as an index. */

  for ( int i = 0; i < methods[method_index].code_attribute.local_variable_table_length; i++ ) {
    Local_variable_table *l = &methods[method_index].code_attribute.local_variable_table[i];

    if ( l->index == local_index && pc >= l->start_pc && pc <= l->start_pc + l->length )
      valid = true;
  }

  return valid;
}

void CJavaClass::FormatType( char *s, char *type ) {
	char suffix[50];

  suffix[0] = '\0';
	s[0] = '\0';

  while ( *type ) {
    switch ( *type ) {
    case ')':
      strcat(suffix,")");
      break;
    case '(':
      strcat(suffix,"(");
      break;
    case 'B':
      strcat(s,"byte");
      break;
    case 'C':
    	strcat(s,"char");
      break;
    case 'F':
      strcat(s,"float");
      break;
    case 'I':
      strcat(s,"int");
      break;
    case 'S':
      strcat(s,"short");
      break;
    case 'Z':
    	strcat(s,"boolean");
      break;

    case 'L':
      while ( *(++type) != ';' ) { 
      	s[strlen(s)+1] = '\0';
        s[strlen(s)] = ((*type)=='/'?'.':(*type));
      }
      break;

    case '[':
    	strcat(suffix,"[]");
      break;

    case 'J':
    	strcat(s,"long");
      break;
    case 'D':
    	strcat(s,"double");
      break;

    default:
      throw EError( kJVM_ERROR, 25, *type );
    }
    type++;
  }
  strcat(s,suffix);
}

int CJavaClass::JemLine2JavaLine( u2 method_index, int pc ) {
  int length = methods[method_index].code_attribute.line_number_table_length;
  Line_number_table *t = methods[method_index].code_attribute.line_number_table;
  int line_number, min_start_pc = -1;

  pc -= GetCodeOffset( method_index );

  // Do a linear search on the line number table
  // We are trying to find the first entry where start_pc[i] <= pc < start_pc[i+1].

	if ( length == 0 )
  	throw EError( kJVM_ERROR, 26, GetJavaClassName() );

  line_number = t[length-1].line_number;

  for (int i = 0; i < length; i++ ) {
    if ( t[i].start_pc <= pc && t[i].start_pc > min_start_pc ) {
      min_start_pc = t[i].start_pc;
      line_number = t[i].line_number;
    }
  }

  return line_number;
}

int CJavaClass::NearestBreakLine( int lineNumber ) {
	int bestLine = -1, bestDistance = 100000;

	for ( int m = 0; m < methodsCount; m++ ) {
    if ( MethodIsNative(m) )
      continue;
  	int length = methods[m].code_attribute.line_number_table_length;
  	Line_number_table *t = methods[m].code_attribute.line_number_table;

    if ( !t )
      continue; // throw EError( kIDE_ERROR, 40, className );


    if ( t[0].line_number > lineNumber )
    	continue;

    for ( int i = 0; i < length; i++ )
     	if ( t[i].line_number >= lineNumber && t[i].line_number - lineNumber < bestDistance ) {
      	bestLine = t[i].line_number;
        bestDistance = t[i].line_number - lineNumber;
        break;
      }
  }

  if ( bestLine == -1 )
  	throw EError( kIDE_ERROR, 20 );
  else
  	return bestLine;
}

int CJavaClass::Java2JemLine( int lineNumber, int *methodNum ) {
	int bestLine = -1, bestMethod, bestDistance = 1000;

	for ( int m = 0; m < methodsCount; m++ ) {
    if ( MethodIsNative(m) )
      continue;

 	  int length = methods[m].code_attribute.line_number_table_length;
  	Line_number_table *t = methods[m].code_attribute.line_number_table;

    if ( !t )
      continue; // throw EError( kIDE_ERROR, 40, className );

    if ( t[0].line_number > lineNumber )
    	continue;

    for ( int i = 0; i < length; i++ )
     	if ( t[i].line_number >= lineNumber && t[i].line_number - lineNumber < bestDistance ) {
      	bestLine = t[i].start_pc;
        bestMethod = m;
        bestDistance = t[i].line_number - lineNumber;
        break;
      }
  }

  if ( bestLine == -1 )
  	throw EError( kIDE_ERROR, 20 );
  else {
    *methodNum = bestMethod;
		return GetCodeOffset(bestMethod) + bestLine;
  }
}

void CJavaClass::AdjustLineNumbers( int method_index, int adjustment ) {
  int length = methods[method_index].code_attribute.line_number_table_length;
  Line_number_table *t = methods[method_index].code_attribute.line_number_table;

  for ( int i = 0; i < length; i++ ) {
    if ( t[i].start_pc != 0 )
      t[i].start_pc += adjustment;
  }
}

void CJavaClass::GetExceptionInfo( u2 method_index, int exceptionNumber,
                 int *startPC, int *endPC, int *handlerPC, char **catchType ) {
  Exception_table *e = &methods[method_index].code_attribute.exception_table[exceptionNumber];

  *startPC = e->start_pc;
  *endPC = e->end_pc;
  *handlerPC = e->handler_pc;
  *catchType = ConstantUtf8(ClassName(e->catch_type));
}

bool CJavaClass::FieldIsFinal( int field_index ) {
  char t = *GetFieldType(field_index);

  return (fields[field_index].access_flags & ACC_FINAL) && t != 'L' && t != '[';
}

