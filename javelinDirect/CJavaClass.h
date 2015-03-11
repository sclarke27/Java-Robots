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

#ifndef _CJAVACLASS_H_
#define _CJAVACLASS_H_

// Wrapper class for Java .class files.
// Classes are loaded dynamically when they are needed. A
// class may be flushed from memory if it hasn't been used recently.

// This code has been written according to the specification of the
// class file format in:
//    The Java Virtual Machine Specification,
//    Tim Lindholm and Frank Yellin, Addison Wesley, 1997.

// The class file is loaded using element by element to ensure
// there are no portability problems with structure padding.

// Currently the class only supports class loading. It would
// be good to implement a save routine for use with persistant
// queries. God forbid I ever implement a Java compiler it would
// be useful too.

// Note that Utf8 strings are not properly handled. It is assumed
// that all characters are single byte.

#include "CObject.h"

#include "Java.h"
#include "Utils.h"
#include <stdio.h>

// The version numbers that this class supports.
const u2 CONSTANT_Class_Major = 45;
const u2 CONSTANT_Class_Minor = 3;

class CJEMLinker;

class CJavaClass : public CObject {

  // A constant pool entry.
  struct tPoolEntry {
    u1 tag; // Tag indicating the kind of entry.
    union {
      struct {
	      u4 integer_value; // Value of integer for CONSTANT_Integer.
      } Integer;

      struct {
	      float float_value; // Value of float for CONSTANT_Float.
      } Float;

      struct {
        u2 length; // Length of the string.
        char *bytes; // Actual string data. (Null terminated C string.
      } Utf_info;

      struct {
        u2 class_index; // Index into constant pool of the class.
        u2 name_and_type_index; // Index of name and type info.
      } Methodref_info;

      struct {
        u2 class_index; // Index into constant pool of the class.
        u2 name_and_type_index; // Index of name and type info.
      } InterfaceMethodref_info;

      struct {
	      u2 class_index; // Index in the constant pool for the class.
	      u2 name_and_type_index; // Index of the name and type of the field.
      } Fieldref_info;

      struct {
        u2 name_index; // Index of the class name.
      } Class_info;

      struct {
        u2 string_index; // Index of the string's UTF_info.
      } String_info;

      struct {
        u4 high_bytes;
	      u4 low_bytes;
      } Long_info;

      struct {
        u4 high_bytes;
	      u4 low_bytes;
      } Double_info;

      struct {
        u2 name_index; // Index of the name.
        u2 descriptor_index; // Index of the type descriptor.
      } NameAndType_info;

    } info;  // Action pool item info. Depends upon the tag.
  };

  struct field_info {
    u2 access_flags;
    u2 name_index;
    u2 descriptor_index;
    u2 attributes_count;

    // Currently only one attribute is handled so it is inline in this structure.
    // ConstantValue
    u2 constantvalue_index; 

    u2 fieldIndex;
  };

  struct Exception_table {
    u2 start_pc;
    u2 end_pc;
    u2 handler_pc;
    u2 catch_type;
  };

  struct Local_variable_table {
  	u2 start_pc;
    u2 length;
    u2 name_index;
    u2 descriptor_index;
    u2 index;
  };

  struct Line_number_table {
  	u2 start_pc;
    u2 line_number;
  };

  struct Code_attribute {
    u2 max_stack;
    u2 max_locals; // The number of words of local variables including parameters.
    u4 code_length;
    u1 *code;
    u2 exception_table_length;
    Exception_table *exception_table;
    u2 local_variable_table_length;
    Local_variable_table *local_variable_table;
    u2 line_number_table_length;
    Line_number_table *line_number_table;
  };

  struct Exceptions_attribute {
    u2 number_of_exceptions;
    u2 *exception_index_table;
  };

  struct method_info {
    u2 access_flags;
    u2 name_index;
    u2 descriptor_index;
    u2 attributes_count;

    Code_attribute code_attribute;
    Exceptions_attribute exceptions_attribute;

    u4 methodOffset; // Used by the linker.
    u4 codeOffset;
  };

  struct linked_method_info {
    char *className;
    char *methodName;
    char *methodType;
    u2 methodOffset; // Offset to the method header in the jem file. Relative to the
                     // start of the jem file.
    u2 classMethodOffset; // Offset to the method header in the jem file. Relative
                          // to the class header.
    bool isVirtual; // Whether the method is virtual.
    CJavaClass *methodClass;
    u2 methodIndex;
  };

