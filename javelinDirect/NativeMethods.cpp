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

#include "NativeMethods.h"
#include <string.h>
/* This file contains a list of all of the native methods in the
   library classes. From this list the native method dispatch
   table is constructed.
   */

char *nativeList[][2] = {
  {"stamp/core/CPU", "writeRegister"},
  {"stamp/core/CPU", "readRegister"},
  {"stamp/core/CPU", "writeObject"},
  {"stamp/core/CPU", "delay"},
  {"stamp/core/CPU", "message"},
  {"page", ""},
  {"stamp/core/CPU", "count"},
  {"stamp/core/CPU", "rcTime"},
  {"stamp/core/CPU", "pulseIn"},
  {"stamp/core/CPU", "pulseOut"},
  {"stamp/core/CPU", "readPin"},
  {"stamp/core/CPU", "writePin"},
  {"stamp/core/CPU", "shiftIn"},
  {"stamp/core/CPU", "shiftOut"},
  {"stamp/core/CPU", "carry"},
  {"page", ""},
  {"stamp/core/CPU", "uninstall"},
  {"stamp/core/CPU", "install"},
  {"stamp/core/CPU", "nap"},
  {"stamp/core/CPU", "setOutput"},
  {"page", ""},
  {"stamp/core/CPU", "setInput"},
  {"page", ""},
  {"stamp/core/CPU", "readSRAM"},
  {"page", ""},
  {"stamp/core/CPU", "writeSRAM"},
  {"page", ""},

  /* EEPROM */
  {"stamp/core/EEPROM", "eeRead"},
  {"page", ""},
  {"stamp/core/EEPROM", "eeWrite"},
  {"page", ""},

  /* Serial */
  {"stamp/core/Uart", "txInit"},
  {"stamp/core/Uart", "rxInit"},
  {"stamp/core/Uart", "rxRead"},

  /* Callback */
  {"stamp/core/Callback", "returnTwo"},

  /* Timer */
  {"stamp/core/Timer", "init"},
  {"stamp/core/Timer", "latch"},
  {"page", ""},

  /* PWM */
  {"stamp/core/PWM", "install"},
  {"page", ""},
  {"stamp/core/PWM", "updateInternal"},
  {"page", ""},

  /* DAC */
  {"stamp/core/DAC", "install"},
  {"page", ""},

  /* ADC */
  {"stamp/core/ADC", "install"},
  {"page", ""},

  /* Terminal */
  {"stamp/core/Terminal", "getByte"},
  {"page", ""},

  {"", ""}};

void LoadNativeMethods() {

}
