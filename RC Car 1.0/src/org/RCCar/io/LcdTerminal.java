/*
 * Copyright © 2003-2004 Parallax Inc. All rights reserved.
 */

package stamp.peripheral.appmod;
import  stamp.core.*;


/**
 * This LcdTerminal class is for Parallax's "LCD Terminal AppMod" (part #29121)
 * <p>
 * The LCD Terminal AppMod provides an interface for direct interaction between
 * a user and the host.  It offers a 2 line by 8 character LCD display, and
 * four pushbuttons.
 * <p>
 * This class may be used in custom designs as it provides an interface to the
 * Hitachi HD44780 or compatible LCD displays using a 4-bit data bus.  Note
 * that this class is derived from Parallax's HD44780 class.
 * <p>
 *
 * @author Jon Williams, Parallax (jwilliams@parallax.com)
 * @version 1.0, 06 August 2003 (updated
 *
 */
public class LcdTerminal {

  public static final int CLR_LCD     = 0x01;   // clear the LCD
  public static final int CRSR_HOME   = 0x02;   // move the cursor home
  public static final int CRSR_LF     = 0x10;   // move the cursor left
  public static final int CRSR_RT     = 0x14;   // move the cursor right
  public static final int DISP_LF     = 0x18;   // shift display left
  public static final int DISP_RT     = 0x1C;   // shift display right

  public static final int ENTRY_MODE  = 0x04;   // entry mode set
  public static final int INC         = 0x02;   // increment DDRAM address
  public static final int DEC         = 0x00;   // decrement DDRAM address
  public static final int SHIFT_DISP  = 0x01;   // shift display

  public static final int DISP_CTRL   = 0x08;   // display on/off control
  public static final int DISP_ON     = 0x04;   // display on
  public static final int DISP_OFF    = 0x00;   // display off
  public static final int CRSR_ON     = 0x02;   // underline cursor on
  public static final int CRSR_OFF    = 0x00;   // underline cursor off
  public static final int BLINK_ON    = 0x01;   // blink cursor position
  public static final int BLINK_OFF   = 0x00;   // no blink of cursor position

  public static final int CRSR_DISP   = 0x10;   // cursor or display shift
  public static final int DISP_SHIFT  = 0x08;   // shift display
  public static final int CRSR_MOVE   = 0x00;   // move the cursor
  public static final int SHIFT_RIGHT = 0x04;
  public static final int SHIFT_LEFT  = 0x00;

  public static final int FUNC_SET    = 0x20;   // function set (interface)
  public static final int BUS_4       = 0x00;   // 4-bit bus
  public static final int LINES_2     = 0x08;   // for multi-line LCDs
  public static final int LINES_1     = 0x00;   // single-line display
  public static final int FONT_5X10   = 0x04;   // 5x10 font
  public static final int FONT_5X8    = 0x00;   // 5x8 font (standard)

  public static final int DDRAM       = 0x80;
  public static final int CGRAM       = 0x40;
  public static final int LINE1       = DDRAM + 0x00;
  public static final int LINE2       = DDRAM + 0x40;
  public static final int LINE3       = DDRAM + 0x14;
  public static final int LINE4       = DDRAM + 0x54;

  private static final int[] LINE_NUM = {LINE1, LINE2};

  public static final char DESC_G     = 0xE7;   // descended characters in
  public static final char DESC_J     = 0xEA;   //  5x10 mode
  public static final char DESC_P     = 0xF0;
  public static final char DESC_Q     = 0xF1;
  public static final char DESC_Y     = 0xF9;

  public static final char SCROLL_TM  = 2500;   // 250 ms scroll delay

  private boolean writeOnly = false;            // read and write

  private int ePin;                             // enable pin
  private int rwPin;                            // read (1) or write (0)
  private int rsPin;                            // register select
  private int db4Pin;                           // 4-bit data bus
  private int db5Pin;
  private int db6Pin;
  private int db7Pin;

  // buffer for scrolling messages
  private StringBuffer scrollMsg = new StringBuffer();


