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

#include "CJEMLinker.h"
#include "Errors.h"
#include "NativeMethods.h"
#include "OptionVar.h"
#include "Java.h"
#include <stdarg.h>
#include <errno.h>

#ifdef WIN32
#define STRNCMPI strncmpi
#else
#define STRNCMPI strncasecmp
#endif

#define HI(x) (((x)&0x0000ff00)>>8)
#define LO(x) ((x)&0x000000ff)

const int kJumpSplit = 51; // Point where split occurs in jump table.
const int kStringObjectSize = 10;

const int kClassHeaderSize = 3;
const int kMaxStaticFields = 127;

const int kMaxBinaryLength = 64 * 1024;

CJEMLinker *gLinker;

CJEMLinker::CJEMLinker() {
  numClasses = 0;
  numStaticFields = 0;
  numStaticInits = 0;
  numStrings = 0;
  classPath = NULL;
  binary = NULL;
  stringInfo = NULL;
  callbackLinked = false;

  for ( int i = 0; i < kMaxNumClasses; i++ ) {
    classLinked[i] = false;
    staticInit[i] = 0;
    classStringsSize[i] = 0;
    classSize[i] = 0;
  }

  // Allocate space for the strings.
  stringInfo = (tStringInfo*) malloc(sizeof(tStringInfo) * kMaxNumStrings );
  FailNULL(stringInfo);

  gLinker = this;
}

CJEMLinker::~CJEMLinker() {
  for ( int i = 0; i < numClasses; i++ )
    delete classList[i];

  if ( classPath )
  	free( classPath );

  if ( binary )
  	free( binary );

  if ( stringInfo )
    free( stringInfo );
}

void CJEMLinker::LinkProgram( char *rootFile, char *aClassPath ) {
	if ( binary )
  	free( binary );
  binaryPointer = 0;

  LoadNativeMethods(); // Load the native method table.

	classPath = strdup(aClassPath);
  FailNULL(classPath);

  rootClass = GetClass( rootFile );

  LinkPass();

  binary = (char*)malloc(binaryLength);
  FailNULL(binary);

  if ( gOptions.debugZeroStackOffset )
    stackBase = 0;
  else
    stackBase = ((binaryLength/16)+1)*16;

  OutputPass();
}

void CJEMLinker::LinkPass() {
  u2 main_method;
  CJavaClass *rootClass = classList[0];
  bool linked;
  int i;

  if ( gOptions.debugLinker )
    StatusDebug("Linker: linking pass" );

  // Check that the root class contains the `main' method.
/* TODO: Work out why this code is unnecessary. */
/*
  try {
    main_method = rootClass->GetMethodByName( "main", "()V" );
    // The main method exists. Check that it is defined as `public static void main()'
    // ??
  }
  catch (EError e) {
    throw EError( kLINK_ERROR, 2 );
  }
*/

  // Force the Throwable class to be linked. Necessary for throwing VM exceptions.
  (void) GetClass( "java/lang/Throwable" );

  // Force the Array class to be linked. Necessary for using arrays.
/* TODO: Work out why this code is commented out. */
  // (void) GetClass( "celsius/core/Array" );


  // Resolve all references, loading classes as required.

  // This routine works recursively. As a result, some classes may be resolved
  // more than once. It would be better to try and do them in order.
  do {
    linked = false;
    for ( i = 0; i < numClasses; i++ ) {
      if ( !classLinked[i] ) {
				ResolveClass( classList[i] );
				classLinked[i] = true;
				linked = true;
      }
    }
  }
  while ( linked );

  // Compute static field indexes.
  for ( i = 0; i < numClasses; i++ ) {
    CJavaClass *j = classList[i];

    for ( int f = 0; f < j->NumFields(); f++ )
      if ( j->FieldIsStatic(f) && !j->FieldIsFinal(f) ) {
        if ( numStaticFields >= kMaxNumStaticFields )
          throw EError(kLINK_ERROR,7);
        staticFields[numStaticFields].fieldClass = j;
        staticFields[numStaticFields].fieldNum = f;
				j->SetFieldIndex( f, numStaticFields++ );
      }
  }

  if ( numStaticFields > kMaxStaticFields )
    throw EError( kIDE_ERROR, 41, kMaxStaticFields );

  // Loop through each class again. This time statically linking
  // all methods and fields.
  for ( i = 0; i < numClasses; i++ ) {
    CJavaClass *j = classList[i];

    j->StaticLink(this);
  }

  // Loop through the classes again, finding those with static initialisers.
  // ## The next loop is slow. Use hash table for names.
  for ( i = 0; i < numClasses; i++ ) {
    CJavaClass *j = classList[i];

    try {
      int offset = j->GetLinkedMethodOffsetByName("<clinit>", "()V");
      numStaticInits++;
    }
    catch (...) {}
  }

  // Compute the offset to the start of each method.
  ComputeOffsets();

  // Loop through the classes again, finding those with static initialisers.
  int k = 0;
  for ( i = 0; i < numClasses; i++ ) {
    CJavaClass *j = classList[i];

    try {
      int offset = j->GetLinkedMethodOffsetByName("<clinit>", "()V");

      staticInit[k++] = offset;
    }
    catch (...) {}
  }

  CJavaClass *mainClass = NULL;

  for ( i = 0; i < numClasses; i++ ) {
    try {
      classList[i]->GetMethodByName( "main", "()V" );
      // Check that only one class contains a method called `main'.
      if ( mainClass != NULL )
        throw EError(kLINK_ERROR, 6 );
      mainClass = classList[i];
    }
    catch (...) {}
  }

  if ( !mainClass )
    throw EError(kLINK_ERROR, 2 );


  mainClass->AdjustLineNumbers(mainClass->GetMethodByName( "main", "()V" ), numStaticInits*3);
}

int CJEMLinker::StaticAdjustment(CJavaClass *j, int methodIndex ) {
  if ( !strcmp( j->GetMethodName(methodIndex), "main") )
    return numStaticInits * 3;
  else
    return 0;
}

