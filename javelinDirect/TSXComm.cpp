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
#include <vcl.h>
#pragma hdrstop
#include "TSXComm.h"
#include "Errors.h"
#include "OptionVar.h"
#include "CJEMLinker.h"
#include <dos.h>
#include <winbase.h>
#include <math.h>
#include <stdio.h>
//---------------------------------------------------------------------------

extern CJEMLinker *Linker;

__fastcall TSXComm::TSXComm(TCommPortDriver *aCom)
{
  SetRunning(false);

  inMessage = false;
  messageEscape = false;

 	com = aCom;

  sentCount = 0;
  queueLength = 0;
}

__fastcall TSXComm::~TSXComm()
{
  if ( com->Connected() )
    com->Disconnect();
}

void __fastcall TSXComm::Open()
{
  bool portFound = false;
  char c;
  
  // Open the serial connection.
  if ( com->Connected() )
    com->Disconnect();

  if ( gOptions.debugPort == 0 ) {
    // Choose port automatically.
    portFound = AutoChoosePort();
  }
  else {
    portFound = true;
    Test();
  }

  if ( !portFound )
    throw EError( kIDE_ERROR, 54);

  if ( !com->Connect() )
    throw EError( kIDE_ERROR, 6);

  // Discard any bytes in the buffer.
  while ( CharReady() && com->ReadChar(c))
    ;
}

bool __fastcall TSXComm::AutoChoosePort() {

  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::AutoChoosePort");

  // We test all valid ports on the machine.
  try {
    for ( int i = 0; i < gOptions.numPorts; i++ ) {

      if ( TestPort(gOptions.validPorts[i]-1) )
        return true;
    }
    if ( gOptions.debugComLevel >= kDebugComFunctions )
      StatusDebug("TSXComm::AutoChoosePort - unsuccessful");
  }
  __finally {

  }

  return false;
}

bool __fastcall TSXComm::TestPort(int PortNum)
{
  com->ComPort = PortNum;
  if ( gOptions.debugComLevel >= kDebugComFunctions )
    StatusDebug("Testing port: COM%d", PortNum+1);

  try {
    if ( !com->Connect() )
      throw EError( kIDE_ERROR, 6);
    try {
      Test();
    }
    catch (...) {
      // Try again immediately if it doesn't work the first time.
      Test();
    }
    if ( com->Connected() )
      com->Disconnect();
    if ( gOptions.debugComLevel >= kDebugComFunctions )
      StatusDebug("TSXComm::AutoChoosePort - successful");
    return true;
  }
  catch (EError e) {
    if ( e.errorNumber == 47 || e.errorNumber == 22) {
      char Buffer[300];

      sprintf(Buffer, "COM%d", com->ComPort+1 );
      e = EError(kIDE_ERROR, 56, Buffer);
      e.PrintError();
    }
    else if ( e.errorNumber == 55 || e.errorNumber == 52 ) {
      e.PrintError();
    }

    //e.PrintError();
    if ( com->Connected() )
      com->Disconnect();
  }

  return false;
}

void __fastcall TSXComm::Test() {
	int pc, activity, bytecode;
  char c;

  try {
    if ( !com->Connect() )
      throw EError( kIDE_ERROR, 6);

    // Discard any bytes in the buffer.
    while ( CharReady() )
      if ( com->ReadChar(c) == 0 )
        break;

    // Check RTS and DSR loopback.
    com->ToggleRTS(true);
    int startTime = GetTickCount();
    while ( GetTickCount() < startTime + 200 )
      ;
    if ( !com->DSR )
      throw EError( kIDE_ERROR, 46);

    com->ToggleRTS(false);
    startTime = GetTickCount();
    while ( GetTickCount() < startTime + 200 )
      ;
    if ( com->DSR )
      throw EError( kIDE_ERROR, 46);

    if ( gOptions.debugComLevel >= kDebugComFunctions )
      StatusDebug("Found connected Javelin Stamp, now testing for power");

    // Reset the Javelin.
    Reset(false);

    // Check for an echo.
    PutChar(kComAtn);
    if ( !gOptions.debugIgnoreEchoes )
      --sentCount;

    int charTime = GetTickCount();
    while ( !CharReady() ) {
      if ( charTime + 100 < GetTickCount() )
        throw EError(kIDE_ERROR, 46 );
    }

    int result = com->ReadChar(c);
    if ( result != 0 )
      throw EError(kIDE_ERROR, 8, GetLastError() );
    if ( c != kComAtn )
      throw EError(kIDE_ERROR, 47);

    // Check the status.
    QueryStatus(&pc, &activity, &bytecode);
    if ( gOptions.debugComLevel >= kDebugComPackets )
      StatusDebug("JVM PC: %d activity %d\n", pc, activity );
  }
  __finally {
    if ( com->Connected() )
      com->Disconnect();
  }
}

