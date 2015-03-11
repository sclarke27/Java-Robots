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
 
#include "Utils.h"

#include "Errors.h"

#include <string.h>
#include <stdarg.h>


void StatusOutput(const char *format, ...)
{
   va_list ap;

   va_start(ap,format);

      vprintf(format, ap);
      fflush( stdout );

   va_end(ap);
}

void ErrorOutput(const char *format, ...)
{
   va_list ap;

   va_start(ap,format);

   vprintf(format, ap);
   fflush( stdout );
   va_end(ap);
}

void StatusDebug(const char *format,...)
{
   va_list ap;

   va_start(ap,format);


     printf("[Debug] ");
     vprintf(format, ap);
     fflush( stdout );

   va_end(ap);
}

void StatusError( long errorClass, long errorNumber,...)
{
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

   if ( foundError == -1 ) {
     printf( "Cannot find text for error message (%d).\n", errorNumber );
     exit(1);
   }

   errorClassStr = gErrorClasses[errorClass];

   va_start( ap,errorNumber );

     char buf[1024];
     vsprintf(buf, gErrorDefs[i].shortMessage, ap);
     StatusOutput( "[Error %s-%04d] %s", errorClassStr, errorNumber, buf );
     //StatusOutput( "\nProblem:\n  %s\nSolution:\n  %s\n\n", gErrorDefs[i].problemMessage,
	   //  gErrorDefs[i].solutionMessage );
     fflush( stdout );

   va_end(ap);
}

void FailNULL( void *p ) {
  if ( p == NULL )
    throw EError( kSYS_ERROR, 30 );
}

#ifdef WIN32
void EnumerateSerialPorts(TStrings *ports)
{

  //Iterate through all the possible supported ports
  for (UINT i=1; i<20; i++)
  {
    //Form the Raw device name
    AnsiString sPort;

    sPort.sprintf("COM%d", i);

    //Call GetDefaultCommConfig to see if the port is present
    COMMCONFIG cc;
    cc.dwSize = sizeof(COMMCONFIG);
    DWORD dwCCSize = cc.dwSize;
    if (GetDefaultCommConfig(sPort.c_str(), &cc, &dwCCSize))
    {
      //Add the port number to the array which will be returned
      ports->Append(sPort);
    }
  }
}
//---------------------------------------------------------------------------
#endif