void CJEMLinker::OutputPass() {
  int paramsSize, localsSize;
  CJavaClass *currentClass = classList[0];
  u2 method;
  u4 offset;

  if ( gOptions.debugLinker )
    StatusDebug("Linker: output pass." );

  if ( gOptions.debugJEMFile ) {
    jemFile = fopen( "jem.out", "wt" );
    if ( !jemFile )
  	  throw EError(kIDE_ERROR, 17, "jem.out", errno, _sys_errlist[errno]  );
  }

  try {
  // Output a dummy byte for the run/debug type
  OutputByte(  0xA4, "\tdw\t%d\t; Placeholder for run/debug.\n",  0xA4 );
  // Output a dummy byte for the length
  OutputByte( 0, "\tdw\t$0\t; Placeholder for the length.\n" );

  // Output the version number.
  OutputByte( (kJemMajorVersion<<4) | (kJemMinorVersion), "\tdw\t$%X\t; Version: %d.%d\n",
	      (kJemMajorVersion<<4) | (kJemMinorVersion), kJemMajorVersion, kJemMinorVersion );

  OutputByte( 2*numStaticFields, "\tdw\t%d\t; Number of static fields = %d\n", 2*numStaticFields, numStaticFields );
  staticLength = 2*numStaticFields;
  // The `main' method
  // Check that the root class contains the `main' method.
  try {
    // Check that the main method is static
    if ( !rootClass->LinkedMethodIsStatic("main","()V"))
      throw EError( kLINK_ERROR, 2 );

    offset = rootClass->GetLinkedMethodOffsetByName( "main", "()V" );
    OutputByte( HI(offset) , "\tdw\t%d\t; `main' method: Offset hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;                       lo = %d\n", LO(offset), LO(offset) );
  }
  catch (EError e) {
    throw EError( kLINK_ERROR, 2 );
  }
  // The java.lang.Object class
  offset = GetClassOffset(GetClass( "java/lang/Object" ));
  OutputByte( HI(offset) , "\tdw\t%d\t; java.lang.Object class: Offset hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;                                lo = %d\n", LO(offset), LO(offset) );
  // The java.lang.String class
  if ( numStrings > 0 )
    offset = GetClassOffset(GetClass( "java/lang/String" ));
  else
    offset = 0;
  OutputByte( HI(offset) , "\tdw\t%d\t; java.lang.String class: Offset hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;                                lo = %d\n", LO(offset), LO(offset) );
  // The java.lang.Throwable.throwVMException method stub.
  offset = GetClass( "java/lang/Throwable" )->GetLinkedMethodOffsetByName( "throwVMException", "(I)V" );
  OutputByte( HI(offset) , "\tdw\t%d\t; Throwable.throwVMException method: Offset hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;                                           lo = %d\n", LO(offset), LO(offset) );
  // The offset to the string table.
  offset = stringTableOffset;
  OutputByte( HI(offset) , "\tdw\t%d\t; String table offset hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;                     lo = %d\n", LO(offset), LO(offset) );
  // The offset to the callback method (only if required).
  if ( callbackLinked ) 
    offset = GetClass( "celsius/core/Callback" )->GetLinkedMethodOffsetByName( "callback", "(IIIII)V" );
  else
    offset = 0;
  OutputByte( HI(offset) , "\tdw\t%d\t; Callback method offset hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;                        lo = %d\n", LO(offset), LO(offset) );
  // The java.lang.OutOfMemoryError class
  offset = GetClassOffset(GetClass( "java/lang/OutOfMemoryError" ));
  OutputByte( HI(offset) , "\tdw\t%d\t; java.lang.OutOfMemoryError class: Offset hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;                                          lo = %d\n", LO(offset), LO(offset) );

  // The stack base.
  offset = stackBase;
  OutputByte( HI(offset) , "\tdw\t%d\t; Stack base hi = %d\n", HI(offset), HI(offset) );
  OutputByte( LO(offset) , "\tdw\t%d\t;            lo = %d\n", LO(offset), LO(offset) );

  // The dummy array class.
  //offset = GetClassOffset(GetClass( "celsius/core/Array" ));
  //OutputByte( HI(offset) , "\tdw\t%d\t; Stack base hi = %d\n", HI(offset), HI(offset) );
  //OutputByte( LO(offset) , "\tdw\t%d\t;            lo = %d\n", LO(offset), LO(offset) );


  // Loop through each class to create the headers
  for ( int c = 0; c < numClasses; c++ ) {
    currentClass = classList[c];

    OutputComment("\n; (%d) Class = `%s'\n", classOffset[c], currentClass->GetClassName() );

    // Output the superclass offset.
    if ( currentClass->GetSuperClassName() != NULL )
      offset = classOffset[GetClassIndex( currentClass->GetSuperClassName() )];
    else
      offset = -1;
    OutputByte( HI(offset) , "\tdw\t%d\t; Superclass offset hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t; Superclass offset lo = %d\n", LO(offset), LO(offset) );

    // Ouput number of words of fields.
    int numFields = currentClass->GetNumLinkedFields();
    OutputByte( 2*numFields, "\tdw\t%d\t; Number of (non-final, non-static) fields = %d\n", 2*numFields, numFields );

    // Loop for each method of the class.
    for ( method = 0; method < currentClass->GetNumLinkedMethods(); method++ ) {
      int methodIndex = currentClass->GetLinkedMethodIndex(method);
      CJavaClass *methodClass = currentClass->GetLinkedMethodClass(method);

      paramsSize = methodClass->GetParamsSize( methodIndex );
      if ( methodClass->MethodIsNative( methodIndex ) )
      	localsSize = 0;
      else
      	localsSize = methodClass->GetLocalsSize( methodIndex ) - paramsSize;

      OutputComment("; Method = `%s'\n", methodClass->GetMethodName( methodIndex ) );

      // Output the number of parameters.
      if ( methodClass->GetNumMethodExceptions( methodIndex ) > 0 )
        OutputByte( (paramsSize*2)|0x80, "\tdw\t%d\t; Number of parameters = %d (exception table present)\n", (paramsSize*2)|0x80, paramsSize );
      else
        OutputByte( paramsSize*2, "\tdw\t%d\t; Number of parameters = %d\n", paramsSize*2, paramsSize );

      // Number of locals.
      OutputByte( localsSize*2, "\tdw\t%d\t; Number of locals = %d\n", localsSize*2, localsSize );

      // Offset to the method.
      int flags = 0;
     if ( methodClass->MethodIsNative( methodIndex ) ) {
	      offset = NativeMethodOffset( methodClass->GetClassName(),
                                     methodClass->GetMethodName( methodIndex ) );
	      flags |= kNativeBit;
      }
      else
 	      offset = methodClass->GetCodeOffset( methodIndex );
      OutputByte( flags , "\tdw\t%d\t; Flags hi = %d\n", flags, flags );
      OutputByte( HI(offset) , "\tdw\t%d\t; Offset hi = %d\n", HI(offset), HI(offset) );
      OutputByte( LO(offset) , "\tdw\t%d\t; Offset lo = %d\n", LO(offset), LO(offset) );

            // Compute the exception table offset.
      if ( methodClass->GetNumMethodExceptions( methodIndex ) > 0 ) {
        offset = methodClass->GetCodeOffset( methodIndex );
        offset += methodClass->GetCodeLength( methodIndex );
        offset += StaticAdjustment(methodClass, methodIndex);
        OutputByte( HI(offset) , "\tdw\t%d\t; Exception table offset hi = %d\n", HI(offset), HI(offset) );
        OutputByte( LO(offset) , "\tdw\t%d\t; Exception table offset lo = %d\n", LO(offset), LO(offset) );
      }
      else {
        offset = 0;
        OutputByte( HI(offset) , "\tdw\t%d\t; Empty exception table hi = %d\n", HI(offset), HI(offset) );
        OutputByte( LO(offset) , "\tdw\t%d\t; Empty exception table lo = %d\n", LO(offset), LO(offset) );
      }
    }
  }

  // Loop through each class again to output the bytecodes.
  for ( int c = 0; c < numClasses; c++ ) {
    u2 poolIndex, fieldNum, fieldIndex;
    CJavaClass *methodClass;
    char *className;

    currentClass = classList[c];

    OutputComment("\n; Class = `%s'\n", currentClass->GetClassName() );

    // Loop for each method of the class.
    for ( method = 0; method < currentClass->NumMethods(); method++ ) {
      OutputComment("; (%d) Method %s /* %s */\n", currentClass->GetCodeOffset(method),
                                                   currentClass->GetMethodName(method),
      currentClass->GetMethodType( method ) );

      // Is the method native?
      if ( currentClass->MethodIsNative( method ) ) {
      	OutputComment(";      Native\n");
				continue;
      }

      // Is this the main method?
      if ( !strcmp( currentClass->GetMethodName(method), "main") ) {
        // Output code to call the static initialisers.
        for ( int s = 0; s < numStaticInits; s++ ) {
          offset = staticInit[s];

          OutputByte( Java2JemByte(INVOKESTATIC), "\tdw\t%d\t;      invokestatic <??.<clinit>>\n", Java2JemByte(INVOKESTATIC) );
          OutputByte( HI(offset) , "\tdw\t%d\t;      Offset hi = %d\n", HI(offset), HI(offset) );
          OutputByte( LO(offset) , "\tdw\t%d\t;      Offset lo = %d\n", LO(offset), LO(offset) );
        }
      }

      // Output the bytecodes.
      for ( u4 i = 0; i < currentClass->GetCodeLength( method ); i++ ) {
				char buffer[1024];
				u1 bytecode, jemcode;
        bool isOpcode;
        tStringInfo *si;
        int k, defaultVal, hiVal, loVal, start, npairs;
        
				isOpcode = currentClass->DisassembleByte( method, i, buffer, 1020 );
				bytecode = currentClass->GetByteCode( method, i );
  			if ( isOpcode ) {
  				jemcode = Java2JemByte(bytecode);

					switch ( bytecode ) {
         	case NEW:
	  				poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
	  				className = currentClass->ConstantUtf8( currentClass->ClassName( poolIndex ) );
	  				offset = classOffset[GetClassIndex( className )];
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( HI(offset), "\tdw\t$%2.2X\t;      %d\n", HI(offset), HI(offset) );
	  				OutputByte( LO(offset), "\tdw\t$%2.2X\t;      %d\n", LO(offset), LO(offset) );
	  				i += 2;
	  				break;

         	case CHECKCAST:
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( HI(offset), "\tdw\t$%2.2X\t;      %d\n", HI(0), HI(0) );
	  				OutputByte( LO(offset), "\tdw\t$%2.2X\t;      %d\n", LO(0), LO(0) );
	  				i += 2;
            break;

          case INSTANCEOF:
	  				poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
	  				className = currentClass->ConstantUtf8( currentClass->ClassName( poolIndex ) );
	  				offset = classOffset[GetClassIndex( className )];
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( HI(offset), "\tdw\t$%2.2X\t;      %d\n", HI(offset), HI(offset) );
	  				OutputByte( LO(offset), "\tdw\t$%2.2X\t;      %d\n", LO(offset), LO(offset) );
	  				i += 2;
	  				break;

          case ANEWARRAY:
          case NEWARRAY:
            OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
            if ( bytecode == ANEWARRAY )
              OutputByte( 0, "\tdw\t$%2.2X\t;      %d\n", 0, 0 );
            {
              int type = currentClass->GetByteCode( method, i+1 );
              if ( type == T_BOOLEAN 
                   || type == T_CHAR
                   || type == T_BYTE ) 
                offset = 1;
              else
                offset = 0;
	  				  OutputByte( offset, "\tdw\t$%2.2X\t;      %d\n", offset, offset );
            }
            if ( bytecode == ANEWARRAY )
              i += 2;
            else
              i += 1;
            break;

					case GETFIELD:
					case PUTFIELD:
	  				poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
	  				className = currentClass->ConstantUtf8( currentClass->ClassName(currentClass->FieldClass( poolIndex )));
          	methodClass = GetClass(className);
	  				//fieldNum = methodClass->GetFieldByName( currentClass->ConstantUtf8( currentClass->FieldName( poolIndex )) );
	  				fieldIndex = methodClass->GetLinkedFieldOffset( className, currentClass->ConstantUtf8( currentClass->FieldName( poolIndex )), currentClass->ConstantUtf8( currentClass->FieldType( poolIndex ) ))*2;
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( HI(2+fieldIndex), "\tdw\t$%2.2X\t;      %d\n", HI(2+fieldIndex), HI(2+fieldIndex) );
	  				OutputByte( LO(2+fieldIndex), "\tdw\t$%2.2X\t;      %d\n", LO(2+fieldIndex), LO(2+fieldIndex) );
	  				i += 2;
	  				break;

 					case GETSTATIC:
					case PUTSTATIC:
	  				poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
	  				className = currentClass->ConstantUtf8( currentClass->ClassName(currentClass->FieldClass( poolIndex )));
          	methodClass = GetClass(className);
	  				fieldNum = methodClass->GetFieldByName( currentClass->ConstantUtf8( currentClass->FieldName( poolIndex )) );
	  				fieldIndex = methodClass->GetFieldIndex( fieldNum )*2;
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( HI(fieldIndex), "\tdw\t$%2.2X\t;      %d\n", HI(fieldIndex), HI(fieldIndex) );
	  				OutputByte( LO(fieldIndex), "\tdw\t$%2.2X\t;      %d\n", LO(fieldIndex), LO(fieldIndex) );
	  				i += 2;
            break;

					case INVOKESPECIAL:
  				case INVOKESTATIC:
 	  				poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
	  				className = currentClass->ConstantUtf8( currentClass->ClassName(currentClass->MethodClass( poolIndex )));
          	methodClass = GetClass(className);
	  				offset = methodClass->GetLinkedMethodOffsetByName( currentClass->ConstantUtf8( currentClass->MethodName( poolIndex )),
                                                       currentClass->ConstantUtf8( currentClass->MethodType( poolIndex) ) );
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( HI(offset), "\tdw\t$%2.2X\t;      %d\n", HI(offset), HI(offset) );
	  				OutputByte( LO(offset), "\tdw\t$%2.2X\t;      %d\n", LO(offset), LO(offset) );
	  				i += 2;
            break;

 					case INVOKEVIRTUAL:
	  				poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
	  				className = currentClass->ConstantUtf8( currentClass->ClassName(currentClass->MethodClass( poolIndex )));
          	methodClass = GetClass(className);
	  				offset = methodClass->GetLinkedClassMethodOffsetByName( currentClass->ConstantUtf8( currentClass->MethodName( poolIndex )),
                                                       currentClass->ConstantUtf8( currentClass->MethodType( poolIndex) ) );
            paramsSize = methodClass->GetParamsSize( methodClass->GetMethodByName( currentClass->ConstantUtf8( currentClass->MethodName( poolIndex )),
                                                     currentClass->ConstantUtf8( currentClass->MethodType( poolIndex) ) ) );

            if ( offset >= 256 )
              throw EError(kLINK_ERROR, 9, className);

	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
	  				OutputByte( 2*paramsSize, "\tdw\t$%2.2X\t;      Object offset = -%d\n", 2*paramsSize, 2*paramsSize );
	  				OutputByte( LO(offset), "\tdw\t$%2.2X\t;      %d\n", LO(offset), LO(offset) );
	  				i += 2;
	  				break;

          case LDC:
            poolIndex = currentClass->GetByteCode( method, i+1 );
            si = FindString( currentClass, poolIndex );
            offset = si->index;
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
            OutputByte( LO(offset), "\tdw\t$%2.2X\t;      index = %d\n", LO(offset), LO(offset) );
            i += 1;
            break;

          case LDC_W:
            poolIndex = ((short)(((u2)currentClass->GetByteCode( method, i+1 )<<8)|currentClass->GetByteCode( method, i+2 )));
            si = FindString( currentClass, poolIndex );
            offset = si->index;
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
            OutputByte( 0, "\tdw\t$%2.2X\t; \n", 0);
            OutputByte( LO(offset), "\tdw\t$%2.2X\t;      index = %d\n", LO(offset), LO(offset) );
            i += 2;
            break;

          case LOOKUPSWITCH:
            OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
            start = (((i+4)>>2)<<2);
            defaultVal = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
            start += 4;
            npairs = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
            start += 4;
            OutputByte( HI(defaultVal-1), "\tdw\t$%2.2X\t;\n", HI(defaultVal-1) );
            OutputByte( LO(defaultVal-1), "\tdw\t$%2.2X\t;\n", LO(defaultVal-1) );
            OutputByte( HI(npairs), "\tdw\t$%2.2X\t;\n", HI(npairs) );
            OutputByte( LO(npairs), "\tdw\t$%2.2X\t;\n", LO(npairs) );
            for ( k = 0; k < npairs; k++ ) {
              int val = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
              start += 4;
              int offset = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
              start += 4;
              OutputByte( HI(val), "\tdw\t$%2.2X\t;\n", HI(val) );
              OutputByte( LO(val), "\tdw\t$%2.2X\t;\n", LO(val) );
              OutputByte( HI(offset-1), "\tdw\t$%2.2X\t;\n", HI(offset-1) );
              OutputByte( LO(offset-1), "\tdw\t$%2.2X\t;\n", LO(offset-1) );
            }
            for ( k = 0; k < 4 + npairs*4 + (((i+4)>>2)<<2)-(i+1); k++ )
              OutputByte( 0, "\tdw\t$%2.2X\t;\n", 0 );
            i += currentClass->BytecodeLength(method,i)-1;
            break;

          case TABLESWITCH:
            OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
            start = (((i+4)>>2)<<2);
            defaultVal = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
            start += 4;
            loVal = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
            start += 4;
            hiVal = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
            start += 4;
            OutputByte( HI(defaultVal-1), "\tdw\t$%2.2X\t;\n", HI(defaultVal-1) );
            OutputByte( LO(defaultVal-1), "\tdw\t$%2.2X\t;\n", LO(defaultVal-1) );
            OutputByte( HI(loVal), "\tdw\t$%2.2X\t;\n", HI(loVal) );
            OutputByte( LO(loVal), "\tdw\t$%2.2X\t;\n", LO(loVal) );
            OutputByte( HI(hiVal-loVal), "\tdw\t$%2.2X\t;\n", HI(hiVal-loVal) );
            OutputByte( LO(hiVal-loVal), "\tdw\t$%2.2X\t;\n", LO(hiVal-loVal) );
            for ( k = loVal; k <= hiVal; k++ ) {
              int val = (currentClass->GetByteCode(method, start)<<24)|(currentClass->GetByteCode(method, start+1)<<16)|(currentClass->GetByteCode(method, start+2)<<8)|(currentClass->GetByteCode(method, start+3));
              start += 4;
              OutputByte( HI(val-1), "\tdw\t$%2.2X\t;\n", HI(val-1) );
              OutputByte( LO(val-1), "\tdw\t$%2.2X\t;\n", LO(val-1) );
            }
            for ( k = 0; k < 6 + 2*((hiVal-loVal)+1) + (((i+4)>>2)<<2)-(i+1); k++ )
              OutputByte( 0, "\tdw\t$%2.2X\t;\n", 0 );
            i += currentClass->BytecodeLength(method,i)-1;
            break;

					default:
	  				OutputByte( jemcode, "\tdw\t$%2.2X\t; %s\n", jemcode, buffer );
        	}
      	}
  			else
      		OutputByte( bytecode, "\tdw\t$%2.2X\t; %s\n", bytecode, buffer );
      }
      // Output the exception handler table (if there is one).
      int numExceptions = currentClass->GetNumMethodExceptions( method );
      if ( numExceptions > 0 ) {
        OutputByte( numExceptions, "\tdw\t%d\t; Number of exception handlers = %d\n", numExceptions, numExceptions );
        offset = currentClass->GetCodeOffset( method );

        for ( int e = 0; e < numExceptions; e++ ) {
          int startPC, endPC, handlerPC;
          char *catchType;

          currentClass->GetExceptionInfo( method, e, &startPC, &endPC, &handlerPC, &catchType );
          // Need to convert PC values to .jem file offsets.
          // Basically we just add the PC to the method offset.

          startPC += offset + 1 + StaticAdjustment(currentClass, method); // Add one to compensate for PC increment in JVM.
          endPC += offset + 1 + StaticAdjustment(currentClass, method);
          handlerPC += offset + StaticAdjustment(currentClass, method);

          OutputByte( HI(startPC) , "\tdw\t%d\t; start_pc hi = %d\n", HI(startPC), HI(startPC) );
          OutputByte( LO(startPC) , "\tdw\t%d\t; start_pc lo = %d\n", LO(startPC), LO(startPC) );
          OutputByte( HI(endPC) , "\tdw\t%d\t; end_pc hi = %d\n", HI(endPC), HI(endPC) );
          OutputByte( LO(endPC) , "\tdw\t%d\t; end_pc lo = %d\n", LO(endPC), LO(endPC) );
          // A catchType of NULL is used by finally to indicate that any exception can be
          // caught. Translate this to mean any subclass of Throwable, since all exceptions
          // must be subclasses of Throwable.
          if ( catchType == NULL )
            catchType = "java/lang/Throwable";
          int typeOffset = classOffset[GetClassIndex( catchType )];
          OutputByte( HI(typeOffset) , "\tdw\t%d\t; catchType offset hi = %d\n", HI(typeOffset), HI(typeOffset) );
          OutputByte( LO(typeOffset) , "\tdw\t%d\t; catchType offset lo = %d\n", LO(typeOffset), LO(typeOffset) );
          OutputByte( HI(handlerPC) , "\tdw\t%d\t; handler_pc hi = %d\n", HI(handlerPC), HI(handlerPC) );
          OutputByte( LO(handlerPC) , "\tdw\t%d\t; handler_pc lo = %d\n", LO(handlerPC), LO(handlerPC) );
        }
      }
    }
  }
  programLength = binaryPointer;

  // Output the string table.
  OutputComment("\n; String index table.\n");
  for ( int i = 0; i < numStrings; i++ ) {
    offset = stringInfo[i].offset;
    OutputByte( HI(offset) , "\tdw\t%d\t; String offset hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;               lo = %d\n", LO(offset), LO(offset) );
  }
  OutputComment("\n; String data table.\n");
  for ( int i = 0; i < numStrings; i++ ) {
    offset = GetClassOffset(GetClass("java/lang/String"));
    OutputByte( HI(offset) , "\tdw\t%d\t; String class hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;              lo = %d\n", LO(offset), LO(offset) );

    offset = strlen(stringInfo[i].data);
    OutputByte( HI(offset) , "\tdw\t%d\t; String length hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;               lo = %d\n", LO(offset), LO(offset) );

    offset = binaryPointer+2;
    OutputByte( HI(offset) , "\tdw\t%d\t; Pointer hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;         lo = %d\n", LO(offset), LO(offset) );

    offset = GetClassOffset(GetClass("java/lang/Object"));
    OutputByte( HI(offset) , "\tdw\t%d\t; Object class hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;              lo = %d\n", LO(offset), LO(offset) );

    offset = strlen(stringInfo[i].data);
    OutputByte( HI(offset) , "\tdw\t%d\t; String length hi = %d\n", HI(offset), HI(offset) );
    OutputByte( LO(offset) , "\tdw\t%d\t;               lo = %d\n", LO(offset), LO(offset) );

    OutputComment("String Data: \"%s\"\n", stringInfo[i].data);
    for ( int s = 0; s < strlen(stringInfo[i].data); s++ ) {
      //OutputByte( 0 , "" ); // High byte is always zero.
      OutputByte( stringInfo[i].data[s] , "%c", stringInfo[i].data[s] );
    }
    OutputComment("\n");
  }
  OutputComment("\n\n");

  stringsLength = binaryPointer - programLength;

  binary[kLengthOffset] = (binaryLength/256)+1;
  if ( binary[kLengthOffset] < 20 )
    binary[kLengthOffset] = 20;

  OutputNativeDispatchTable();
  OutputJavaDispatchTable();

	}
  catch(EError e) {
    if ( gOptions.debugJEMFile ) {
  	  fclose( jemFile );
      jemFile = NULL;
    }
    throw e;
  }
  if ( gOptions.debugJEMFile ) {
    fclose( jemFile );
    jemFile = NULL;

    // Save the JEM file as hexadecimal.
    FILE *hexFile = fopen( "jem.hex", "wt" );
    if ( !hexFile )
  	  throw EError(kIDE_ERROR, 17, "jem.hex", errno, _sys_errlist[errno] );
    for ( int i = 0; i < binaryPointer; i++ )
      fprintf(hexFile,"%2.2X ", binary[i]&0xff );
    fclose(hexFile);
    
    // Save the JEM file as binary.
    FILE *binFile = fopen( "jem.bin", "wb" );
    if ( !binFile )
      throw EError(kIDE_ERROR, 17, "jem.bin", errno, _sys_errlist[errno] );
    fwrite(binary, binaryPointer, 1, binFile );
    fclose(binFile);
  }
}

