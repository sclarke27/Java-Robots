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
#ifndef _UTILS_H_
#define _UTILS_H_

#ifdef WIN32
#include <vcl.h>
#include <vcl/registry.hpp>
#pragma hdrstop
#endif

#include <stdlib.h>
#include <stdio.h>

#include <string.h>
#include <memory.h>

#define min(a,b) ((a)<=(b)?(a):(b))
#define max(a,b) ((a)>=(b)?(a):(b))

void StatusOutput( const char *format, ...);
void ErrorOutput(const char *format, ...);
void StatusDebug( const char *format, ...);
void StatusError( long errorClass, long errorNumber,...);

void FailNULL( void *p );
// Throw an out of memory exception if p is NULL.

#ifdef WIN32
void EnumerateSerialPorts(TStrings *ports);
#endif
#endif










