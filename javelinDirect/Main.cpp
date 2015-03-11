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
#ifdef WIN32
#include <vcl.h>
#pragma hdrstop
#include <conio.h>
#include <ComDrv32.hpp>
#include "TSXComm.h"
#endif
#include "OptionVar.h"
#include "CJEMLinker.h"
#include <string.h>
#include <stdlib.h>
#include <errno.h>
//---------------------------------------------------------------------------

#ifdef WIN32
#pragma link "ComDrv32"
#pragma package(smart_init)
#endif

CJEMLinker *Linker;

void PrintHelp(void)
{
  printf("\nJavelinDirect [options] ClassName\n");
  printf("   Link and download a Javelin program from the command line.\n");
  printf("\n");
  printf("   ClassName is the name of the .class file containing the\n");
  printf("   main() method. Do not use the .class or .java suffix.\n");
  printf("\n");
  printf("   --path <path> Classpath. Multiple paths are separated by semicolons.\n");
  printf("                 Use quotes if there are spaces in the paths.\n");
#ifdef WIN32
  printf("   --port <port> Serial port number. e.g. 2 for COM2.\n");
  printf("                 The port number will be automatically chosen if not specified.\n");
  printf("   --listen      After downloading the program listen and receive any\n");
  printf("                 debug messages from the Javelin.\n");
#endif
  printf("   --help        Display this help message\n");
  printf("\n");
  printf("   e.g. JavelinDirect --path \"C:\\javelin\\lib;C:\\javelin\\Files\" BoEBot\n");
  printf("\n");
}

#ifdef WIN32

class TDataClass : public TObject {
public:
  TSXComm *jvm;
  CJEMLinker *linker;
  
  void __fastcall ReceiveData(TObject *Sender,
      Pointer DataPtr, int DataSize);
};

void __fastcall TDataClass::ReceiveData(TObject *Sender,
      Pointer DataPtr, int DataSize)
{
  unsigned char *p = (unsigned char*)DataPtr;

  // Don't throw any uncaught exceptions in this method.
  try {
    if ( gOptions.debugComLevel >= kDebugComFunctions )
      StatusDebug("Received data on serial port");

    if ( gOptions.debugComLevel >= kDebugComPackets ) {
      char s[2024];

      s[0] = '\0';
      for ( int i = 0; i < DataSize; i++ ) {
  	    char ss[200];
        sprintf( ss, "0x%2.2X ", ((unsigned char*)DataPtr)[i] );
  	    strcat(s, ss);
        if ( strlen(s) > 100 ) {
          StatusDebug("Data on serial port: %s", s);
          s[0] = '\0';
        }
      }
      StatusDebug("Data on serial port: %s", s);
    }

    if ( jvm )
      jvm->HandlePacket(p, DataSize, linker );
  }
  catch (EError e) {
    e.PrintError();
  }
  catch (...) {}
}
//---------------------------------------------------------------------------

#endif

int main(int argc, char* argv[])
{
  char *FileName = "", *FullPath = "";
  int Listen = 0;
  
  InitOptions();

#ifdef WIN32
  TStringList *Ports = new TStringList();
  // Enumerate the serial ports in order to find the list.
  try {
    EnumerateSerialPorts(Ports);

    gOptions.numPorts = Ports->Count;
    for (int i = 0; i < Ports->Count && i < MAX_NUM_PORTS; i++ )
      gOptions.validPorts[i] = Ports->Strings[i].SubString(4,Ports->Strings[i].Length()-3).ToInt();
  }
  __finally {
    delete Ports;
  }
#endif

  gOptions.debugPort = 0;
  
  if (argc == 1) {
    PrintHelp();
    exit(0);
  }

  for (int i = 1; i < argc; i++) {
    if (!strcmp(argv[i], "--path")) {
      i++;
      if (i < argc) {
        FullPath = argv[i];
      } else {
        printf("\nError: Path expected.\n\n");
        exit(1);
      }
    } else if (!strcmp(argv[i], "--port")) {
      i++;
      if (i < argc) {
        gOptions.debugPort = atoi(argv[i]);
        if (gOptions.debugPort != 0)
          --gOptions.debugPort;
      } else {
        printf("\nError: Port number expected.\n\n");
        exit(1);
      }
    } else if (!strcmp(argv[i], "--help") || !strcmp(argv[i], "-h")
      || !strcmp(argv[i], "/h") || !strcmp(argv[i], "/?")) {
      PrintHelp();
      exit(0);
    } else if (!strcmp(argv[i], "--listen")) {
      Listen = 1;
    } else {
      FileName = argv[i];
    }
  }

  try {
    Linker = new CJEMLinker();

    if ( gOptions.debugLinker )
      StatusDebug("Linking file: %s with path %s", FileName, FullPath );

    printf("Linking\n");
    Linker->LinkProgram( FileName, FullPath );

#ifdef WIN32
    printf("Finding Javelin\n");
    TCommPortDriver *Port = new TCommPortDriver(NULL);
    Port->ComPortSpeed = br28800;
    Port->PacketMode = pmPass;
    Port->EnableDTROnOpen = false;
    Port->ComPort = (TComPortNumber)gOptions.debugPort;
  	TSXComm *jvm = new TSXComm(Port);
    jvm->Open();
    
    Application->Initialize();
    TDataClass *DataClass = new TDataClass();
    DataClass->jvm = jvm;
    DataClass->linker = Linker;
    Port->OnReceiveData = DataClass->ReceiveData;

    jvm->DownloadJemFile(Linker->GetBinary(), Linker->GetBinaryLength(), false );
    printf("Download complete. Resetting Javelin.\n");
    jvm->Reset(true);

    if (Listen) {
      printf("Listening for messages\n");
      while (1) {
        Application->HandleMessage();
        if (kbhit()) {
          int c = getch();

          jvm->SendTerminalByte(c);
        }
      }
    }
    delete jvm;
#else
    /*
     * Save the .jem file.
     */
    char FileBuffer[1024];

    strcpy(FileBuffer, FileName);
    strcat(FileBuffer, ".jem");
    FILE *jemFile = fopen(FileBuffer, "wb" );
    if (!jemFile )
      throw EError(kIDE_ERROR, 17, FileBuffer, errno, _sys_errlist[errno] );
    fwrite(Linker->GetBinary(), Linker->GetBinaryLength(), 1, jemFile );
    fclose(jemFile);

#endif

  }
  catch (EError e) {
    e.PrintError(false);
  }
  return 0;
}
//---------------------------------------------------------------------------