int CJEMLinker::GetClassOffset( CJavaClass *j ) {
  for ( int i = 0; i < numClasses; i++ )
    if ( classList[i] == j )
      return classOffset[i];

  throw EError(kIDE_ERROR, 19, j->GetJavaClassName());
}

bool CJEMLinker::StringExists(char *s, int stringCount) {
  for (int i = 0; i < stringCount; i++) {
    if (!strcmp(stringInfo[i].data, s)) {
      return true;
    }
  }
  return false;
}

void CJEMLinker::ComputeOffsets() {
  u4 globalOffset;
  int m;

  if ( gOptions.debugLinker )
    StatusDebug("Linker: Computing offsets." );

  // Compute the offset for the class descriptions.

  globalOffset = kHeaderSize; // Skip over the version and main stub offset.

  for ( int i = 0; i < numClasses; i++ ) {
    CJavaClass *currentClass = classList[i];
    int classMethodOffset = kClassHeaderSize;

    classOffset[i] = globalOffset;
    globalOffset += kClassHeaderSize; // Fixed size for each class.
    classSize[i] = kClassHeaderSize;

    for ( m = 0; m < currentClass->GetNumLinkedMethods(); m++ ) {
    	currentClass->SetMethodOffset( m, globalOffset );
      currentClass->SetClassMethodOffset(m, classMethodOffset);
      if ( currentClass->GetLinkedMethodClass(m)->GetNumMethodExceptions( currentClass->GetLinkedMethodIndex( m ) ) > 0 ) {
        globalOffset += 7;
        classMethodOffset += 7;
        classSize[i] += 7;
      }
      else {
    	  globalOffset += 7; // Use 6 rather than 4 so that subclassing works.
        classMethodOffset += 7; // Use 6 rather than 4 so that subclassing works.
        classSize[i] += 7;
      }
    }
  }

  // Loop through each class and method computing the offsets.

  for ( int i = 0; i < numClasses; i++ ) {
    CJavaClass *currentClass = classList[i];

    for ( m = 0; m < currentClass->NumMethods(); m++ ) {
      if ( gOptions.debugLinker )
      	StatusDebug("Offset of %s is %d", currentClass->GetMethodName(m), globalOffset );
      currentClass->SetCodeOffset( m, globalOffset );
      globalOffset += currentClass->GetCodeLength( m );
      classSize[i] += currentClass->GetCodeLength( m );
      globalOffset += StaticAdjustment(currentClass, m);
      classSize[i] += StaticAdjustment(currentClass, m);

      // Add space for the exception table.
      if ( currentClass->GetNumMethodExceptions( m ) > 0 ) {
        globalOffset++;
        globalOffset += currentClass->GetNumMethodExceptions( m ) * 8;
        classSize[i] += 1 + currentClass->GetNumMethodExceptions( m ) * 8;
      }
    }

  }
  binaryLength = globalOffset;

  // Compute the string offsets.
  int stringOffset = globalOffset, stringCount = 0;
  stringTableOffset = stringOffset;

  stringOffset += numStrings * 2;
  for ( int i = 0; i < numClasses; i++ ) {
    CJavaClass *j = classList[i];

    classStringsSize[i] = 0;
    
    for ( m = 0; m < j->NumMethods(); m++ ) {
     int l = 0;
      while ( l < j->MethodLength( m ) ) {
        u4 offset = l;
        u1 opcode = j->GetByteCodeInc( m, &offset );
        u2 poolRef;

        if ( opcode == LDC ) {
          char *s;

          poolRef = j->GetByteCode( m, l+1 );
          s = j->ConstantUtf8( j->StringUtf8(poolRef ));
          if (!StringExists(s, stringCount)) {
            stringInfo[stringCount].index = stringCount;
            stringInfo[stringCount].offset = stringOffset;
            stringInfo[stringCount].poolRef = poolRef;
            stringInfo[stringCount].whichClass = j;
            stringOffset += kStringObjectSize + strlen(s) /**2*/;
            stringInfo[stringCount++].data = s;
            classStringsSize[i] += kStringObjectSize + strlen(s) /**2*/;
          }
        }
        else if ( opcode == LDC_W ) {
          char *s;
          poolRef = ((short)(((u2)j->ByteCodePoolIndex( m, l+1 )<<8)|j->ByteCodePoolIndex( m, l+2 )));
          poolRef = j->ByteCodePoolIndex( m, l+1 );
          s = j->ConstantUtf8( j->StringUtf8(poolRef ));
          if (!StringExists(s, stringCount)) {
            stringInfo[stringCount].index = stringCount;
            stringInfo[stringCount].offset = stringOffset;
            stringInfo[stringCount].poolRef = poolRef;
            stringInfo[stringCount].whichClass = j;
            stringOffset += kStringObjectSize + strlen(s) /**2 */;
            stringInfo[stringCount++].data = s;
            classStringsSize[i] += kStringObjectSize + strlen(s) /**2 */;
          }
        }
        l = offset;
      }
    }
  }
  // Check that we haven't exceeded the maximum number of strings.
  if ( stringCount >= kMaxNumStrings )
    throw EError( kIDE_ERROR, 31, kMaxNumStrings );
  numStrings = stringCount;

  binaryLength = stringOffset;

  if ( binaryLength >= kMaxBinaryLength )
    throw EError( kLINK_ERROR, 8 );
}