void __fastcall TSXComm::QueryStatus(int *pc, int *activity, int *bytecode)
{
	unsigned char packet[100];
  int length = 100;

   if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::QueryStatus");

  SendCommand(kComStatus);

  ReadPacket(packet, &length);
  if ( length != 6 )
    throw EError(kIDE_ERROR,10);
  if ( !CheckChecksum(packet, 6) )
  	throw EError(kIDE_ERROR,9);
  if ( packet[0] != (unsigned char)((unsigned char)kComStatus|kReplyBit) )
  	throw EError(kIDE_ERROR,10);

  *pc = (packet[1]|(packet[2]<<8))-1; // Subtract 1 to compensate for the increment in the JVM.
  *activity = packet[3];
  *bytecode = packet[4];
}

void __fastcall TSXComm::QueryConfig(int *memSize, int *progSize, int *heapBase)
{
	unsigned char packet[100];
  int length = 100;

  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::QueryConfig");

  SendCommand(kComConfig);

  ReadPacket(packet, &length);
  if ( length != 8 )
    throw EError(kIDE_ERROR,10, length);
  if ( !CheckChecksum(packet, 8) )
  	throw EError(kIDE_ERROR,9);
  if ( packet[0] != (unsigned char)((unsigned char)kComConfig|kReplyBit) )
  	throw EError(kIDE_ERROR,10);

  *memSize = packet[1]|(packet[2]<<8);
  *heapBase = packet[5]|(packet[6]<<8);

  // The returned program size is a constant and does not reflect the true
  // program size.
  *progSize = packet[3]|(packet[4]<<8);

}

void __fastcall TSXComm::SendCommand(int command)
{
  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::SendCommand(%d)", command);

  // Send the command.
  PutChar(command);
}

void __fastcall TSXComm::SendSimpleCommand(int command)
{
  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::SendSimpleCommand(%d)", command);

  // Send the command.
  PutChar(command);

  ReceiveAck(command);

  if ( command == kComReset )
    RelaxLink(kResetWait); // Wait for the SX to reset before doing anything else.
}

void __fastcall TSXComm::ReceiveAck(int command, int wait )
{
  // Wait for acknowledgement.
  int charTime = GetTickCount(), endTime;
  unsigned char c;

  while ( charTime + wait >= GetTickCount() ) {
    while ( !CharReady() ) {
        if ( charTime + wait < GetTickCount() )
          throw EError(kIDE_ERROR, 22 );
    }
    endTime = GetTickCount();

    c = GetChar();

    if ( gOptions.debugComLevel >= kDebugComBytes )
      StatusDebug("TSXComm::received byte $%2.2X '%c' in %dms", c, isprint(c)?c:'.', endTime-charTime);

    if ( c == (unsigned char)((unsigned char)command|kReplyBit) )
      return;

    // We just got an asynchronous message. We can't deal with it here so
    // pretend we just received a one byte packet and queue it for later.
    HandlePacket(&c, 1, Linker);
  }

  // We didn't get the reply we expected within the timeout.
  throw EError(kIDE_ERROR, 22 );
}

unsigned char __fastcall TSXComm::ComputeChecksum(unsigned char *packet, int length)
{
	unsigned char checksum = 0;
	for ( int i = 0; i < length; i++ )
  	checksum += packet[i];

  return (~checksum) + 1;
}

bool __fastcall TSXComm::CheckChecksum(unsigned char *packet, int length)
{
	char checksum = 0;
	for ( int i = 0; i < length; i++ )
  	checksum += packet[i];

  return (checksum == 0);
}

