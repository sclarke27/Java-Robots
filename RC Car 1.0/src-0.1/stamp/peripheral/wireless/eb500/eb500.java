package stamp.peripheral.wireless.eb500;
import stamp.core.*;
import stamp.util.text.*;

/**
 * The EmbeddedBlue command set is comprised of visible ASCII characters. Therefore, a
 * command can be issued from a terminal application, such as HyperTerminal, or directly from
 * a custom application program, written in a programming language such as C++ or Visual
 * Basic, running on a PC, using the eb600 PC adapter. From a BASIC Stamp application,
 * these commands can be issued by using the PBASIC™ SERIN and SEROUT commands.
 *
 * Command Basics
 * Commands may only be sent to the module when it is in Command Mode. White spaces are
 * used to separate parameters of the command and a carriage-return is used to mark the end
 * of the command. Upon receipt of a command the eb500 begins to parse the parameters. If
 * the syntax of the command is correct the eb500 returns an ACK string, not the ACK character
 * (0x06); otherwise, a NAK string is returned. Following the ACK or NAK string is a carriagereturn
 * (0x0D) character. If an error occurs while processing the command an error string is
 * returned followed by a carriage-return followed by the prompt (>) character. If the command
 * executed successfully the module will issue the prompt (>) character. Please see the Error
 * Codes section for a description of the error codes.
 * The following example shows the basic structure of a command. A prompt (>) is issued by
 * the EmbeddedBlue module. A command followed by a carriage-return is sent to the module.
 * The module responds with either an ACK or NAK string followed by a carriage-return. If an
 * error occurs, the module responds with an Err string followed by a space followed by an
 * ASCII string numeric value followed by a carriage-return. A prompt (>) is then issued by the
 * module.
 * >command<CR>
 * ACK | NAK<CR>
 * Err number<CR>
 * >
 *
 * Command Error Handling in BASIC Stamp Applications
 * The BASIC Stamp has a software based UART; meaning it does not buffer incoming serial
 * data. Therefore, the checking of errors from the issuing of an eb500 command must be
 * performed immediately after the issuing of the command; otherwise, the data may be lost.
 * Below is a sample of BASIC Stamp code that issues an eb500 Connect command, waits for
 * the ACK<CR> response from the eb500, then waits for the error string or the prompt (>) to be
 * returned from the eb500. It then checks the first bye of the data returned to determine if an
 * error has occurred. If an error has occurred, the code jumps to the error handler code, where
 * an error string along with the error number is shown in the debug window of the Basic Stamp
 * Editor.
 * 'Connect to remote Bluetooth device
 * SEROUT 1,84,["con 00:0C:84:00:07:D8",CR]
 * SERIN 0,84,[WAIT(“ACK”,CR)]
 * ‘Either an Err #<CR> or a ">" will be received
 * SERIN 0,84,[STR bBuffer\6\”>”]
 * IF bBuffer(0) = “E” THEN ErrorCode
 * … Progam Logic …
 * ErrorCode:
 * bErrorCode = bBuffer(4)
 * DEBUG “Error: “,STR bErrorCode,CR
 * END
 */

public class eb500 {

  static final int CR = 0x0D;
  public static final int DISCONNECTED = 0;
  public static final int DATA = 1;
  public static final int COMMAND = 2;

  private Uart rx;
  private Uart tx;
  private int statusPin;
  private int cmdPin;
  private char[] sendbuf = new char[128];
  private char[] recvbuf = new char[128];
  private int mode = 0; //0=disconnected, 1=data, 2=command
  private int rindex = 0; //index pointer for recvbuf
  private char esc = '+';

  static final String[] errorText = {"",
                        "General connection failure",
                        "Connection attempt failed",
                        "Command not valid while active",
                        "Command only valid while active",
                        "An unexpected request occured",
                        "Connection attempt failed due to timeout",
                        "Connection attempt was refused by the remote device",
                        "Connection attempt failed because the remote device does not support the Serial Port Profile",
                        "An unexpected error occurred when deleting trusted devices",
                        "Unable to add a new trusted device",
                        "Trusted device not found",
                        "Command not valid during startup"};