void CJEMLinker::OutputNativeDispatchTable() {
  int method = 0;
  char buffer[1025], *s;

  OutputComment(";\n; Native method dispatch table.\n;\n");

  while ( *nativeList[method][kNativeClass] != '\0' ) {
    // Replace all instances of '/' with '_' to keep the assembler happy.
    strcpy( buffer, nativeList[method][kNativeClass] );
    s = buffer;
    while (*s++) 
      if ( *s == '/' )
    *s = '_';

    if ( gOptions.debugJEMFile ) {
      // Print a native method entry.
      fprintf(jemFile, "\tjmp\tnm_%s_%s\t; %d\n", buffer,
              nativeList[method][kNativeMethod], method );
    }

    method++;
  }
}

int CJEMLinker::NativeMethodOffset( char *className, char *methodName ) {
  int i = 0;

  while ( *nativeList[i][kNativeClass] != '\0' ) {
    if ( !strcmp( nativeList[i][kNativeClass], className )
          && !strcmp( nativeList[i][kNativeMethod], methodName ) )
      return i;

    i++;
  }

  throw EError(kLINK_ERROR, 4, className, methodName);
}

void CJEMLinker::OutputByte( u1 byte, char *detail, ... ) {
  va_list ap;
  char s[1024];

  if ( binaryPointer >= binaryLength )
    throw EError( kIDE_ERROR, 27 );

  binary[binaryPointer++] = byte;

  if ( gOptions.debugJEMFile ) {
    va_start(ap,detail);
    //  printf("(%X) ", byte );
    vsnprintf( s, 1000, detail, ap );
    va_end(ap);

    fprintf(jemFile, "%d:", binaryPointer-1);
    fprintf(jemFile, "%s", s);
  }
}

