Javelin Stamp IDE Installer
===========================

This installer will overwrite your previous installation. Any Java files which you have created will not be affected.

For the latest class files, new versions of the IDE and discussion forums, go to www.javelinstamp.com.

It is recommended that you uninstall any previous installation before installing this one.


Known Problems
==============

None.


Version History
===============

2.0.3 - 2/12/03
---------------

* Fixed error in linker when too many strings are used.
* Improved linker to aggressively reuse strings in the binary image.
* Increased resolution, accuracy and rollover behavior of Timer class.
* Fixed problem in stamp.core.CPU with uninstalling and reinstalling VPs at runtime.
* Debug view now shows address of string objects.

2.0.2 - 9/3/02
---------------

* Fixed an error in the linker which resulted in an error message about being unable to find Callback.java.
* Fixed an error in the linker which resulted from using a type cast of an array in an lvalue.
* Improved debugger stack tree display update speed.
* All of the online documentation features have been removed. A precompiled help file is now distributed with JIDE. 
* Add new I/O classes from www.javelinstamp.com.
* The Identify window now opens before starting to scan for Javelins.
* Added supported for debugging nested classes. 
* All VPs now include protection for being started more than once, and for being accessed when they are not started.
* Added new methods to stamp.core.Uart: sendBufferEmpty, sendBufferFull, setDirection, restart.
* Fixed dialog boxes to work if the system is using large fonts.
* Added the stamp.core.Freqout class.
* Added documentation for the StringBuffer and stamp.util.os classes.
* Added indexOf methods to StringBuffer and String classes and added toNewString method to StringBuffer.

2.0.1 - 5/26/02
---------------

* Reworked the automatic serial port handling. This fixes lockup problems with other serial port users, like the Palm Desktop. 
* Improved the Integer.parseInt() method so it will correctly parse malformed strings.

2.0 - 4/20/02
---------------

* Double clicking on a file in Windows Explorer will open the file in the IDE. This feature does not work if you try to open more than one file at once.

1.17.0 - 4/12/02
---------------

* The state of the debugging tree view is now reset between sessions.
* The StringBuffer class will now automatically increase the size of its internal buffer.
* Strings longer than 200 bytes will no longer crash the debugger, but they will be truncated and only the first 200 bytes displayed.
* Specifying a particular serial port, rather than 'Auto', will now work.
* The default window sizes are now much more sensible.


1.16.0 - 4/6/02
---------------

* Added "Open class at cursor" feature to allow easier navigation between files.
* Installer will now ask before associating .java files with JIDE.
* Moved DS1620 class into stamp.periperhal.sensor.temperature directory.
* JIDE supports drag and drop. You can drag any file onto the editor window and the file will be opened.

1.15.0 - 3/31/02
----------------

* Added new setInput and setOutput native methods.
* Added new I/O methods to the stamp.core.CPU class.
* Double clicking an associated file will now open that file when the Javelin IDE is launched.
* Fixed error where compiler complained it couldn't find files.
* Added jog option to debugger.
* Added stamp.core.Memory class to enquire about free memory in the Javelin Stamp.

1.13.0 - 3/13/02
----------------

* Version number rev only.

1.12.0 - 3/13/02
----------------

* The help screen now goes to the help page automatically on startup.
* Stamp firmware version number checking is improved so that any future firmware changes will only require a new version of JIDE if the communication protocol changes.
* Updated the DS1620 class.
* Updated time unit values in the stamp.core.CPU and stamp.core.Timer classes.

1.11.0 - 3/11/02
----------------

* When Save As is used the file name is now correctly added to the recent files list.
* Added the complete stamp.core.Button class.
* Updated the timing compensation values in stamp.core.Timer for 25MHz operation.
* Added the program icon.


1.10.0 - 2/23/02
----------------

* Fixed missing green program counter line while debugging.
* Added Identify... dialog box to help with version management and serial debugging.
* Reenabled drag and drop editing.
* It is not possible to print the help screens.
* Added parseInt functions to the Integer class for converting strings to integers.

1.9.0 - 2/18/02
---------------

* Initial production release.
