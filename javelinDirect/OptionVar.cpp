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

#include "OptionVar.h"
#include "Utils.h"
#ifdef WIN32
#include <dir.h>
#else
#include <unistd.h>
#endif
#include <stdlib.h>

tOptions gOptions;

const int kRegistryVersion = 1;

void InitOptions() {
  DefaultOptions();
}

void FreeOptions() {
  free(gOptions.classPath);
  free(gOptions.compilerPath);
}

void DefaultOptions() {
  char dir[501];
  
  getcwd(dir, 500);

	gOptions.classPath = (char*)malloc(2*strlen(dir) + strlen("\\lib;") + strlen("\\Projects") + 1 );
  FailNULL(gOptions.classPath);
  strcpy(gOptions.classPath,dir);
  strcat(gOptions.classPath,"\\lib;");
  strcat(gOptions.classPath,dir);
  strcat(gOptions.classPath,"\\Projects");

  gOptions.compilerPath = (char*)malloc(strlen(dir) + strlen("\\jikes\\jikes.exe") + 1 );
  FailNULL(gOptions.compilerPath);
  strcpy(gOptions.compilerPath,dir);
  strcat(gOptions.compilerPath,"\\jikes\\jikes.exe");

  gOptions.debugPort = 0;

  gOptions.singleStepBytecode = false;

  gOptions.debugComLevel = kDebugComNone;
  gOptions.debugLinker = false;
  gOptions.debugLineTranslate = false;
  gOptions.debugStepping = false;
  gOptions.debugClassLoader = false;
  gOptions.debugDataView = false;
  gOptions.debugJEMFile = false;
  gOptions.debugIgnoreEchoes = false;
  gOptions.debugZeroStackOffset = false;
  gOptions.debugLMNative = false;

  gOptions.cmdLineMode = false;

}