void CJEMLinker::OutputJavaDispatchTable() {
	int count;                                       

  if ( !gOptions.debugJEMFile )
    return;

  OutputComment("; Jem opcodes.\n");

  count = 0;
  while ( gJemOps[count] >= 0 ) {
    if ( gOptions.debugJVM2 ) {
        OutputComment("\tj_%s\t=\t%d\n", gJavaOps[gJemOps[count]], count );
    }
    else {
      if ( count > kJumpSplit )
        OutputComment("\tj_%s\t=\t%d\n", gJavaOps[gJemOps[count]], 2 * count-kJumpSplit );
      else
  	    OutputComment("\tj_%s\t=\t%d\n", gJavaOps[gJemOps[count]], count );
    }
  	count++;
  }

  OutputComment("; Jem dispatch table.\n");

  count = 0;
  while ( gJemOps[count] >= 0 ) {
    if ( gOptions.debugJVM2 ) {
        OutputComment("\tpage\tl_j_%s\n\tjmp\tl_j_%s\t; $%2.2X\n", gJavaOps[gJemOps[count]], gJavaOps[gJemOps[count]], count );
    }
    else {
      if ( count > kJumpSplit ) {
        if ( count == kJumpSplit+1 )
          OutputComment("\tnop\n");
        OutputComment("\tjmp\t@_l_j_%s\t; $%2.2X\n", gJavaOps[gJemOps[count]], 2 * count-kJumpSplit );
      }
      else
  	    OutputComment("\tjmp\tl_j_%s\t; $%2.2X\n", gJavaOps[gJemOps[count]], count );
    }
  	count++;
  }
}