void __fastcall TSXComm::SendPacket(unsigned char *packet, int length)
{
  char *packetBuf;
  int packetLength = 0;

  packetBuf = (char*)malloc(1024);

  for ( int i = 0; i < length; i++ ) {
    if ( packet[i] == kEscape || packet[i] == kFlag ) {
      packetBuf[packetLength++] = kEscape;
      packetBuf[packetLength++] = packet[i]^0x20;
    }
    else
      packetBuf[packetLength++] = packet[i];
  }
  
  int result;

  if ( gOptions.debugComLevel >= kDebugComBytes )
    StatusDebug("Transmiting packet");

  sentCount += packetLength;

  result = com->SendData(packetBuf, packetLength );

  free( packetBuf );

  if ( result != packetLength )
    throw EError(kIDE_ERROR, 7, GetLastError() );
}

void __fastcall TSXComm::SendTerminalByte( char c ) {
  // Add the byte to the terminal queue.
  if ( queueLength == kMaxQueueLength )
    throw EError(kIDE_ERROR, 53);
  else
    terminalQueue[queueLength++] = c;
}

void __fastcall TSXComm::ReadPacket(unsigned char *packet, int *length)
{
	int startTime, charTime, nowTime, charDelay, packetDelay;
  int lengthRead;
  bool gotFlag, escape;
  unsigned char c;

  // If the program is running make the delays longer in case the
  // program calls for a delay.
  if ( running ) {
    charDelay = 5000;
    packetDelay = 6000;
  }
  else {
    charDelay = 1000;
    packetDelay = 2000;
  }

restart:

	lengthRead = 0;
  gotFlag = true; // Packets no longer start with a flag.
  escape = false;

	// Get the current time.
  startTime = GetTickCount();

  while (true) {
    charTime = GetTickCount();
  	while ( !CharReady() ) {
  	  nowTime = GetTickCount();

 	  	if ( nowTime - charTime > charDelay
      	   || nowTime - startTime > packetDelay )
  	  	throw EError(kIDE_ERROR, 22 );

  	}
    c = GetChar();
    if ( !gotFlag && c == kFlag ) {
     	gotFlag = true;
      lengthRead = 0;
      if ( gOptions.debugComLevel >= kDebugComBytes )
    		StatusDebug("First flag - resetting");
    }
    else if ( gotFlag && c == kFlag && lengthRead > 0 ) {
      if ( gOptions.debugComLevel >= kDebugComBytes )
    		StatusDebug("Flag - end");
     	break;
    }
    else if ( gotFlag && c == kFlag ) {
      if ( gOptions.debugComLevel >= kDebugComBytes )
    		StatusDebug("Flag - resetting");
     	lengthRead = 0;
    }
    else if ( c == kEscape ) {
      if ( gOptions.debugComLevel >= kDebugComBytes )
    		StatusDebug("Escape");
    	escape = true;
      continue;
    }
    else {
    	if ( escape ) {
       	if ( gOptions.debugComLevel >= kDebugComBytes )
        	StatusDebug("Escaped data");
      	c ^= 0x20;
      }
      /*
      else if ( c < 0x20 ) {
        if ( gOptions.debugComLevel >= kDebugComBytes )
        	  StatusDebug("Received unknown control character 0x%2.2X '%c'", c, c );
        escape = false;
        continue;
      }
      */
  	  if ( gOptions.debugComLevel >= kDebugComBytes )
    		StatusDebug("Receive 0x%2.2X '%c'", c, c);
     	packet[lengthRead++] = c;
      if ( lengthRead == *length )
      	throw EError(kIDE_ERROR,11);
    }

    escape = false;
  }
  // If there is still data to retrieve get the next packet. (The SX must only
  // send one packet at a time.
  if ( CharReady() ) {
    //if ( gOptions.debugComLevel >= kDebugComBytes )
      StatusDebug("Discarding packet");
    goto restart;
  }

  *length = lengthRead;
}