  struct linked_field_info {
    char *className;
    char *fieldName;
    char *fieldType;
    u2 fieldIndex; // Index into the array of fields in the class.
    u2 fieldOffset; // Offset of the field in the object.
    u2 fieldWidth; // The number of words in the field (1 or 2).
  };

  bool isLoaded; // Whether the class has been loaded into memory.
  FILE *classFile; // File from which to load the class.
  char *className; // Name of the file.
  char *javaClassName; // Class name in Java . format.
  char *classPath;
  char *filePath;
  char *sourcePath;

  tPoolEntry *constantPool; // The constant pool.
  u2 constantPoolCount; // Number of entries in the constant pool.

  u2 access_flags; // Flags for access to this class.
  u2 this_class; // Index into constant pool for this class ref.
  u2 super_class; // Index into constant pool for super class ref.	

  u2 interfacesCount; // Number of interfaces.
  u2 *interfaces; // Index into the constant pool for each index.
  
  u2 fieldsCount; // Number of fields.
  field_info *fields; // List of fields.

  u2 methodsCount; // Number of methods.
  method_info *methods; // List of methods.

  int sOldIndex, sOldMethod, sCount; // Static counters used for disassembly.

public:
  
  // List of methods once static linking is done.
  // This list will contain all virtual methods from the super class.
  linked_method_info *linkedMethods;
  int numLinkedMethods;
  linked_field_info *linkedFields;
  int numLinkedFields;
  bool linked;

  CJavaClass( char *aClassName, char *aClassPath );
    // Specify the file to use for this class. The path needs to be
    // resolved by the caller.
  virtual ~CJavaClass();

  void LoadClass();
    // Load the class from disk so that it can be used. This function is 
    // called automatically if the class needs loading.
  void FlushClass();
    // Flush the in memory structures. The class will need to be loaded
    // again before it can be used.

  bool CheckVersion( u2 majorVersion, u2 minorVersion );
    // Check whether we can handle the class file version.

  void PrintConstantPoolEntry( u2 index, char *s, int length );
    // Print a constant pool entry.

  char *GetClassPath() { return className; };
    // Get the path and name of this class;
  char *GetJavaClassName() { return javaClassName; };
    // Get the path and name of this class;

  char *GetFilePath() { return filePath; };
  	// Get the full path name of the file.
  char *GetSourcePath();
    // Get the path name of the source file.

  char *GetClassName() { return ConstantUtf8(ClassName(this_class)); };
    // Return the name of this class.
  char *GetSuperClassName() { return ConstantUtf8(ClassName(super_class)); };
    // Return the name of super class.

  // Access methods:
  char *ConstantUtf8( u2 index ) { return constantPool[index].info.Utf_info.bytes; };
    // Get a Utf8 as a C string.
  u2 ClassName( u2 index ) { return constantPool[index].info.Class_info.name_index; };
  u2 NTName( u2 index ) { return constantPool[index].info.NameAndType_info.name_index; };
  u2 NTType( u2 index ) { return constantPool[index].info.NameAndType_info.descriptor_index; };
  u2 FieldClass( u2 index ) { return constantPool[index].info.Fieldref_info.class_index; };
  u2 FieldName( u2 index ) { return NTName(constantPool[index].info.Fieldref_info.name_and_type_index); };
  u2 FieldType( u2 index ) { return NTType(constantPool[index].info.Fieldref_info.name_and_type_index); };
  u2 MethodClass( u2 index ) { return constantPool[index].info.Methodref_info.class_index; };
  u2 MethodName( u2 index ) { return NTName(constantPool[index].info.Methodref_info.name_and_type_index); };
  u2 MethodType( u2 index ) { return NTType(constantPool[index].info.Methodref_info.name_and_type_index); };
  char *GetMethodType( u2 index ) { return ConstantUtf8(methods[index].descriptor_index); };
  char *GetFieldType( u2 index ) { return ConstantUtf8(fields[index].descriptor_index); };
  u2 GetFieldWidth( u2 index ) { return (*GetFieldType(index)=='J' || *GetFieldType(index)=='D')?2:1; };
  u2 InterfaceMethodClass( u2 index ) { return constantPool[index].info.InterfaceMethodref_info.class_index; };
  u2 InterfaceMethodName( u2 index ) { return NTName(constantPool[index].info.InterfaceMethodref_info.name_and_type_index); };
  u2 StringUtf8( u2 index ) { return constantPool[index].info.String_info.string_index; };
  u2 ConstantInt( u2 index ) { return constantPool[index].info.Integer.integer_value; };
  u2 IsStringEntry( u2 index ) { return constantPool[index].tag == CONSTANT_String; };
  u2 IsIntegerEntry( u2 index ) { return constantPool[index].tag == CONSTANT_Integer; };