  /**
   * Creates new LCD Terminal object using AppMod connections
   */
  public LcdTerminal() {
    this.ePin = CPU.pin1;
    this.rwPin = CPU.pin2;
    this.rsPin = CPU.pin3;
    this.db4Pin = CPU.pin4;
    this.db5Pin = CPU.pin5;
    this.db6Pin = CPU.pin6;
    this.db7Pin = CPU.pin7;
    CPU.writePin(ePin, false);                  // high pulses on ePin
    initMultiLine();                            // standard init
  }


  /*
   * Sends four-bit value to bus
   */
  private void putNibble(int n) {
    CPU.writePin(db4Pin,((n & 0x01) == 0x01));  // bit 0
    CPU.writePin(db5Pin,((n & 0x02) == 0x02));  // bit 1
    CPU.writePin(db6Pin,((n & 0x04) == 0x04));  // bit 2
    CPU.writePin(db7Pin,((n & 0x08) == 0x08));  // bit 3
    CPU.pulseOut(1, ePin);
  }


  /*
   * Sends eight-bit value to bus
   */
  private void putByte(int b) {
    putNibble(b >> 4);                          // output high nibble
    putNibble(b);                               // output low nibble
  }


  /**
   * Initializes display for single-line operation
   */
  public void initSingleLine() {
    CPU.delay(150);
    CPU.writePin(rsPin, false);
    if (!writeOnly) CPU.writePin(rwPin, false);
    putNibble(0x03);                            // 8-bit interface
    CPU.delay(50);
    CPU.pulseOut(1, ePin);
    CPU.delay(5);
    CPU.pulseOut(1, ePin);
    putNibble(0x02);                            // 4-bit interface
    command(FUNC_SET | LINES_1 | FONT_5X8);
    command(DISP_CTRL | DISP_ON | CRSR_OFF | BLINK_OFF);
    command(ENTRY_MODE | INC);                  // move cursor, no display shift
    command(CLR_LCD);
  }


  /**
   * Initializes display for multi-line operation
   */
  public void initMultiLine() {
    CPU.delay(150);
    CPU.writePin(rsPin, false);
    if (!writeOnly) CPU.writePin(rwPin, false);
    putNibble(0x03);                            // 8-bit interface
    CPU.delay(50);
    CPU.pulseOut(1, ePin);
    CPU.delay(5);
    CPU.pulseOut(1, ePin);
    putNibble(0x02);                            // 4-bit interface
    command(FUNC_SET | LINES_2 | FONT_5X8);
    command(DISP_CTRL | DISP_ON | CRSR_OFF | BLINK_OFF);
    command(ENTRY_MODE | INC);                  // move cursor, no display shift
    command(CLR_LCD);
  }


  /**
   * Initializes display for 5x10 font
   */
  public void init5x10() {
    CPU.delay(150);
    CPU.writePin(rsPin, false);
    if (!writeOnly) CPU.writePin(rwPin, false);
    putNibble(0x03);                            // 8-bit interface
    CPU.delay(50);
    CPU.pulseOut(1, ePin);
    CPU.delay(5);
    CPU.pulseOut(1, ePin);
    putNibble(0x02);                            // 4-bit interface
    command(FUNC_SET | LINES_1 | FONT_5X10);
    command(DISP_CTRL | DISP_ON | CRSR_OFF | BLINK_OFF);
    command(ENTRY_MODE | INC);                  // move cursor, no display shift
    command(CLR_LCD);
  }


  /**
   * Sends command byte to LCD
   *
   * @param cmd Command to send to LCD
   */
  public void command(int cmd) {
    if (!writeOnly) CPU.writePin(rwPin, false); // set write mode
    CPU.writePin(rsPin, false);                 // command mode
    putByte(cmd);
    CPU.writePin(rsPin, true);                  // return to data mode
  }


  /**
   * Writes character on LCD at cursor position
   *
   * @param c Character to write on LCD
   */
  public void write(int c) {
    if (!writeOnly) CPU.writePin(rwPin, false); // set write mode
    CPU.writePin(rsPin, true);                  // character mode
    putByte(c);
  }