void __fastcall TSXComm::Reset(bool Run) {
  bool receivedEcho = false, receivedEnd = false;

  if ( gOptions.debugComLevel >= kDebugComFunctions )
    StatusDebug("Resetting");

  com->ToggleDTR(true);
  int startTime = GetTickCount();
  while ( GetTickCount() < startTime + 2 )
    ;

  com->ToggleDTR(false);

  // Wait until we receive the comLoadStart from the Javelin.
  startTime = GetTickCount();
  char c = 0;
  while ( GetTickCount() < startTime + kResetWait )
    if ( (c=GetChar(kResetWait)) == kComStartLoad )
      break;

  if ( c != kComStartLoad ) {
    if ( gOptions.debugComLevel >= kDebugComFunctions )
      StatusDebug("Incorrect response after reset");
    throw EError(kIDE_ERROR, 43);
  }
    
  if ( gOptions.debugComLevel >= kDebugComBytes )
    StatusDebug("Got kComStartLoad");

  if ( !Run ) {
    // Wait a little bit to avoid a collision with the download start and
    // the ATN.
    ::Sleep(50);
    PutChar(kComAtn);
    --sentCount;
  }

  while ( !receivedEcho || !receivedEnd ) {
    char c = GetChar(kResetWait);

    if ( c == kComAtn )
      receivedEcho = true;
    else {
      Info.DeviceVersion.printf("$%2.2X", c);
      if ( (c&0xf0) == kComEndLoad ) {
        receivedEnd = true;
        if ( gOptions.debugComLevel >= kDebugComBytes )
          StatusDebug("Got kComEndLoad");
        if ( Run )
          return;
      }
      else {
        throw EError(kIDE_ERROR, 52, c);
      }
    }
  }
}

void __fastcall TSXComm::Step() {
  SendSimpleCommand(kComStep);
  // Problem here when asynchronous message received.
  ReceiveAck(kComStepDone, kStepWait);
}

void __fastcall TSXComm::Run() {
  SendSimpleCommand(kComRun);
  SetRunning(true);
}

void __fastcall TSXComm::Stop() {
  SendCommand(kComStop);
  SetRunning(false);
  ReceiveAck(kComStop, kStepWait);
}

void __fastcall TSXComm::TerminalSendByte() {
  if ( queueLength == 0 ) {
    PutChar(kComTerminalNoData);
  }
  else {
    PutChar(kComTerminalData);
    PutChar(terminalQueue[0]);
    memcpy(terminalQueue,terminalQueue+1,--queueLength);
  }
}

void __fastcall TSXComm::ProcessMessage(CJEMLinker *linker )
{
  switch ( buffer[0] ) {
  case kErrNone:
    break;

  case kErrOpcode:
    SetRunning( false );
    try {
    }
    catch (...) {}
    throw EError( kIDE_ERROR, 24, gJavaOps[linker->Jem2JavaByte(buffer[1])]);

  case kErrMessage:
    buffer[bufferPos] = '\0';
    printf((char*)(buffer+1));
    break;

  case kErrException:
    try {
      CJavaClass *j = linker->FindClassByOffset( buffer[2]|(buffer[1]<<8) );
      char *className = j->GetJavaClassName();
      char *fileName;
      int line, jPC, pc;

      pc = (buffer[4]|(buffer[3]<<8))-1; // -1 to compensate for PC increment.
      fileName = linker->TranslateLineNumber( pc, &line, &jPC );
      if ( gOptions.cmdLineMode ) {
        printf("Exception: %s\n", className );
        Application->Terminate();
      }
      else {
        StatusError( kIDE_ERROR, 28, className );
        ErrorOutput( "Exception thrown in file '%s' at line %d", fileName, line );
      }
    }
    catch (...) {
      throw EError( kIDE_ERROR, 42, buffer[2]|(buffer[1]<<8) );
    }
    break;

  case kErrBreakpoint:
  case kErrEnd:
    if ( gOptions.cmdLineMode ) {
      Application->Terminate();
    }
    else if ( Running() ) {
      SetRunning( false );
    }
    break;

  default:
    StatusDebug("Unknown message from Javelin");
  }
}