  /*
     command constant is 16bit value consisting of 4 nibbles
     nib3 = flags b15=persistent, b14-b12=reserved
     nib2 = optional second parameter
     nib1 = first parameter
     nib0 = command
  */
  public static final int Connect                          = (short)0x0001; //string,[int]
  public static final int DeleteTrustedDevice              = (short)0x00D2; //string
  public static final int DeleteTrustedAll                 = (short)0x01D2;
  public static final int Disconnect                       = (short)0x0003;
  public static final int GetAddress                       = (short)0x0014;
  public static final int GetConnectableMode               = (short)0x0044;
  public static final int GetEncryptMode                   = (short)0x0054;
  public static final int GetEscapeCharacter               = (short)0x0064;
  public static final int GetFlowControl                   = (short)0x0084;
  public static final int GetLinkTimeout                   = (short)0x0094;
  public static final int GetName                          = (short)0x00A4;
  public static final int GetSecurityMode                  = (short)0x00C4;
  public static final int GetVisibleMode                   = (short)0x00E4;
  public static final int Help                             = (short)0x0005;
  public static final int HelpCon                          = (short)0x0305;
  public static final int HelpDel                          = (short)0x0405;
  public static final int HelpDis                          = (short)0x0505;
  public static final int HelpGet                          = (short)0x0605;
  public static final int HelpLst                          = (short)0x0805;
  public static final int HelpRst                          = (short)0x0D05;
  public static final int HelpSet                          = (short)0x0E05;
  public static final int HelpVer                          = (short)0x0F05;
  public static final int ListTrustedDevices               = (short)0x00D6;
  public static final int ListVisibleDevices               = (short)0x00E6; //[int]
  public static final int ResetFactoryDefaults             = (short)0x0077;
  public static final int ReturnToDataMode                 = (short)0x0008;
  public static final int SetBaud19200                     = (short)0x0029;
  public static final int SetBaud19200Persistent           = (short)0x8029;
  public static final int SetBaud9600                      = (short)0x0039;
  public static final int SetBaud9600Persistent            = (short)0x8039;
  public static final int SetConnectableModeOff            = (short)0x0A49;
  public static final int SetConnectableModeOffPersistent  = (short)0x8A49;
  public static final int SetConnectableModeOn             = (short)0x0B49;
  public static final int SetConnectableModeOnPersistent   = (short)0x8B49;
  public static final int SetEncryptModeOff                = (short)0x0A59;
  public static final int SetEncryptModeOffPersistent      = (short)0x8A59;
  public static final int SetEncryptModeOn                 = (short)0x0B59;
  public static final int SetEncryptModeOnPersistent       = (short)0x8B59;
  public static final int SetEscapeCharacter               = (short)0x0069; //int
  public static final int SetEscapeCharacterPersistent     = (short)0x8069; //int
  public static final int SetFlowControlHardware           = (short)0x0789;
  public static final int SetFlowControlHardwarePersistent = (short)0x8789;
  public static final int SetFlowControlNone               = (short)0x0989;
  public static final int SetFlowControlNonePersistent     = (short)0x8989;
  public static final int SetLinkTimeout                   = (short)0x0099; //int
  public static final int SetLinkTimeoutPersistent         = (short)0x8099; //int
  public static final int SetName                          = (short)0x00A9; //string
  public static final int SetNamePersistent                = (short)0x80A9; //string
  public static final int SetPasskey                       = (short)0x00B9; //string
  public static final int SetPasskeyPersistent             = (short)0x80B9; //string
  public static final int SetSecurityModeClosed            = (short)0x02C9;
  public static final int SetSecurityModeClosedPersistent  = (short)0x82C9;
  public static final int SetSecurityModeOff               = (short)0x0AC9;
  public static final int SetSecurityModeOffPersistent     = (short)0x8AC9;
  public static final int SetSecurityModeOpen              = (short)0x0CC9;
  public static final int SetSecurityModeOpenPersistent    = (short)0x8CC9;
  public static final int SetVisibleModeOff                = (short)0x0AE9;
  public static final int SetVisibleModeOffPersistent      = (short)0x8AE9;
  public static final int SetVisibleModeOn                 = (short)0x0BE9;
  public static final int SetVisibleModeOnPersistent       = (short)0x8BE9;
  public static final int Version                          = (short)0x000A;
  public static final int VersionAll                       = (short)0x010A;

