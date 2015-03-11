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
 
#ifndef _CJEMLinker_H_
#define _CJEMLinker_H_

/* The link works in two passes. In the first, `link', pass all referenced methods and classes
   are loaded so they can be statically numbered. In the second, `output' pass the code 
   is output to the .jem file with all dynamic attributes statically linked. It is in the
   `output' pass that any conversions on java opcodes are performed. Consistency and
   specification compliance checking is done in the `link' pass. 
   */

/*
  .jem File Format: (For version 0.1)

 +---------------+
 | Flag          | Run/Debug flag.
 +---------------+
 +---------------+
 | Length        | Length of the file in 256 byte multiples.
 +---------------+
 +---------------+
 | Version       | Version stored as BCD major.minor version #.
 +---------------+
 +---------------+
 | NumStatic     | Space required for static variables (measured in bytes).
 +---------------+
 | MainOffsetMSB | Offset to the code for the `main' method.
 | MainOffsetLSB |
 +---------------+
 |ObjectOffsetMSB| Offset to the java.lang.Object class for instantiating arrays.
 |ObjectOffsetLSB|
 +---------------+
 |StringOffsetMSB| Offset to the java.lang.String class for instantiating strings.
 |StringOffsetLSB|
 +---------------+
 | ThrowOffsetMSB| Offset to the java.lang.Throwable.throwVMException() method
 | ThrowOffsetLSB| for throwing exceptions.
 +---------------+
 | STOffsetMSB   | Offset to the string table.
 | STOffsetLSB   |
 +---------------+
 | CBOffsetMSB   | Offset to the callback method.
 | CBOffsetLSB   |
 +---------------+
 | OOMOffsetMSB  | Offset to the java.lang.OutOfMemoryError class for JVM errors.
 | OOMOffsetLSB  |
 +---------------+
 | StackBaseMSB  | Base of the stack in memory.
 | StackBaseLSB  |
 +---------------+
 | ArrayClassMSB | Offset to the dummy array class.
 | ArrayClassLSB |
 +---------------+
 +---------------+ First Class:
 | SuperOffsetMSB| Offset of the superclass from the start of the file.
 | SuperOffsetLSB|
 +---------------+
 | NumFields     | Number of fields in objects of the class.
 +-+-------------+
 | NumParams     | Number of method parameters 
 +-+-------------+
 | Meth1ETabMSB  | Offset to the exception table for this method.     
 | Meth1ETabLSB  | Offsets measured from start of file.
 +---------------+
 | NumLocals     | Number of local variables (excluding parameters)
 +---------------+
 | Flags         | Native method flag.
 +---------------+
 | Meth1OffsetMSB| Offset for the first method of this class (and superclass)
 | Meth1OffsetLSB| Offsets measured from start of file.
 +---------------+
 |E|NumParams    | Number of method parameters
 +---------------+
 | Meth1ETabMSB  | Offset to the exception table for this method.
 | Meth1ETabLSB  | Offsets measured from start of file.
 +---------------+
 | NumLocals     | Number of local variables (excluding parameters)
 +---------------+
 | Flags         | Native method flag.
 +---------------+
 | Meth2OffsetMSB| Second method
 | Meth2OffsetLSB| 
 +---------------+
 | ...           |
 +---------------+
 +---------------+ Second Class:
 | SuperOffsetMSB| Offset of the superclass from the start of the file.
 | SuperOffsetLSB|
 +---------------+
 | NumFields     |
 +---------------+
 |E|NumParams    |
 +---------------+
 | NumLocals     | 
 +---------------+
 | Method1 offset|
 | ...           |

 | code ...      |

 +---------------+ Method exception table.
 | NumHandlers   |
 +---------------+
 | fromMSB       | PC to catch from.
 | fromLSB       |
 +---------------+
 | toMSB         | PC to catch to.
 | toLSB         |
 +---------------+
 | handlerMSB    | Start of the catch handler.
 | handlerLSB    |
 +---------------+
 | TypeMSB       | Offset of the class to catch.
 | TypeLSB       |
 +---------------+
 |...            |

 +---------------+ String offsets table.
 |Offset0MSB     | Offset to string 0, measured from the start of the file.
 |Offset0LSB     |
 +---------------+
 |Offset1MSB     | Offset to string 1, measured from the start of the file.
 |Offset1LSB     |
 +---------------+
 |...            |

 +---------------+ String data.
 |...            |

 If the high bit of the high byte of the method offset is set then the method is native. The low byte
 is then an offset into the native method dispatch table. Thus there can be only 256 native methods.

 */

#include "CObject.h"
#include "CJavaClass.h"
#include "Errors.h"
#include "Java.h"

const int kMaxNumClasses = 255; // Maximum number of classes allowed in .jem file.
const int kHeaderSize = 20; // Size of the .jem file header.

// Major and minor version numbers for .jem files. A JVM is be able to accept
// any .jem file with the same major version number. Minor version numbers indicate
// backwards compatible improvements that improved JVMs can take advantage of.
const int kJemMajorVersion = 0;
const int kJemMinorVersion = 1;

const unsigned char kNativeBit = (1<<7);
const int kMaxNumStrings = 128;
const int kMaxNumStaticFields = 128;
const int kLengthOffset = 1;