  bool DisassembleByte( u2 method_index, u4 index, char *s, int length );
    // Disassemble the byte at the index in the method. The disassembly is placed in the string
    // s up to a maximum length. Returns true if the byte is an opcode (rather than an operand).

  void DisassembleMethod( u2 method_index );
    // Disassemble a method.
  short NumMethods() { return methodsCount; };
    // Get the number ofv methods in the class.
  short NumVirtualMethods();
    // Get the number of virtual methods in the class.
  short NumNonVirtualMethods();
    // Get the number of non-virtual methods in the class.
  u2 GetMethodByName( char *name, char *type );
    // Get the index of a method given its name.
  char *GetMethodName( u2 method_index ) { return ConstantUtf8(methods[method_index].name_index); };
    // Get the index of a method given its name.
  u4 MethodLength( u2 method_index ) { return methods[method_index].code_attribute.code_length; };
    // Get the length (in bytes) of a method.
  u1 GetByteCode( u2 method_index, u4 offset ) { return methods[method_index].code_attribute.code[offset]; };
    // Get the byte code at the offset from the start of a given method.
  void SetByteCode( u2 method_index, u4 offset, u1 value );
    // Change a byte code.
  u1 GetByteCodeInc( u2 method_index, u4 *offset );
    // Return the byte code at the given offset and then increment the offset to point to the next
    // valid byte code.
  int CJavaClass::BytecodeLength(u2 method_index, u4 offset);
    // Get the number of bytes taken by a bytecode.
  u2 ByteCodePoolIndex( u2 method_index, u4 offset ) { return ((short)((GetByteCode(method_index,offset)<<8)|GetByteCode(method_index, offset+1))); };
    // Get the constant pool reference at the given offset.

  short NumFields() { return fieldsCount; };
    // Return the total number of fields.
  short NumNonStaticFields();
    // Get the number of non-static, non-final fields in the class.
  short NumStaticFields();
    // Get the number of static, non-final fields in the class.
  u2 GetFieldIndex( int field_index ) { return fields[field_index].fieldIndex; };
    // Return the field index (relative to other static or non-static fields.
  void SetFieldIndex( int field_index, u2 index ) { fields[field_index].fieldIndex = index; };
    // Set the field index.
  bool FieldIsStatic( int field_index ) { return fields[field_index].access_flags & ACC_STATIC; };
    // Return TRUE if the given field is static.
  bool FieldIsFinal( int field_index );
    // Return TRUE if the given field is final.
  u2 GetFieldByName( char *name );
    // Get the index of a field given its name.
  char *GetFieldName( u2 field_index ) { return ConstantUtf8(fields[field_index].name_index); };

  int GetParamsSize( u2 method_index );
    // Get the size in words of the parameters to the given method. 
  int GetLocalsSize( u2 method_index ) { return methods[method_index].code_attribute.max_locals; };
    // Get the size in words of the locals for the given method. 
  u4 GetCodeLength( u2 method_index ) { return methods[method_index].code_attribute.code_length; };
    // Get the length of a method in bytes.
  u4 GetMethodOffset( u2 method_index ) { return linkedMethods[method_index].methodOffset; };
    // Get the offset for a method.
  void SetMethodOffset( u2 method_index, u4 offset ) { linkedMethods[method_index].methodOffset = offset; };
    // Set the offset for a method.
  void SetClassMethodOffset( u2 method_index, u4 offset ) { linkedMethods[method_index].classMethodOffset = offset; };
    // Set the offset for a method relative to the class.
  u4 GetCodeOffset( u2 method_index ) { return methods[method_index].codeOffset; };
    // Get the offset for a method.
  void SetCodeOffset( u2 method_index, u4 offset ) { methods[method_index].codeOffset = offset; };
    // Set the offset for a method.
  bool MethodIsNative( u2 method_index ) { return (methods[method_index].access_flags & ACC_NATIVE) !=0; };
    // Test if a method is native.
  bool MethodIsFinal( u2 method_index ) { return (methods[method_index].access_flags & ACC_FINAL) !=0; };
    // Test if a method is final.
  bool MethodIsStatic( u2 method_index ) { return (methods[method_index].access_flags & ACC_STATIC) !=0; };
    // Test if a method is static.
  int GetNumMethodExceptions( u2 method_index ) { return methods[method_index].code_attribute.exception_table_length; }
    // Get the number of exception handlers for a method.
  void GetExceptionInfo( u2 method_index, int exceptionNumber, int *startPC, int *endPC, int *handlerPC, char **catchType );
    // Get information about an exception handler.

