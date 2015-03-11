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
//---------------------------------------------------------------------------
#ifndef TSXCommH
#define TSXCommH
//---------------------------------------------------------------------------

#include <vcl/sysdefs.h>
#include <ComDrv32.hpp>

const unsigned char kReplyBit = 0x80;
const unsigned char kFlag = 0x7E;
const unsigned char kEscape = 0x7D;
const unsigned char kInterrupt = 0x19;

const unsigned char kDebugMagic	= 0x1D;
const unsigned char kRunMagic	= 0xA4;

const int kResetWait = 10000; // Time taken for SX to reset.
const int kAckWait = 5000; // The amount of time (in milliseconds) to wait for acknowledgement.
const int kResetCharWait = 3; // The amount of time to wait before sending first char after a reset.
const int kProgramWait = 2000; // The amount of time (in milliseconds) to wait for acknowledgement when programming.
const int kStepWait = 20000; // The amount of time (in milliseconds) to wait for acknowledgement when stepping.
const int kBaseVersion = 0x50; // Compatible base version of the firmware.
const int kMaxQueueLength = 500; // Maximum number of characters than can be queued.

/*  Downloading to tthe SX (breakpoints and program code) now uses a two step
    process. The first step is to ask the SX if it is ready to accept the download.
    The second step is to do the download. This is so that if the SX cannot accept
    the download there is no chance of a partial download resulting in a corrupt
    packet confusing the state machine.
*/

class CJEMLinker;

typedef struct {
  AnsiString PortName;
  AnsiString DeviceName;
  AnsiString DeviceVersion;
  bool Loopback;
  bool Echo;
  bool Status;
} PortInfo;

class TSXComm : public TObject {

	enum {
  	kComNone = 0,
  	kComStatus = 1,
    kComReset = 2,
    kComStep = 3,
    kComRun = 4,
    kComError = 5,
    kComStop = 6,
    kComStack = 7,
    kComConfig = 8,
    kComHeap = 9,
    kComBreakpoints = 10,
    kComProgram = 11,
    kComDownloadOK = 12,
    kComStepDone = 13,
    kComAtn = 14,
    kComStartLoad = 15,
    kComEndLoad = kBaseVersion,
    kComTerminalQuery =	17,	// Check for data from the terminal.
    kComTerminalData	=	18,	// Data follows.
    kComTerminalNoData = 19	// No data follows.
  };

  enum {
    kErrNone = 0,
    kErrOpcode = 1,
    kErrBreakpoint = 2,
    kErrMessage = 3,
    kErrException = 4,
    kErrEnd = 5
  };

	TCommPortDriver *com; // Communications pipeline to the SX.

  bool running; // Whether the JVM is currently running.
  bool messageEscape; // Whether The previous character was an escape in a message.
  bool inMessage; // Whether a message is part processed.

  #define BUFFER_LENGTH 3048
  unsigned char buffer[BUFFER_LENGTH];
  int bufferPos;

  int sentCount; // The number of characters that have been sent and no echo yet.

public:

  enum {
  	kPassive = 0x00,
    kRun,
    kJog,
    kBreak
  };

  char terminalQueue[kMaxQueueLength];
  int queueLength;
  PortInfo Info;

	__fastcall TSXComm(TCommPortDriver *aCom);
  virtual __fastcall ~TSXComm();

  void __fastcall Open();
    // Open the comm port.
  bool __fastcall AutoChoosePort();
    // Find a port with a Javelin connected.
  bool __fastcall TestPort(int PortNum);
    // Test a single port.
  void __fastcall Test();
    // Test for existance of the Javelin.
  void __fastcall Program( char *data, long length );
  	// Download a program (.jem file) to the SX.
  void __fastcall Reset(bool Run);
  	// Reset the SX.
  void __fastcall Step();
  	// Ask the SX to single step.
  void __fastcall Run();
  	// Ask the SX to run.
  void __fastcall Stop();
  	// Ask the SX to stop.
  void __fastcall Break();
  	// Retrieve the JVM stack.
  void __fastcall QueryHeap(int rangeStart, int rangeLength, unsigned char *data, int *hp );
  	// Retrieve a portion of the JVM heap.

  void __fastcall QueryStatus(int *pc, int *activity, int *bytecode);
		// Ask the SX its status.
  void __fastcall QueryConfig(int *memSize, int *progSize, int *heapBase);
  	// Ask the SX the size of its stack/heap and its program memory.
  void __fastcall SendCommand(int command);
  	// Send a command.
  void __fastcall SendSimpleCommand(int command);
  	// Send a command and receive the simple reply.
  void __fastcall ReceiveAck(int command, int wait = kAckWait);
    // Receive the acknowledgement to a simple command.
  void __fastcall DownloadBreakpoints( int *bpList, int count, int stepBP  );
    // Download a list of breakpoints to the SX.
  void __fastcall DownloadJemFile( char *jemData, int length, bool debug );
  	// Download the .jem file to the SX.

  void __fastcall ProcessMessage(CJEMLinker *linker );
  int __fastcall ReceiveMessage(unsigned char *packet, int length, int pos, CJEMLinker *linker );
  void __fastcall HandlePacket( unsigned char *packet, int length, CJEMLinker *linker );
    // Process an unsolicited packet.

  void __fastcall SendTerminalByte( char c );
    // Send a single byte from the terminal.

  void __fastcall DataAvailable();
    // Respond to an event from the comm component.

  void __fastcall TerminalSendByte();
    // Send a byte from the terminal queue.

  unsigned char __fastcall ComputeChecksum(unsigned char *packet, int length);
  	// Compute the checksum byte for a packet.
  bool __fastcall CheckChecksum(unsigned char *packet, int length);
  	// Check that the checksum is correct.

  void __fastcall SendPacket(unsigned char *packet, int length);
  	// Transmit a packet.
	void __fastcall ReadPacket(unsigned char *packet, int *length);
  	// Receive a packet.
  void __fastcall PutChar( char c );
    // Write a character to the serial port.
  char __fastcall GetChar(int wait = kAckWait);
    // Read a character from the serial port.
  bool __fastcall CharReady() { return com->CharCount() > 0; };
    // See if any characters have been received.

  bool __fastcall Running() { return running; };
  	// Check if the JVM is running.
  void __fastcall SetRunning( bool runFlag );
  	// Change the running state.

  void __fastcall RelaxLink(int relaxTime);
    // Wait to allow the SX to catch up since there is no flow control.
};

/*

Packet formats:

To SX:
kStatusCommand = $1 { $1, checksum }

From SX:
kStatusReply = $1 { $1, PC_lsb, PC_msb, activity, opcode, checksum }


kStackReply = $7 { $7, SP, FP, method_lsb, method_msb, bytes ... }


kBreakpoints = 10 { count, bp1_lsb, bp1_msb, bp2_lsb, bp2_msb, ... }
*/

#endif