struct tStringInfo {
  int offset;
  char *data;
  CJavaClass *whichClass;
  int poolRef;
  int index;
};

class CJEMLinker : public CObject {

public:

  CJavaClass *rootClass;
  char *classPath;

  int numClasses;   	// Number of classes in classList.
  CJavaClass *classList[kMaxNumClasses];	// List of loaded classes.
  bool classLinked[kMaxNumClasses];	// A flag for each class indicating whether the link pass has been performed yet.
  int classOffset[kMaxNumClasses]; // Offset to the start of the class in the .jem file.

  int classSize[kMaxNumClasses]; // Size of the linked class (exlcuding strings).
  int classStringsSize[kMaxNumClasses]; // Size of the strings in the class.

  typedef struct {
    int fieldNum;
    CJavaClass *fieldClass;
  } tStaticFieldInfo;

  tStaticFieldInfo staticFields[kMaxNumStaticFields];
  int numStaticFields;	// Total number of static fields in all the linked classes.
  int numStaticInits; // Total number of static initialisers.
  int staticInit[kMaxNumClasses]; // Array of static initialiser offsets.

  FILE *jemFile;

  char *binary; // The .jem file binary.
  int binaryLength; // The length of the binary.
  int binaryPointer;

  tStringInfo *stringInfo;
  int numStrings;
  int stringTableOffset;

  bool callbackLinked;

  int stackBase;
  int programLength;
  int stringsLength;
  int staticLength;

  CJEMLinker();
  virtual ~CJEMLinker();

  int GetNumClasses() { return numClasses; };
    // Get the number of linked classes.
  CJavaClass *GetClass( int index ) { return classList[index]; };
    // Get a class based on its index.
  int GetClassSize(int index) { return classSize[index]; };
    // Get the size of a class.
  int GetClassStringsSize(int index) { return classStringsSize[index]; };
    // Get the size of the strings in a class.

  void LinkProgram( char *rootFile, char *classPath );
    // Link a program into a .jem file based on the root class.
    // rootFile is the file name only of the class containing the `main'
    // method. classPath is the class path. It must include the path of
    // the rootFile. classPath can be a semicolon delimited list of paths.

  int FindMethodByOffset( CJavaClass **whichClass, int methodOffset );
  	// Find a method and class based on the .jem file offset.
  CJavaClass *FindClassByOffset( int anOffset );
    // Find a class by offset.

  int NearestBreakLine( char *filePath, int lineNumber );
    // Given a file and a line in that file find the nearest (>=) line number
    // that can support a breakpoint.
  int Java2JemLine( char *filePath, int lineNumber );
    // Convert a line number in a .java file to a jem line number. Uses the same
    // logic as NearestBreakLine to determine the line.

  char *TranslateLineNumber( int pc, int *lineNumber, int *jPC );
  	// Translate a program counter to a line number and return the file it is in.

  CJavaClass *GetClass( char *className );
    // Get a class from the class list or load from disk if 
    // not already in the list.

  char *GetBinary() { return binary; };
  int GetBinaryLength() { return binaryLength; };
    // Get the .jem file and length.

  int GetStackBase() { return stackBase; };
    // Get the base of the stack in memory.
  int GetProgramLength() { return programLength; };
    // Get the length of the program, excluding strings.
  int GetStringsLength() { return stringsLength; };
    // Get the length of the strings.
  int GetStaticLength() { return staticLength; };
    // Get the length of the static variables.

  u1 Java2JemByte(u1 javaByte);
  	// Convert a java bytecode to a jem bytecode.
  u1 Jem2JavaByte(u1 jemByte);
  	// Convert a jem bytecode to a java bytecode.

private:

  void LinkPass();
    // Perform the link pass.
  void OutputPass();
    // Perform the output pass.

  void ComputeOffsets();
    // Compute the offset to the start of each method.

  void OutputByte( u1 byte, char *detail, ... );
    // Output a byte (or the detail string) to the .jem file.
  void OutputComment( char *detail, ... );
    // Output a comment to the .jem file.
 	void OutputJavaDispatchTable();
   	// Output the .jem opcodes and dispatch table.
  void OutputNativeDispatchTable();
    // Generate the dispatch table for native methods.
  int NativeMethodOffset( char *className, char *methodName );
    // Find the index of a native method.
  
  void ResolveClass( CJavaClass *j );
    // Resolve and load all classes referenced by a class.
  void LinkClass( CJavaClass *j );
    // Perform the link pass on the given class.

  CJavaClass *LoadClass( char *className );
    // Load a class from disk and place in classList.
  int GetClassIndex( char *className );
    // Return the index of the class in the class list.

  int GetClassOffset( CJavaClass *j );
    // Get the offset of a class.
  tStringInfo *FindString( CJavaClass *j, int aRef );
    // Find a string based on its class and constant pool reference.
  bool StringExists(char *s, int stringCount);
    // Returns true is the string is already referenced.
    
  char *MakeOutputFileName( char *rootClassPath );
    // Based upon the root class make the name of the output file.

  int StaticAdjustment(CJavaClass *j, int methodIndex );
    // Adjust an offset to compensate for static initialisers.
};

extern CJEMLinker *gLinker;


#endif