  char *GetLocalVariableName( u2 method_index, u2 local_index, int pc );
    // Get the name of a local variable. Depends upon the program counter
    // since the same local variable name might be used multiple times in
    // a method with different types.
  char *GetLocalVariableName( u2 method_index, u2 local_index );
    // Get the name of a local with regard to the PC.
  char *GetLocalVariableType( u2 method_index, u2 local_index, int pc );
    // Get the type of a local variable.
  u2 GetLocalVariableIndex( u2 method_index, u2 local_index, int pc );
    // Get the index of a local variable.
  bool LocalVariableInScope( u2 method_index, u2 local_index, int pc );
  	// Check if a local variable is in scope.

  static void FormatType( char *s, char *type );
    // Pretty print a type descriptor in the string s.

  int JemLine2JavaLine( u2 method_index, int pc );
  	// Translate a program counter value to a java line number. The caller
    // must have already determined the correct method number.
  int NearestBreakLine( int lineNumber );
    // Find the nearest (>=) line number that can support a breakpoint.
  int Java2JemLine( int lineNumber, int *methodNum );
    // Convert a line number in a .java file to a jem line number. Uses the same
    // logic as NearestBreakLine to determine the line.

  void StaticLink(CJEMLinker *linker);
    // Statically link all methods and fields.
  u2 GetLinkedFieldIndex( char *className, char *fieldName, char *fieldType );
    // Return the statically linked field index.
  u2 GetLinkedFieldOffset( char *className, char *fieldName, char *fieldType );
    // Return the offset of the field in the object.
  u2 GetNumLinkedFields() { return numLinkedFields; };
    // Return the number of statically linked fields.
  u2 GetNumLinkedMethods() { return numLinkedMethods; };
    // Return the number of statically linked methods.
  CJavaClass *GetLinkedMethodClass(u2 index) { return linkedMethods[index].methodClass; };
    // Get the class of a linked method.
  u2 GetLinkedMethodIndex(u2 index) { return linkedMethods[index].methodIndex; };
    // Get the index into the methods[] array of a linked method.
  u2 GetLinkedMethodOffsetByName(char *methodName, char *methodType);
    // Get the offset of a linked method by name.
  u2 GetLinkedClassMethodOffsetByName(char *methodName, char *methodType);
    // Get the offset of a linked method by name.
  u2 GetLinkedMethodIndexByName( char *methodName, char *methodType );
    // Get the index of a linked method by name.
  bool LinkedMethodIsStatic( char *methodName, char *methodType ) { return GetLinkedMethodClass(GetLinkedMethodIndexByName(methodName, methodType))->MethodIsStatic(GetLinkedMethodIndex(GetLinkedMethodIndexByName(methodName, methodType))); };

  void AdjustLineNumbers( int method_index, int adjustment );
    // Adjust line numbers to compenstate for instructions added to method.
private:

  // Class loading routines.
  void LoadHeader();
    // Load the header information and check the version.
  void LoadConstantPool();
    // Load the constant pool structures.
  void LoadClassInfo();
    // Load info about the class.
  void LoadInterfaces();
    // Load the interfaces.
  void LoadFields();
    // Load the field definitions.
  void LoadMethods();
    // Load the methods.
  void LoadAttributes();
    // Load the attributes.

  void FlushConstantPool();
    // Dispose of the constant pool.

  // Extra utility routines.
  void ReadBytes( char *bytes, u4 length );
    // Read `length' bytes from the file.
  u1 ReadU1();
    // Load an unsigned char.
  u2 ReadU2();
    // Load an unsigned short.
  u4 ReadU4();
    // Load an unsigned long.

  u2 PrintOpcode( u2 method, u4 index );
    // Output one instruction. Returns the number of bytes in the instruction.

  static int LNTCompare(const void *a, const void *b);
};

// Defines to simplify the code.
#define METHOD_LENGTH(m) (methods[(m)].code_attribute.code_length)
#define OPCODE(m,a) (methods[(m)].code_attribute.code[(a)])

#endif
