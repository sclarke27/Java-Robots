package stamp.peripheral.lcd;

import stamp.core.*;

/**
 * Parallax Serial LCD
 * <p>
 * @author Jon Williams, Parallax Inc.
 * @version 1.0 April 26, 2005
 */

public class SerialLCD {

  public static final int CRSR_LF     = 0x08;   // move the cursor left
  public static final int CRSR_RT     = 0x09;   // move the cursor right
  public static final int LF          = 0x0A;   // line feed
  public static final int FF          = 0x0C;   // form feed
  public static final int CR          = 0x0D;   // carriage return
  public static final int BL_On       = 0x11;   // backlight on
  public static final int BL_Off      = 0x12;   // backlight off
  public static final int LCD_Off     = 0x15;   // lcd off
  public static final int LCD_On      = 0x16;   // lcd on (no cursor or blink)

  public static final int LINE1       = 0x80;
  public static final int LINE2       = 0x94;

  private Uart lcdUart;                         // uart control object


  /**
   * Creates new serial LCD display object
   *
   * @param lcdUart TX uart object setup for LCD control
   */
  public SerialLCD(Uart lcdUart) {
    this.lcdUart = lcdUart;
  }


  /**
   * Writes character or command byte to LCD
   *
   * @param c Character or Command byte to write on LCD
   */
  public void write(int c) {
    lcdUart.sendByte(c);                        // send the character
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
   * Clears LCD; returns cursor to line 1, position 0
   */
  public void clearScr() {
    write(FF);
    CPU.delay(60);
  }


  /**
   * Moves cursor to home positon (line 1, position 0) -- DDRAM unchanged
   */
  public void home() {
    write(LINE1);
    CPU.delay(60);
  }


  /**
   * Moves LCD cursor to specified line and cursor position
   *
   * @param line Line number (LINE1 ... LINE2)
   * @param column Position on line (0 .. 15)
   */
  public void moveTo(int line, int column) {
    write(line + column);
  }


  /**
   * Sends custom character data to LCD
   *
   * @param cNum Customer character number (0 - 7)
   * @param cData[] Custom character data
   */
  public void createChar5x8(int cNum, char cData[]) {
    write(0xF8 + cNum);                         // point to character RAM
    for (int i = 0; i < 8; i++) {
      write(cData[i]);                          // download character data
    }
    write(0x80);                                // move cursor back to screen
  }


  /**
   * Displays underline cursor on LCD
   */
  public void cursorOn() {
    write(0x18);
  }


  /**
   * Removes underline cursor from LCD
   */
  public void cursorOff() {
    displayOn();
  }


  /**
   * Displays blinking [block] cursor on LCD
   */
  public void blinkOn() {
    write(0x17);
  }


  /**
   * Removes blinking cursor from LCD
   */
  public void blinkOff() {
    displayOn();
  }


  /**
   * Restores display -- cursors are removed
   */
  public void displayOn() {
    write(0x16);
  }


  /**
   * Blanks display without changing contents
   */
  public void displayOff() {
    write(0x15);
  }
}