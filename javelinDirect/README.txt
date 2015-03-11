Javelin Stamp Direct Interface
==============================

http://www.parallax.com/javelin/

Command-line utility to link and download a program to a Javelin
Stamp. This is a subset of the code used by the Javelin Integrated
Debugging Environment (JIDE) and is distributed in the hope that
it is useful for people who want to get Javelin support on 
platforms other than Windows.

License 
-------

Copyright (c) 1997-2005 Christopher Waters. All rights reserved.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Usage
-----

JavelinDirect [options] ClassName
   Link and download a Javelin program from the command line.

   ClassName is the name of the .class file containing the
   main() method. Do not use the .class or .java suffix.

   --path <path> Classpath. Multiple paths are separated by semicolons.
                 Use quotes if there are spaces in the paths.
   --port <port> Serial port number. e.g. 2 for COM2.
                 The port number will be automatically chosen if not specified.
   --listen      After downloading the program listen and receive any
                 debug messages from the Javelin.
   --help        Display this help message
   
  e.g. JavelinDirect --path "C:\javelin\lib;C:\javelin\Files" BoEBot

Building
--------

The Javelin IDE was written using Borland C++Builder 6. It has not been
compiled with any other compiler, but the linker and communication code 
is straight C++ and should be relatively easy to port. A different platform
or compiler would require a replacement for the ComDrv.pas file which
provides the interface to the serial port.