  static final int[] nib0 = {0,  1,  5,  9, 13, 17, 21, 25, 29, 33, 37,  0,  0,  0,  0,  0};
  static final int[] nib1 = {0, 41, 50, 62, 73, 86, 95,104,113,119,132,138,147,157,166,  0};
  static final int[] nib2 = {0,175,180,188,193,198,203,208,218,223,229,234,238,244,249,254};

  static final char[] text = { 0                                                  //0
                              //nib0
                              ,'c','o','n',0                                      //1
                              ,'d','e','l',0                                      //5
                              ,'d','i','s',0                                      //9
                              ,'g','e','t',0                                      //13
                              ,'h','l','p',0                                      //17
                              ,'l','s','t',0                                      //21
                              ,'r','s','t',0                                      //25
                              ,'r','e','t',0                                      //29
                              ,'s','e','t',0                                      //33
                              ,'v','e','r',0                                      //37
                              //nib1
                              ,' ','a','d','d','r','e','s','s',0                  //41
                              ,' ','b','a','u','d',' ','1','9','2','0','0',0      //50
                              ,' ','b','a','u','d',' ','9','6','0','0',0          //62
                              ,' ','c','o','n','n','e','c','t','a','b','l','e',0  //73
                              ,' ','e','n','c','r','y','p','t',0                  //86
                              ,' ','e','s','c','c','h','a','r',0                  //95
                              ,' ','f','a','c','t','o','r','y',0                  //104
                              ,' ','f','l','o','w',0                              //113
                              ,' ','l','i','n','k','t','i','m','e','o','u','t',0  //119
                              ,' ','n','a','m','e',0                              //132
                              ,' ','p','a','s','s','k','e','y',0                  //138
                              ,' ','s','e','c','u','r','i','t','y',0              //147
                              ,' ','t','r','u','s','t','e','d',0                  //157
                              ,' ','v','i','s','i','b','l','e',0                  //166
                              //nib2
                              ,' ','a','l','l',0                                  //175
                              ,' ','c','l','o','s','e','d',0                      //180
                              ,' ','c','o','n',0                                  //188
                              ,' ','d','e','l',0                                  //193
                              ,' ','d','i','s',0                                  //198
                              ,' ','g','e','t',0                                  //203
                              ,' ','h','a','r','d','w','a','r','e',0              //208
                              ,' ','l','s','t',0                                  //218
                              ,' ','n','o','n','e',0                              //223
                              ,' ','o','f','f',0                                  //229
                              ,' ','o','n',0                                      //234
                              ,' ','o','p','e','n',0                              //238
                              ,' ','r','s','t',0                                  //244
                              ,' ','s','e','t',0                                  //249
                              ,' ','v','e','r',0                                  //254
                             };

  public eb500(Uart rx, Uart tx, int statusPin, int cmdPin) {
  }

  private int send(int k) {
    int i=0;
    //if (sendbuf[k]!=0) return -1;
    //Format.printf("%s\n",sendbuf);
    while (i!=k) {
     tx.sendByte(sendbuf[i]);
     i=i+1;
    }
    return k;
  }

  private int assembleNibble(int k, int index) {
    while (text[index] != 0) {
      sendbuf[k++] = text[index++];
    }
    return k;
  }

