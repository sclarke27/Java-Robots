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

#include "Errors.h"

#include <stdarg.h>
#include <string.h>

tErrorDefs gErrorDefs[] = 
{ {kSYS_ERROR, 30, "Out of memory.", "There was not enough memory to complete the requested action.", "Buy a better computer."},
  
  {kJVM_ERROR, 1, "Too many methods in class.", "The class already contains the maximum number of methods.", "Increase the number of methods a class may have."}, 
  {kJVM_ERROR, 2, "Stack overflow.", "The stack is too small and has overflowed.", "Increase the size of the virtual machine stack."}, 
  {kJVM_ERROR, 3, "Stack underflow.", "Attempt to pop from an empty stack.", "This indicates a bug in the linker."},
  {kJVM_ERROR, 4, "Undefined instruction.", "Attempt to execute an unknown instruction.", "This indicates a bug in the linker."},
  {kJVM_ERROR, 5, "No `return' instruction found.", "A program must end with a `return' instruction but none was found.", "This indicates a bug in the linker."},
  {kJVM_ERROR, 6, "Divide by zero.", "An attempt was made to divde by zero.", "Check the statement."},
  {kJVM_ERROR, 7, "Constant pool index out of range.", "An index into the constant pool is incorrect.", "This indicates a bug in the linker."},
  {kJVM_ERROR, 8, "Unable to resolve function name.","The name of a function could not be resolved.", "Check the statement."},
  {kJVM_ERROR, 9, "Constant pool full.", "The constant pool is full.", "Increase the size of the constant pool or simplify the statement."}, 
  {kJVM_ERROR, 10, "Too many arguments to function.", "Too many arguments were given in a function call.", "Correct the statement."}, 
  {kJVM_ERROR, 11, "Unable to resolve local variable name.","The name of a local variable could not be resolved.", "Check the statement."}, 
  {kJVM_ERROR, 12, "Program buffer overflow.", "Program code buffer is too small and has overflowed.", "This indicates an internal error."}, 
  {kJVM_ERROR, 13, "No `Code' attribute for method.", "The code for a method could not be located.", "This indicates an internal error."}, 
  {kJVM_ERROR, 14, "No method found for `%s'.", "A method could not be found by name.", "This indicates an internal error."}, 
  {kJVM_ERROR, 15, "Method arguments corrupt.", "The arguments to a method have become corrupt.", "This indicates an internal error."}, 
  {kJVM_ERROR, 16, "Reference to `this' is null in function call.", "", "This indicates an internal error."}, 
  {kJVM_ERROR, 17, "Unable to load class file: `%s', '%s'.", "The file does not exist or you do not have permission to read it.", "Check that the file exists and you have the correct permissions."}, 
  {kJVM_ERROR, 18, "File does not contain a Java class: `%s'.", "The file is not a Java class file or it is corrupt.", "Check the path is correct and replace the file if necessary."}, 
  {kJVM_ERROR, 19, "Cannot read Java class file: `%s'. Version not supported.", "The version of the class file is either too new or too old.", "Try recompiling the class with a different compiler version."}, 
  {kJVM_ERROR, 20, "Java class file corrupt: `%s'.", "There is a corruption in the class file or a bug in the class loader.", "Report the error to technical support."}, 
  {kJVM_ERROR, 21, "Unknown tag (%d) in java class file: `%s'.", "There is a corruption in the class file or a bug in the class loader.", "Report the error to technical support."}, 
  {kJVM_ERROR, 22, "Unknown parameter (`%c') in definition of method `%s'.", "There is a corruption in the class file or a bug in the class loader.", "Try recompiling the class, or report the error to technical support."},
  {kJVM_ERROR, 23, "Code index out of range.", "There is a corruption in the class file or a bug in the class loader.", "Try recompiling the class, or report the error to technical support."},
  {kJVM_ERROR, 24, "No debug info in class file `%s'.", "The debug information could not be found. Either the class was not compiled with debugging information or an internal error has occured.", "Compile the class with the `-g' flag."},
  {kJVM_ERROR, 25, "Unknown parameter (`%c').", "There is a corruption in the class file or a bug in the class loader.", "Report the error to technical support."},
  {kJVM_ERROR, 26, "No line number info in class file `%s'.", "The line number information could not be found. Either the class was not compiled with debugging information or a compiler generated method is being executed.", "Compile the class with the `-g' flag, or use 'run' rather than 'step'."},
  {kJVM_ERROR, 27, "Invalid local variable index.", "An invalid local variable index was used.", "Report the error to technical support."},
  {kJVM_ERROR, 28, "Cannot find linked field (%s.%s).", "The named field could not be found.", "Report the error to technical support."},
  {kJVM_ERROR, 29, "Method %s::%s contains a constant out of range (%s).", "The class contains a constant which is out of the range allowed in a JEM file.", "Restrict variables to those allowed in JEM files."},
  {kJVM_ERROR, 30, "Unimplemented bytecode (%d).", "An unimplemented bytecode was encountered.", "Check that your source file complies with the JavaCard specification."},
  {kJVM_ERROR, 31, "Cannot find linked method (%s.%s).", "The named method could not be found.", "Report the error to technical support."},
  {kJVM_ERROR, 32, "Interfaces are not currently supported.", "Interfaces are not support in this version.", "You tried to load or link a class that uses interfaces. Report that you want to use interfaces to technical support."},

  {kLINK_ERROR, 1, "Too many referenced classes.", "Only %d classes can be referenced in a single program.", "Reference fewer classes."},
  {kLINK_ERROR, 2, "No suitable 'main' method found.", "A class must contain a method with the prototype: `public static void main()'.", "Add a suitable `main' method."},
  {kLINK_ERROR, 3, "Unable to load class `%s'.", "The class file could not be found or loaded.", "Check that the .class file exists."},
  {kLINK_ERROR, 4, "Unknown native method `%s.%s'.", "The named native method was not listed in the native method table.", "New native methods cannot be defined."},
  {kLINK_ERROR, 5, "Linking error.", "There is a bug in the IDE.", "Report the error to technical support."},
  {kLINK_ERROR, 6, "Too many 'main' methods.", "There is more than one method called 'main'.", "A program should contain only one method called 'main'."},
  {kLINK_ERROR, 7, "Too many static fields.", "There is a limit of 127 static fields per jem file.", "Modify you program so it uses fewer static fields."},
  {kLINK_ERROR, 8, "Maximum binary size exceeded.", "Your program is larger than the Javelin's EEPROM can contain.", "Reduce the size of your program."},
  {kLINK_ERROR, 9, "Maximum number of methods per class exceeded in class %s.", "Your class has too many methods.", "Reduce the number of methods in the class."},
  {kLINK_ERROR, 10, "Unsupported float or long operation.", "Javelin does not support floating point or long types.", "Remove the floating point or long variables."},

  {kIDE_ERROR, 1, "Internal IDE error (%d).", "There is a bug in the IDE.", "Report the error to technical support."},
  {kIDE_ERROR, 2, "Unable to open file `%s'.", "The file could not be opened.", "Check that the file exists and is not already open by another program."},
  {kIDE_ERROR, 3, "System error when %s: %s.", "A system error occured.", "Try to rectify the error and repeat the operation."},
  {kIDE_ERROR, 4, "Attempt to save an un-named file.", "The program attempted to save a file that doesn't have a name.", "Report the error to technical support."},
  {kIDE_ERROR, 5, "A file must be saved before it can be compiled.", "The current file has not been saved yet.", "Save the file before compiling."},
  {kIDE_ERROR, 6, "Could not open the serial port: %s.", "The serial port could not be opened.", "Make sure another program is not already using the serial port."},
  {kIDE_ERROR, 7, "Error writing to the serial port (%d).", "The serial port could not be written to.", "Make sure the serial cable is connected and that another program is not using the same serial port."},
  {kIDE_ERROR, 8, "Error reading from the serial port (%d).", "The serial port could not be read from.", "Make sure the serial cable is connected."},
  {kIDE_ERROR, 9, "Error communicating over the serial port (checksum failed).", "The receieved data was corrupted.", "Make sure the serial cable is connected securely."},
  {kIDE_ERROR, 10, "Error communicating over the serial port (corrupt packet $%2.2X).", "The receieved data was corrupted.", "Make sure the serial cable is connected securely."},
  {kIDE_ERROR, 11, "Buffer overrun.", "The read buffer was not large enough.", "Report the error to technical support."},
  {kIDE_ERROR, 12, "Could not find compiler.", "The compiler executable was not in the same directory as the IDE.", "Make sure the compiler exists. Reinstall the IDE if necessary."},
  {kIDE_ERROR, 13, "Not enough memory to run the compiler.", "There was not enough free memory to run the compiler.", "Try closing other programs or restarting the computer to free up memory."},
  {kIDE_ERROR, 14, "Cannot run compiler (%d, '%s').", "There was an error running the external compiler.", "Report the error to technical support."},
  {kIDE_ERROR, 15, "Stack trace is corrupt.", "The stack trace cannot be reconciled with the .jem file.", "Check that the .jem file is up to date and the SX is running the same file."},
  {kIDE_ERROR, 16, "Unable to do line number translation for pc = %d.", "The translation from the program counter to a line number could not be performed.", "The .jem file on the device is probably not current with the one in memory. Try downloading again."},
  {kIDE_ERROR, 17, "Unable to open file for writing `%s' (%d, %s).", "The specified file could not be opened for writing.", "Check that the file is not open in another application."},
  {kIDE_ERROR, 18, "Type not implemented in debugger (%s).", "The debugger tried to decode a variable of a type that is not implemented.", "Report the error to technical support."},
  {kIDE_ERROR, 19, "Unable to find class (%s).", "The linker could not locate the specified class.", "Report the error to technical support."},
  {kIDE_ERROR, 20, "Unable to find a breakable line.", "The linker could not locate a line (greater or equal) that a breakpoint can be placed on.", "Try selecting another line."},
  {kIDE_ERROR, 21, "Unable to decode compiler message.", "An error message from the compiler could not be decoded.", "Report the error to technical support."},
  {kIDE_ERROR, 22, "Error reading from the serial port (timeout).", "The serial port could not be read from.", "Make sure the serial cable is connected."},
  {kIDE_ERROR, 23, "Unknown message from the JVM.", "A message was received from the JVM with could not be interpreted.", "Make sure the JVM version and IDE version are compatible."},
  {kIDE_ERROR, 24, "Unknown bytecode in the JEM file (%s).", "An unknown bytecode occurred in the JVM.", "Ensure that your program does not contain operations which are not permitted."},
  {kIDE_ERROR, 25, "Invalid bytecode sent from JVM ($%2.2X).", "The JVM sent an invalid bytecode.", "Report the error to technical support."},
  {kIDE_ERROR, 26, "Aborting a possible endless loop.", "The debugger detected that the code might be in an endless loop.", "This message is merely a warning."},
  {kIDE_ERROR, 27, "Binary too long.", "This is an internal error.", "Report the error to technical support."},
  {kIDE_ERROR, 28, "Unhandled exception in JVM: %s", "There was an unhandled exception.", "This indicates an error in your code. See the Javelin manual."},
  {kIDE_ERROR, 29, "Invalid class offset.", "This is an internal error.", "Report the error to technical support."},
  {kIDE_ERROR, 30, "Unable to find constant pool string.", "This is an internal error.", "Report the error to technical support."},
  {kIDE_ERROR, 31, "Maximum number of strings exceeded (%d).", "The current version has a limit on the maximum number of static strings.", "Use fewer strings in your program."},
  {kIDE_ERROR, 32, "String too long.", "This is an internal error.", "Report the error to technical support."},
  {kIDE_ERROR, 33, "Problem invoking the external compiler: %s", "The external Java compiler could not be invoked.", "Check the compiler path is set correctly."},
  {kIDE_ERROR, 34, "Problem reading compiler output (%d)", "This is an internal error.", "Report the error to technical support."},
  {kIDE_ERROR, 35, "Error writing to the serial port. Received byte not expected (%d)", "The IDE received an unexpected byte.", "Repeat the operation."},
  {kIDE_ERROR, 36, "Error reading from the serial port (timeout waiting for echo).", "The echo was not received from the Javelin.", "Make sure the serial cable is connected."},
  {kIDE_ERROR, 37, "Test file name expected.", "The -t option was used without a test file.", "The correct format for the -t option is: '-t file'."},
  {kIDE_ERROR, 38, "Class path expected.", "The -classpath option was used without a path.", "The correct format for the -classpath option is: '-classpath path'."},
  {kIDE_ERROR, 39, "Unknown option on command line: '%s'.", "An unknown option was used on the command line.", "Check the manual for the available command line options."},
  {kIDE_ERROR, 40, "Class compiled without debug information: '%s'.", "A class was compiled without the debug option.", "Recompile the class using JIDE."},
  {kIDE_ERROR, 41, "Maximum number of static fields exceeded (%d).", "Your program uses too many static fields.", "Redesign the program to use fewer static fields."},
  {kIDE_ERROR, 42, "Unknown unhandled exception in JVM (%d).", "This is an internal error.", "Report the error to technical support."},
  {kIDE_ERROR, 43, "Javelin did not respond to reset.", "The Javelin did not respond after being reset.", "Ensure that the Javelin is properly connected to your PC."},
  {kIDE_ERROR, 44, "File must be saved before compiling.", "An attempt was made to compile a file without saving it first.", "Save the file before compiling."},
  {kIDE_ERROR, 45, "Action terminated by user.", "The user terminated the current action.", ""},
  {kIDE_ERROR, 46, "Javelin not found on serial port.", "The Javelin was not connected to the serial port.", "Check that the correct serial port is selected and the Javelin is connected."},
  {kIDE_ERROR, 47, "Another device connected to the serial port.", "The device connected to the serial port is not a Javelin.", "Check that the correct serial port is selected."},
  {kIDE_ERROR, 48, "Internal error.", "An internal error occured in the linker.", "Report the problem to technical support."},
  {kIDE_ERROR, 49, "Cannot open variables while Javelin is running.", "New variables can only be expanded when the Javelin is stopped.", "Stop the Javelin and try again."},
  {kIDE_ERROR, 50, "Error loading the project file.", "The project file for this project has become corrupted.", "Try deleting the project file (.jpf)."},
//  {kIDE_ERROR, 51, "No documentation packages specified.", "Documentation is only generated for packages that are specified.", "Use the Project Pptions dialog to specify which packages you wish to document."},
  {kIDE_ERROR, 52, "Incompatible firmware revision ($%x).", "The firmware in the Javelin is not compatible with this version of the IDE.", "Get the correct version of the IDE to work with the firmware in the Javelin you have."},
  {kIDE_ERROR, 53, "Terminal window queue overflowed.", "Too many characters were typed into the terminal queue before the Javelin had a chance to receive them.", "Try typing slower, or pasting a smaller amount of text."},
  {kIDE_ERROR, 54, "Unable to find a Javelin on any port.", "It was not possible to find a connected Javelin.", "Check that the Javelin is connected and powered up."},
  {kIDE_ERROR, 55, "Javelin did not complete power-on-reset.", "The Javelin did not start up correctly.", "Try resetting the Javelin again."},
  {kIDE_ERROR, 56, "Possible Javelin on %s did not respond.", "It appears that a Javelin is connected but it did not repond correctly.", "Try resetting the Javelin again."},

  {kLAST_ERROR, 0, "", "", ""} };