void CJEMLinker::OutputComment( char *detail, ... ) {
  va_list ap;
  char s[1024];

  if ( !gOptions.debugJEMFile )
    return;

  va_start(ap,detail);
  vsnprintf( s, 1000, detail, ap );
  va_end(ap);

  fprintf(jemFile, "%s", s);
}

void CJEMLinker::LinkClass( CJavaClass *j ) {
}

void CJEMLinker::ResolveClass( CJavaClass *j ) {
  char *newClassName;
  char buf[100];
  char *s;
  /* A class is linked by processing each method resolving class and method references. */

  if ( gOptions.debugLinker )
    StatusDebug("Linker: resolving class %s.", j->GetClassName() );

  for ( u2 m = 0; m < j->NumMethods(); m++ ) {
    int i = 0;
    while ( i < j->MethodLength( m ) ) {
      u4 offset = i;
      u1 opcode = j->GetByteCodeInc( m, &offset );
      u2 poolRef, classRef;
      char *className;
      bool foundClass = false;

      switch ( opcode ) {

      case GETSTATIC:
      case GETFIELD:
        poolRef = j->ByteCodePoolIndex( m, i+1 );
	      classRef = j->FieldClass( poolRef );
        newClassName = j->ConstantUtf8(j->ClassName(classRef));
	      foundClass = true;
	      break;

      case INVOKEINTERFACE:
        throw EError(kJVM_ERROR, 32 );
        break;

      case INVOKESTATIC:
      case INVOKEVIRTUAL:
      case INVOKESPECIAL:
	      poolRef = j->ByteCodePoolIndex( m, i+1 );
	      classRef = j->MethodClass( poolRef );
	      foundClass = true;
        newClassName = j->ConstantUtf8(j->ClassName(classRef));
	      break;

      case NEW:
        classRef = j->ByteCodePoolIndex( m, i+1 );
	      foundClass = true;
        newClassName = j->ConstantUtf8(j->ClassName(classRef));
        break;

      case LDC:
        poolRef = j->GetByteCode( m, i+1 );
        if ( !j->IsStringEntry( poolRef ) ) {
          j->PrintConstantPoolEntry(poolRef, buf, 100);
          throw EError( kJVM_ERROR, 29, j->GetClassName(), j->GetMethodName(m), buf  );
        }
        s = j->ConstantUtf8( j->StringUtf8(poolRef ));
        if (!StringExists(s, numStrings)) {
          if ( numStrings >= kMaxNumStrings )
            throw EError( kIDE_ERROR, 31, kMaxNumStrings );

          stringInfo[numStrings++].data = s;
        }
        foundClass = true;
        newClassName = "java/lang/String"; // Force the String class to be linked in.
        break;

      case LDC_W:
        poolRef = ((short)(((u2)j->GetByteCode( m, i+1 )<<8)|j->GetByteCode( m, i+2 )));
        if ( !j->IsStringEntry( poolRef ) ) {
          j->PrintConstantPoolEntry(poolRef, buf, 100);
          throw EError( kJVM_ERROR, 29, j->GetClassName(), j->GetMethodName(m), buf);
        }
        s = j->ConstantUtf8( j->StringUtf8(poolRef ));
        if (!StringExists(s, numStrings)) {
          if ( numStrings >= kMaxNumStrings )
            throw EError( kIDE_ERROR, 31, kMaxNumStrings );

          stringInfo[numStrings++].data = s;
        }
        foundClass = true;
        newClassName = "java/lang/String"; // Force the String class to be linked in.
        break;

      case LCONST_0:
      case LCONST_1:
      case DCONST_0:
      case DCONST_1:
      case I2L:
      case I2D:
      case LNEG:
      case LSHL:
      case LSHR:
      case LUSHR:
      case D2L:
      case D2I:
      case L2D:
      case DNEG:
      case LADD:
      case LSUB:
      case LMUL:
      case LDIV:
      case LREM:
      case LAND:
      case LOR:
      case LXOR:
      case LCMP:
      case DADD:
      case DSUB:
      case DMUL:
      case DDIV:
      case DREM:
      case DCMPL:
      case DCMPG:
        throw EError( kLINK_ERROR, 10, opcode );
        /*
        Callbacks are not supported.
        callbackLinked = true;
        foundClass = true;
        newClassName = "stamp/core/Callback"; // Force the Callback class to be linked in.
        */
        break;

	      // By default do nothing.
      }

      if ( foundClass ) {
	      // Force the referenced class to be loaded.
	      try {
	        (void) GetClass(newClassName);
	      }
	      catch (EError e) {
	        e.PrintError(false);
	        throw EError( kLINK_ERROR, 3, newClassName );
	      }
      }

      i = offset;
    }

    if ( gOptions.debugLinker )
      StatusDebug("Linker: checking exceptions for %s.", j->GetClassName() );

    int numExceptions = j->GetNumMethodExceptions( m );
    if ( numExceptions > 0 ) {

      for ( int e = 0; e < numExceptions; e++ ) {
        int startPC, endPC, handlerPC;
        char *catchType;

        j->GetExceptionInfo( m, e, &startPC, &endPC, &handlerPC, &catchType );

        if ( !catchType )
          continue;
          
        // Force the class to be loaded.
	      try {
	        (void) GetClassIndex(catchType);
	      }
	      catch (EError e) {
	        e.PrintError(false);
	        throw EError( kLINK_ERROR, 3, newClassName );
	      }
      }
    }
  }

}