  private int assembleStart(int command) {
    int k=0, index;
    index = nib0[command & 0x000F];
    k = assembleNibble(k,index);
    index = nib1[(command >>> 4) & 0x000F];
    k = assembleNibble(k,index);
    index = nib2[(command >>> 8) & 0x000F];
    k = assembleNibble(k,index);
    return k;
  }

  private int assembleFinish(int command, int k) {
    if (command < 0) {
      sendbuf[k++] = ' ';
      sendbuf[k++] = '*';
    }
    sendbuf[k++] = CR;
    sendbuf[k] = 0;
    return k;
  }

  public void commandmode() {
    if (mode == DATA) {
      CPU.delay(21000); //2 second pause
      tx.sendByte(esc); //3 consecutive esc characters
      tx.sendByte(esc);
      tx.sendByte(esc);
      CPU.delay(21000); //2 second pause
      mode = COMMAND;
    }
  }

  private void enterDataMode() {
    if (mode == COMMAND) {
      sendCommand(ReturnToDataMode);
    }
  }

  /**
   * Send command to eb500
   *
   * @param command One of the following public commands:
   *                del - DeleteTrustedAll
   *                dis - Disconnect
   *                get - GetAddress, GetConnectableMode, GetEncryptMode, GetEscapeCharacter,
   *                      GetFlowControl, GetLinkTimeout, GetName, GetSecurityMode, GetVisibleMode
   *                hlp - Help, HelpCon, HelpDel, HelpDis, HelpGet, HelpLst, HelpRst, HelpSet, HelpVer
   *                lst - ListTrustedDevices, ListVisibleDevices
   *                rst - ResetFactoryDefaults
   *                ret - ReturnToDataMode
   *                set - SetBaud19200, SetBaud19200Persistent, SetBaud9600, SetBaud9600Persistent,
   *                      SetConnectableModeOff, SetConnectableModeOffPersistent, SetConnectableModeOn, SetConnectableModeOnPersistent,
   *                      SetEncryptModeOff, SetEncryptModeOffPersistent, SetEncryptModeOn, SetEncryptModeOnPersistent,
   *                      SetFlowControlHardware, SetFlowControlHardwarePersistent, SetFlowControlNone, SetFlowControlNonePersistent,
   *                      SetSecurityModeClosed, SetSecurityModeClosedPersistent, SetSecurityModeOff,
   *                      SetSecurityModeOffPersistent, SetSecurityModeOpen, SetSecurityModeOpenPersistent,
   *                      SetVisibleModeOff, SetVisibleModeOffPersistent, SetVisibleModeOn, SetVisibleModeOnPersistent
   *                ver - Version, VersionAll
   * @return Number of bytes sent
   */
  public int sendCommand(int command) {
    int mode = this.mode;
    commandmode();
    int k = assembleStart(command);
    k = assembleFinish(command,k);
    if (command == ReturnToDataMode) this.mode = DATA;
    return send(k);
  }

  /**
   * Send command to eb500
   *
   * @param command One of the following public commands:
   *                ListVisibleDevices           - sendCommand(ListVisibleDevices,30)
   *                SetLinkTimeout               - sendCommand(SetLinkTimeout,30)
   *                SetLinkTimeoutPersistent     - sendCommand(SetLinkTimeoutPersistent,30)
   * @param num Numeric command parameter
   * @return Number of bytes sent
   */
  public int sendCommand(int command, int num) {
    int k = assembleStart(command);
    k = Format.bprintf(sendbuf,k," %d",num);
    k = assembleFinish(command,k);
    return send(k);
  }

  /**
   * Send command to eb500
   *
   * @param command One of the following public commands:
   *                SetEscapeCharacter           - sendCommand(SetEscapeCharacter,'&')
   *                SetEscapeCharacterPersistent - sendCommand(SetEscapeCharacterPersistent,'&')
   * @param chr Ascii printable character
   * @return Number of bytes sent
   */
  public int sendCommand(int command, char chr) {
    int k = assembleStart(command);
    k = Format.bprintf(sendbuf,k," %c",chr);
    k = assembleFinish(command,k);
    esc = chr; //remember esc character
    return send(k);
  }