char *gErrorClasses[] = 
{ "Last", 
  "SYS",
  "JVM",
  "LNK",
  "IDE"};


EError::EError( long aClass, long aNumber, ... ) {
  magic = 0xEEEE;

  errorClass = aClass;
  errorNumber = aNumber;
  
  va_list ap;
  int i, foundError = -1;
  char *errorClassStr;

  // Find the error.
  for ( i = 0; gErrorDefs[i].errorClass != kLAST_ERROR; i++ ) {
    if ( gErrorDefs[i].errorClass == errorClass 
	        && gErrorDefs[i].errorNumber == errorNumber ) {
      foundError = i;
      break;
    }
  }

  if ( foundError == -1 ) 
    sprintf( errorString, "Cannot find text for error message (%d).\n", errorNumber );

  errorClassStr = gErrorClasses[errorClass];

  va_start( ap, aNumber );

  vsprintf( shortMessage, gErrorDefs[foundError].shortMessage, ap);
  problemMessage = gErrorDefs[foundError].problemMessage;
  solutionMessage = gErrorDefs[foundError].solutionMessage;

  sprintf( errorString,"[Error %s-%04d] %s\n", errorClassStr, errorNumber, shortMessage );

  va_end(ap);

};

void EError::PrintError( bool longFormat ) {
  int foundError = -1;
  ErrorOutput(errorString );
  if ( longFormat ) {
    // Find the error.
    for ( int i = 0; gErrorDefs[i].errorClass != kLAST_ERROR; i++ ) {
      if ( gErrorDefs[i].errorClass == errorClass 
          && gErrorDefs[i].errorNumber == errorNumber ) {
        foundError = i;
        break;
      }
    }
    if ( foundError == -1 )
      return;
      
    ErrorOutput("\nProblem:\n  %s\nSolution:\n  %s\n\n",
		  gErrorDefs[foundError].problemMessage,
		  gErrorDefs[foundError].solutionMessage );
  }
}

void FindError( int errorClass, int errorNumber, char **sm, char **pm, char **ss ) {
  int foundError = -1, i;

  for ( i = 0; gErrorDefs[i].errorClass != kLAST_ERROR; i++ ) {
    if ( gErrorDefs[i].errorClass == errorClass 
	        && gErrorDefs[i].errorNumber == errorNumber ) {
      foundError = i;
      break;
    }
  }
  if ( foundError == -1 ) {
    *sm = "Cannot find text for error message";
    *pm = "";
    *ss = "";
  }
  else {
    *sm = gErrorDefs[foundError].shortMessage;
    *pm = gErrorDefs[foundError].problemMessage;
    *ss = gErrorDefs[foundError].solutionMessage;
  }
}