  /**
   * Writes string on LCD at cursor position
   *
   * @param s String to write on LCD
   */
  public void write(String s) {
    for (int i = 0; i < s.length(); i++)
      write(s.charAt(i));
  }


  /**
   * Writes string on LCD at cursor position
   *
   * @param sb StringBuffer to write on LCD
   */
  public void write(StringBuffer sb) {
    for (int i = 0; i < sb.length(); i++)
      write(sb.charAt(i));
  }


  /**
   * Scrolls string on LCD on designated line
   *
   * @param msg String to scroll across LCD
   */
  public void scroll(int line, String msg) {

    int lastStart;

    // pad ends of message with spaces to clear window
    scrollMsg.clear();
    scrollMsg.append("        ");
    scrollMsg.append(msg);
    scrollMsg.append("        ");

    // set last start position of substring
    lastStart = scrollMsg.length() - 8;

    // scroll the message
    for (int start = 0; start <= lastStart; start++) {
      moveTo(line, 0);
      for (int offset = 0; offset < 8; offset++) {
        write(scrollMsg.charAt(start + offset));
      }
      CPU.delay(SCROLL_TM);
    }
  }


  /**
   * Scrolls stringbuffer on LCD on designated line
   *
   * @param msg StringBuffer to scroll across LCD
   */
  public void scroll(int line, StringBuffer msg) {

    int lastStart;

    // pad ends of message with spaces to clear window
    scrollMsg.clear();
    scrollMsg.append("        ");
    // scrollMsg.append(msg.toString());
    for (int pos = 0; pos < msg.length(); pos++) {
      scrollMsg.append(msg.charAt(pos));
    }
    scrollMsg.append("        ");

    // set last start position of substring
    lastStart = scrollMsg.length() - 8;

    // scroll the message
    for (int start = 0; start <= lastStart; start++) {
      moveTo(line, 0);
      for (int offset = 0; offset < 8; offset++) {
        write(scrollMsg.charAt(start + offset));
      }
      CPU.delay(SCROLL_TM);
    }
  }


  /**
   * Scrolls integer on LCD on designated line
   *
   * @param num Integer to scroll across LCD
   */
  public void scroll(int line, int num) {

    int lastStart;

    // pad ends of message with spaces to clear window
    scrollMsg.clear();
    scrollMsg.append("        ");
    scrollMsg.append(num);
    scrollMsg.append("        ");

    // set last start position of substring
    lastStart = scrollMsg.length() - 8;

    // scroll the message
    for (int start = 0; start <= lastStart; start++) {
      moveTo(line, 0);
      for (int offset = 0; offset < 8; offset++) {
        write(scrollMsg.charAt(start + offset));
      }
      CPU.delay(SCROLL_TM);
    }
  }


  /**
   * Clears LCD; returns cursor to line 1, position 0
   */
  public void clearScr() {
    command(CLR_LCD);
    CPU.delay(20);
  }


  /**
   * Moves cursor to home positon (line 1, position 0) -- DDRAM unchanged
   */
  public void home() {
    command(CRSR_HOME);
    CPU.delay(20);
  }


  /**
   * Moves LCD cursor to specified line and cursor position
   *
   * @param line Line number (LINE1 ... LINE4)
   * @param column Position on line (0 .. 7)
   */
  public void moveTo(int line, int column) {
    command(line + column);
  }


