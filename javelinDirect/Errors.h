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

#ifndef _ERRORS_H_
#define _ERRORS_H_

#include "CObject.h"
#include "Utils.h"

typedef struct {
   int errorClass;
     // The class of the error.
   int errorNumber;
     // The number of the error.
   char *shortMessage;
     // A succinct statement of the error.
   char *problemMessage;
     // Message saying what the problem is.
   char *solutionMessage;
     // Message saying what to do about the problem.
} tErrorDefs;

enum {
   kLAST_ERROR = 0,
   kSYS_ERROR,
   kJVM_ERROR,
   kLINK_ERROR,
   kIDE_ERROR
};

extern tErrorDefs gErrorDefs[];

extern char *gErrorClasses[];

class EError {

  unsigned long magic;

public:

  char errorString[1024];

  long errorClass;
  long errorNumber;

  char shortMessage[1024];
  char *problemMessage;
  char *solutionMessage;

  EError( long aClass, long aNumber, ... );
    // Create an error.

  virtual void PrintError( bool longFormat = false );
    // Print out a message for the error. If longFormat is true
    // Then the error message is more verbose.
};

void FindError( int errorClass, int errorNumber, char **sm, char **pm, char **ss );

#endif