// Receive a packet and return the position of the next byte in the packet.
int __fastcall TSXComm::ReceiveMessage(unsigned char *packet, int length, int pos, CJEMLinker *linker )
{
  while ( pos < length ) {
    if ( packet[pos] != kEscape && packet[pos] != kFlag ) {
      unsigned char data = packet[pos];

      if ( messageEscape ) {
        data ^= 0x20;
        messageEscape = false;
      }

      if ( bufferPos >= BUFFER_LENGTH ) {
        StatusDebug("Message buffer length exceeded");
      }
      else {
        buffer[bufferPos++] = data;
      }
    }
    else if ( packet[pos] == kEscape ) {
      messageEscape = true;
    }
    else if ( packet[pos] == kFlag ) {
      // Process the complete packet.
      inMessage = false;
      ProcessMessage(linker);
      return pos+1;
    }
    pos++;
  }
  return pos;
}

// Improved handling for packets.
void __fastcall TSXComm::HandlePacket( unsigned char *packet, int length, CJEMLinker *linker )
{
  unsigned char cmd;
  int diff, pos = 0;

  // Check if we have an incomplete message from the last invocation.
  if ( inMessage ) {
    pos = ReceiveMessage(packet, length, pos, linker);
  }

  // Don't throw any uncaught exceptions in this method. It is called asynchronously.
  try {
    // Skip over any expected echos.
    /* TODO: The echos may not be at the start of the packet? */
    diff = min(length,sentCount);
    pos += diff;
    sentCount -= diff;

    // Now process each character in the packet.
    while ( pos < length ) {
      cmd = packet[pos++];

      switch ( cmd ) {
      case kComTerminalQuery:
        // The module has requested data from the terminal queue.
        TerminalSendByte();
        break;

      case kComError:
        // Accumulate the entire message up to the flag character and process it.
        messageEscape = false;
        inMessage = true;
        bufferPos = 0;
        pos = ReceiveMessage(packet, length, pos, linker);
        break;

      case kComStartLoad:
        // The Javelin was reset. Do nothing.
        if ( gOptions.debugComLevel >= kDebugComFunctions )
  	      StatusDebug("TSXComm::Got kComStartLoad");
        break;

      default:
        if ( (cmd&0xf0) == kComEndLoad ) {
          // The Javelin was reset. Do nothing.
          if ( gOptions.debugComLevel >= kDebugComFunctions )
  	        StatusDebug("TSXComm::Got kComEndLoad");
        }
      }
    }
  }
  catch (EError &e) {
    e.PrintError();
  }
  catch (...) {}
}

void __fastcall TSXComm::SetRunning( bool runFlag ) {
  running = runFlag;
  if ( !com )
    return;
}

void __fastcall TSXComm::QueryHeap( int rangeStart, int rangeLength, unsigned char *data, int *hp )
{
	unsigned char *packet = NULL;
  int length = rangeLength + 40;

  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::QueryHeap");

  try {

  	packet = (unsigned char*)malloc(length);
    FailNULL( packet );

    packet[0] = kComHeap;
    packet[1] = (rangeStart&0x0ff00)>>8;
    packet[2] = rangeStart&0x00ff;
    packet[3] = (rangeLength&0x0ff00)>>8;
    packet[4] = rangeLength&0x00ff;
    SendPacket(packet, 5);

  	ReadPacket(packet, &length);
    if ( length != rangeLength + 4 )
      throw EError(kIDE_ERROR,10, length);
//  	if ( !CheckChecksum(packet, length) )
//  		throw EError(kIDE_ERROR,9);
  	if ( packet[0] != (unsigned char)((unsigned char)kComHeap|kReplyBit) )
  		throw EError(kIDE_ERROR,10);

    // Set the heap packet.
    memcpy( data, &packet[3], rangeLength );

    *hp = packet[2]|(packet[1]<<8);

  	if ( packet )
  		free(packet);
  }
  catch (...) {
  	if ( packet )
  		free(packet);
    throw;
  }
}

void __fastcall TSXComm::DownloadBreakpoints( int *bpList, int count, int stepBP ) {
	unsigned char packet[256];
  int length = 256;

  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::DownloadBreakpoints(%d)", count);

  // Check that the SX is ready to accept a download.
  SendSimpleCommand(kComDownloadOK);

  packet[0] = kComBreakpoints;
  packet[1] = count;
  if ( stepBP > 0 )
    packet[1]++;
  for ( int i = 0; i < count; i++ ) {
   	packet[2 + i*2] = bpList[i]&0x00ff;
   	packet[2 + i*2 + 1] = (bpList[i]&0x0ff00)>>8;
  }
  if ( stepBP > 0 ) {
    packet[2 + count*2] = stepBP&0x00ff;
    packet[2 + count*2 + 1] = (stepBP&0x0ff00)>>8;
  }

  packet[packet[1]*2 + 2] = ComputeChecksum(packet, packet[1]*2 + 2	);

  // Send the command.
  SendPacket(packet, packet[1]*2 + 3);

    // Receive the acknowledgement.
  ReceiveAck(kComBreakpoints);
}