  /**
   * Read byte from LCD at cursor position (0x00 if write-only LCD)
   *
   * @return Data at current cursor position
   */
  public int read(){

    int lcdChar = 0x00;

    if (!writeOnly) {
      // make bus pins inputs before commanding read
      CPU.setInput(db4Pin);
      CPU.setInput(db5Pin);
      CPU.setInput(db6Pin);
      CPU.setInput(db7Pin);
      // read the bus
      CPU.writePin(rwPin, true);
      CPU.writePin(rsPin, true);
      // get high nibble
      CPU.writePin(ePin, true);
      if (CPU.readPin(db4Pin)) lcdChar |= 0x10;
      if (CPU.readPin(db5Pin)) lcdChar |= 0x20;
      if (CPU.readPin(db6Pin)) lcdChar |= 0x40;
      if (CPU.readPin(db7Pin)) lcdChar |= 0x80;
      CPU.writePin(ePin, false);
      CPU.delay(1);
      // get low nibble
      CPU.writePin(ePin, true);
      if (CPU.readPin(db4Pin)) lcdChar |= 0x01;
      if (CPU.readPin(db5Pin)) lcdChar |= 0x02;
      if (CPU.readPin(db6Pin)) lcdChar |= 0x04;
      if (CPU.readPin(db7Pin)) lcdChar |= 0x08;
      CPU.writePin(ePin, false);
      CPU.writePin(rwPin, false);
    }

    return lcdChar;
  }


  /**
   * Read byte from LCD at specified address
   *
   * @param address Address to read from LCD
   * @return Data at current cursor position
   */
  public int read(int address){
    command(address);

    return read();
  }


  /**
   * Sends custom character data to LCD
   *
   * @param cNum Customer character number (0 - 7)
   * @param cData[] Custom character data
   */
  public void createChar5x7(int cNum, char cData[]) {
    command(CGRAM + (8 * cNum));                // point to character RAM
    for (int i = 0; i < 8; i++) {
      write(cData[i]);                          // download character data
    }
  }


  /**
   * Sends custom character data to LCD
   *
   * @param cNum Customer character number (0 - 3)
   * @param cData[] Custom character data
   */
  public void createChar5x10(int cNum, char cData[]) {
    command(CGRAM + (16 * cNum));               // point to character RAM
    for (int i = 0; i < 11; i++) {
      write(cData[i]);                          // download character data
    }
  }


  /**
   * Displays underline cursor on LCD
   */
  public void cursorOn() {
    command(DISP_CTRL | DISP_ON | CRSR_ON);
  }


  /**
   * Removes underline cursor from LCD
   */
  public void cursorOff() {
    command(DISP_CTRL | DISP_ON);
  }


  /**
   * Displays blinking [block] cursor on LCD
   */
  public void blinkOn() {
    command(DISP_CTRL | DISP_ON | BLINK_ON);
  }


  /**
   * Removes blinking cursor from LCD
   */
  public void blinkOff() {
    command(DISP_CTRL | DISP_ON);
  }


  /**
   * Restores display -- cursors are removed
   */
  public void displayOn() {
    command(DISP_CTRL | DISP_ON);
  }


  /**
   * Blanks display without changing contents
   */
  public void displayOff() {
    command(DISP_CTRL | DISP_OFF);
  }


  /**
   * Reads LCD Terminal AppMod buttons
   *
   * This method causes the LCD bus pins to become inputs, ensures that the
   * LCD is in Write mode (so that its bus pins remain tri-stated), then
   * debounces the buttons with a simple loop.  It is not necessary to return
   * the LCD bus pins to outputs as this will be handled by the next LCD
   * write.
   */
  public int readButtons() {

    int buttons = 0x0F;                         // assume all pressed

    // make bus pins inputs before commanding read
    CPU.setInput(db4Pin);
    CPU.setInput(db5Pin);
    CPU.setInput(db6Pin);
    CPU.setInput(db7Pin);

    if (!writeOnly) CPU.writePin(rwPin, false); // force LCD bus pins to inputs

    // read pushbuttons on shared LCD bus
    for(int i = 0; i < 5; i++) {
      // clear bit(s) if not pressed
      if (!CPU.readPin(db4Pin)) buttons &= 0x0E;
      if (!CPU.readPin(db5Pin)) buttons &= 0x0D;
      if (!CPU.readPin(db6Pin)) buttons &= 0x0B;
      if (!CPU.readPin(db7Pin)) buttons &= 0x07;
      CPU.delay(50);
    }

    return buttons;
  }
}