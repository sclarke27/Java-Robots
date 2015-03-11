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

#ifndef _OPTIONVAR_H_
#define _OPTIONVAR_H_

#define MAX_NUM_PORTS 20

struct tOptions {
  bool loaded; // Whether the options have been loaded.
  
	char *classPath; // Path to search for class files.
  char *compilerPath; // Path to the Jikes compiler.
  bool singleStepBytecode; // Whether Step Into single steps byte codes.

  int debugPort; // Serial port number.

  int numPorts;
  int validPorts[MAX_NUM_PORTS]; // The array of valid serial port numbers.

  // Debugging options.
  int debugComLevel; // Level of debugging detail. (1=functions, 2=packets, 3=bytes)
  bool debugLinker; // Debug the linker.
  bool debugLineTranslate; // Debug line translations.
  bool debugStepping; // Debug stepping.
  bool debugClassLoader; // Debug the class loader.
  bool debugDataView; // Debug the data viewer.
  bool debugJEMFile; // Generate a JEM file.
  bool debugIgnoreEchoes; // Ignore echoed characters from the Javelin.
  bool debugZeroStackOffset; // Start the stack at memory address zero.
  bool debugLMNative; // Use lightningmail native methods.
  bool debugJVM2; // Work with version 2 JVM.

  // Command line options.
  bool cmdLineMode; // Whether we are operating in command line mode.
};

enum {
  kDebugComNone = 0,
  kDebugComFunctions = 1,
  kDebugComPackets = 2,
  kDebugComBytes = 3
};

extern tOptions gOptions;

void InitOptions();
void FreeOptions();
void DefaultOptions();

#endif