void __fastcall TSXComm::PutChar( char c ) {
  int result, charTime;
  char d;

  if ( gOptions.debugComLevel >= kDebugComBytes )
    StatusDebug("Transmit: $%2.2X '%c'", c, c );

  result = com->SendData((void*)&c,1);
  
  if ( result != 1 )
    throw EError(kIDE_ERROR, 7, GetLastError() );

  sentCount++;

/*
  // Read and ignore the reflected character.
  charTime = GetTickCount();
  while ( !(com->CharCount() > 0) ) {
    if ( charTime + 2000 < GetTickCount() )
      throw EError(kIDE_ERROR, 36 );
  }

  result = com->ReadChar(d);

  if ( (result == 0 && c != d) || result == 1 )
    throw EError(kIDE_ERROR, 35, d );
*/
/*
  // Purge the receive buffer strategy.
  if ( !PurgeComm( (HANDLE) com->ComHandle, PURGE_RXCLEAR ) ) {
    result = 1;
  }
*/
}

char __fastcall TSXComm::GetChar(int wait) {
  char c;
  int result;
  int charTime = GetTickCount();

  if ( !gOptions.debugIgnoreEchoes ) {
    while ( sentCount > 0 ) {
      while ( !CharReady() ) {
 	    	if ( charTime + wait < GetTickCount() )
  	    	throw EError(kIDE_ERROR, 22 );
      }
      result = com->ReadChar(c);

      if ( result != 0 )
        throw EError(kIDE_ERROR, 8, GetLastError() );

      if ( gOptions.debugComLevel >= kDebugComBytes )
        StatusDebug("Discarding echo 0x%2.2X '%c'", c, c);

      --sentCount;
    }
  }

  charTime = GetTickCount();
  while ( !CharReady() ) {
 	  if ( charTime + wait < GetTickCount() )
  	 	throw EError(kIDE_ERROR, 22 );
  }

  result = com->ReadChar(c);

  if ( result != 0 )
    throw EError(kIDE_ERROR, 8, GetLastError() );

  return c;
}

void __fastcall TSXComm::DownloadJemFile( char *jemData, int length, bool debug ) {
	unsigned char packet[19];
  int pLength = 19, packetStart = 0;
  int firstPacket = 1;

  if ( gOptions.debugComLevel >= kDebugComFunctions )
  	StatusDebug("TSXComm::DownloadJemFile(%d)", length);

  // Check that the SX is ready to accept a download.
  SendSimpleCommand(kComDownloadOK);

  if ( debug )
  	jemData[0] = kDebugMagic;
  else
  	jemData[0] = kRunMagic;

  printf("Downloading ");
  for ( int i = 0; i < ceil(length/16.0); i++ ) {

    // Only do the update 20 times.
    if ( i%(int)(ceil(length/16.0)/20) == 0 ) {
      printf(".");
      fflush(stdout);
    }

    /* TODO: This reads past the end of the jemData array. Need to handle the end case properly. */
    memcpy(&packet[2], jemData+packetStart, 16 );

    packet[0] = kComProgram;
    packet[1] = firstPacket;
    packet[16+2] = ComputeChecksum(packet, 16+2	);

    // Send the command.
    SendPacket(packet, 16+3);

    firstPacket = 0;
    packetStart += 16;

    // Programming the EEPROM may take more time than usual for the Ack.
    // Use an increased timeout.
    ReceiveAck(kComProgram, kProgramWait);
  }
  printf("\n");

}

void __fastcall TSXComm::RelaxLink(int relaxTime) {
  int startTime;

  // Pause to allow the SX to catch up.
  startTime = GetTickCount();
  while ( GetTickCount() < startTime + relaxTime )
    ;
}