CJavaClass *CJEMLinker::LoadClass( char *className ) {
  CJavaClass *j = new CJavaClass(className,classPath);

  if ( gOptions.debugLinker )
    StatusDebug("Linker: loading class %s.", className );

  j->LoadClass();

  if ( numClasses == kMaxNumClasses )
    throw EError( kLINK_ERROR, 1, kMaxNumClasses );

  classLinked[numClasses] = false;
  classOffset[numClasses] = 0;
  classList[numClasses++] = j;

  return j;
}

CJavaClass *CJEMLinker::GetClass( char *className ) {
  for ( int i = 0; i < numClasses; i++ ) 
    if ( !strcmp( classList[i]->GetClassName(), className ) )
      return classList[i];

  return LoadClass( className );
}

char *CJEMLinker::MakeOutputFileName( char *rootClassPath ) {
	return rootClassPath;  // This is wrong!
}

int CJEMLinker::GetClassIndex( char *className ) {
  CJavaClass *j = GetClass( className );

  for ( int i = 0; i < numClasses; i++ ) 
    if ( classList[i] == j )
      return i;

  throw EError( kLINK_ERROR, 3, className );
}

CJavaClass *CJEMLinker::FindClassByOffset( int anOffset ) {
  for ( int i = 0; i < numClasses; i++ )
    if ( classOffset[i] == anOffset )
      return classList[i];

  throw EError( kIDE_ERROR, 29 );
}