  /**
   * Send command to eb500
   *
   * @param command One of the following public commands:
   *                Connect              - sendCommand(Connect,"00:0C:84:00:05:29")
   *                DeleteTrustedDevice  - sendCommand(DeleteTrustedDevice,"00:0C:84:00:05:29")
   *                SetName              - sendCommand(SetName,"MyDeviceName")
   *                SetNamePersistent    - sendCommand(SetNamePersistent,"MyDeviceName")
   *                SetPasskey           - sendCommand(SetPasskey,"1234567890")
   *                SetPasskeyPersistent - sendCommand(SetPasskeyPersistent,"1234567890")
   * @param txt Ascii string command parameter
   * @return Number of bytes sent
   */
  public int sendCommand(int command, String txt) {
    int k = assembleStart(command);
    k = Format.bprintf(sendbuf,k," %s",txt);
    k = assembleFinish(command,k);
    return send(k);
  }

  /**
   * Send command to eb500
   *
   * @param command One of the following public commands:
   *                Connect - sendCommand(Connect,"00:0C:84:00:05:29",30)
   * @param txt Ascii string command parameter
   * @param num Numeric command parameter
   * @return Number of bytes sent
   */
  public int sendCommand(int command, String txt, int num) {
    int k = assembleStart(command);
    k = Format.bprintf(sendbuf,k," %s",txt);
    k = Format.bprintf(sendbuf,k," %d",num);
    k = assembleFinish(command,k);
    return send(k);
  }

  /**
   * Check for response bytes.
   * If complete response is received (eg. the > prompt)
   * then true is returned, otherwise false.
   * The received bytes are stored in a receive buffer for parsing.
   * Received CR characters are replaced by newlines.
   *
   * @return True if > prompt received, otherwise false
   */
  public boolean response() {
    int c;
    while (rx.byteAvailable()) {
      c = rx.receiveByte();
      if (c == '\r') c = '\n'; //replace CR by newline
      recvbuf[rindex++] = (char)c;
      if (c == '>') {
        recvbuf[rindex] = 0;
        rindex = 0;
        return true;
      }
    }
    return false;
  }

  /**
   * Print the response to the IDE message window
   */
  public void print() {
    Format.printf("%s\n",recvbuf);
  }

/*
  The following example uses the Switch to Command Mode and Return to Data Mode serial commands
  to switch between data mode and command mode.

  char[] szPortFData = new char[32];
  memset(szPortFData, 0, sizeof(szPortFData));
  // Connect to the remote device
  sendString("con 00:0C:84:00:05:29\r");
  waitForString("ACK\r", 0);
  // Wait for the connection to be established
  while(!BitRdPortI(PDDR, 4));
  printf("Connection established\n");
  serFputs("This string is sent in data mode\r");
  // Switch to Command Mode
  a7_pauseMs(2000);
  serFputs("+++");
  a7_serFwaitForString("\r>", 0);
  printf("In Command Mode\n");
  // Get local eb506 Bluetooth Address
  serFputs("get address\r");
  a7_serFwaitForString("ACK\r", 0);
  // Read local address from get command
  while(serFpeek() == -1) {}
  serFread(szPortFData, 17, 10);
  szPortFData[17] = '\r';
  printf("Local eb506 address: %s\n", szPortFData);
  a7_serFwaitForString("\r>", 0);
  // Return to data mode
  The Basics
  Page 16 . EmbeddedBlue 506 User Manual
  serFputs("ret\r");
  a7_serFwaitForString("\r>", 0);
  // Send data through eb506
  serFputs("My eb506 address is ");
  serFputs(szPortFData);
  // Switch to Command Mode
  a7_pauseMs(2000);
  serFputs("+++");
  a7_serFwaitForString("\r>", 0);
  printf("In Command Mode\n");
  // Disconnect from remote device
  serFputs("dis\r");
  a7_serFwaitForString("\r>", 0);
  printf("Disconnected\n");
  serFclose();
*/

}