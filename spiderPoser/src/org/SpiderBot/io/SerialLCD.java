package org.SpiderBot.io;

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
  public static final int LINE3       = 0xA8;
  public static final int LINE4       = 0xBC;

  private static Uart lcdUart;                         // uart control object


  /**
   * Creates new serial LCD display object
   *
   * @param lcdUart TX uart object setup for LCD control
   */
  public SerialLCD(Uart tempLcdUart) {
    lcdUart = tempLcdUart;
  }

  public static void initDisplay() {
	write(SerialLCD.BL_Off);
	clearScr();
	displayOn();
	write(SerialLCD.BL_On);
	write("   SpiderPoser 1.0 ");
	moveTo(SerialLCD.LINE2, 0);
	write("      Hello!");
  }  

  /**
   * Writes character or command byte to LCD
   *
   * @param c Character or Command byte to write on LCD
   */
  public static void write(int c) {
    lcdUart.sendByte(c);                        // send the character
  }


  /**
   * Writes string on LCD at cursor position
   *
   * @param s String to write on LCD
   */
  public static void write(String s) {
    for (int i = 0; i < s.length(); i++)
      write(s.charAt(i));
  }


  /**
   * Writes string on LCD at cursor position
   *
   * @param sb StringBuffer to write on LCD
   */
  public static void write(StringBuffer sb) {
    for (int i = 0; i < sb.length(); i++)
      write(sb.charAt(i));
  }


  /**
   * Clears LCD; returns cursor to line 1, position 0
   */
  public static void clearScr() {
    write(FF);
    CPU.delay(60);
  }


  /**
   * Moves cursor to home positon (line 1, position 0) -- DDRAM unchanged
   */
  public static void home() {
    write(LINE1);
    CPU.delay(60);
  }


  /**
   * Moves LCD cursor to specified line and cursor position
   *
   * @param line Line number (LINE1 ... LINE2)
   * @param column Position on line (0 .. 15)
   */
  public static void moveTo(int line, int column) {
    write(line + column);
  }


  /**
   * Sends custom character data to LCD
   *
   * @param cNum Customer character number (0 - 7)
   * @param cData[] Custom character data
   */
  public static void createChar5x8(int cNum, char cData[]) {
    write(0xF8 + cNum);                         // point to character RAM
    for (int i = 0; i < 8; i++) {
      write(cData[i]);                          // download character data
    }
    write(0x80);                                // move cursor back to screen
  }


  /**
   * Displays underline cursor on LCD
   */
  public static void cursorOn() {
    write(0x18);
  }


  /**
   * Removes underline cursor from LCD
   */
  public static void cursorOff() {
    displayOn();
  }


  /**
   * Displays blinking [block] cursor on LCD
   */
  public static void blinkOn() {
    write(0x17);
  }


  /**
   * Removes blinking cursor from LCD
   */
  public static void blinkOff() {
    displayOn();
  }


  /**
   * Restores display -- cursors are removed
   */
  public static void displayOn() {
    write(0x16);
  }


  /**
   * Blanks display without changing contents
   */
  public static void displayOff() {
    write(0x15);
  }
  
  public static void writeLine(String message, int lineNumber) {
	int newLineNumber = SerialLCD.LINE1;
	switch(lineNumber) {
	case 1:
		newLineNumber = SerialLCD.LINE1;
		break;
	case 2:
		newLineNumber = SerialLCD.LINE2;
		break;
	case 3:
		newLineNumber = SerialLCD.LINE3;
		break;
	case 4:
		newLineNumber = SerialLCD.LINE4;
		break;
	}
	moveTo(newLineNumber, 0);
    write(message);
  }

  public static void clearLine(int lineNumber) {
		int newLineNumber = SerialLCD.LINE1;
		switch(lineNumber) {
		case 1:
			newLineNumber = SerialLCD.LINE1;
			break;
		case 2:
			newLineNumber = SerialLCD.LINE2;
			break;
		case 3:
			newLineNumber = SerialLCD.LINE3;
			break;
		case 4:
			newLineNumber = SerialLCD.LINE4;
			break;
		}
	    moveTo(newLineNumber, 0);
	    write("                   ");
  }
  
  
  public static void backlight(String lighStatus) {
	  if(lighStatus == "on") {
			  write(SerialLCD.BL_On);
		  } else {
			  write(SerialLCD.BL_Off);
		  }
	  }  
  
  
}