int CJEMLinker::FindMethodByOffset( CJavaClass **whichClass, int methodOffset ) {
	CJavaClass *currentClass;

	// Loop through each class checking the offsets.
  for ( int c = 0; c < numClasses; c++ ) {
    currentClass = classList[c];

    /* Loop for each method of the class. */
    for ( int method = 0; method < currentClass->GetNumLinkedMethods(); method++ ) {
   		if ( currentClass->GetMethodOffset( method ) == methodOffset ) {
       	*whichClass = currentClass->GetLinkedMethodClass(method);
        return currentClass->GetLinkedMethodIndex(method);
      }
    }
  }

  throw EError( kIDE_ERROR, 15);
}

char *CJEMLinker::TranslateLineNumber( int pc, int *lineNumber, int *jPC ) {
	// Loop through each class to determine the method the pc is in.
  // The methods are guaranteed to have offsets in increasing order so
  // this is relatively easy if we start from the back.

  for ( int c = numClasses-1; c >= 0; c-- ) {
  	CJavaClass *j = classList[c];

    for ( int m = j->NumMethods()-1; m >= 0; m-- ) {
     	if ( pc >= j->GetCodeOffset(m) ) {
        *jPC = pc - j->GetCodeOffset(m);

        // Compensate for static inits if necessary.
        *jPC -= StaticAdjustment(j, m);;

       	// Found the correct method now do the translation.
        *lineNumber = j->JemLine2JavaLine( m, pc );

        if ( gOptions.debugLineTranslate )
          StatusDebug("Method offset = %d, pc = %d, jPC = %d, line = %d", j->GetCodeOffset(m), pc, *jPC, *lineNumber );
        return j->GetSourcePath();
      }
    }
  }

  throw EError(kIDE_ERROR, 16, pc );
}

int CJEMLinker::NearestBreakLine( char *filePath, int lineNumber ) {
	// Find the class.
  for ( int c = 0; c < numClasses; c++ )
  	if ( !STRNCMPI(classList[c]->GetFilePath() , filePath, strlen(filePath)-4) ) {
    	CJavaClass *j = classList[c];

      return j->NearestBreakLine( lineNumber );
    }

  throw EError(kIDE_ERROR, 19, filePath );
}

int CJEMLinker::Java2JemLine( char *filePath, int lineNumber ) {
	// Find the class.
  for ( int c = 0; c < numClasses; c++ )
  	if ( !STRNCMPI(classList[c]->GetFilePath() , filePath, strlen(filePath)-4) ) {
    	CJavaClass *j = classList[c];
      int m;
      int line = j->Java2JemLine( lineNumber, &m );

      // Compensate for static inits if necessary.
      //if ( !strcmp( j->GetMethodName( m ), "main") )
      //  line += numStaticInits*3;

      return line;
    }

  throw EError(kIDE_ERROR, 19, filePath );
}

tStringInfo *CJEMLinker::FindString( CJavaClass *j, int aRef ) {
  char *s = j->ConstantUtf8( j->StringUtf8(aRef ));

  for ( int i = 0; i < numStrings; i++ )
    if ( !strcmp(stringInfo[i].data,s) )
      return &stringInfo[i];

  throw EError(kIDE_ERROR, 30 );
}

u1 CJEMLinker::Java2JemByte(u1 javaByte) {
  if ( gJava2JemOps[javaByte] == -1 )
    throw EError(kJVM_ERROR,30, javaByte);
  else {
    u1 j = gJava2JemOps[javaByte];

    if ( j > kJumpSplit )
      j = 2 * j - kJumpSplit;
    return j;
  }
}

u1 CJEMLinker::Jem2JavaByte(u1 jemByte) {
  if ( jemByte > kJumpSplit )
    jemByte = kJumpSplit + (jemByte-kJumpSplit)/2;
  return gJemOps[jemByte];
};
