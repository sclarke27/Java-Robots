package org.SpiderBot.io;

import stamp.core.CPU;
import stamp.core.Uart;


public class LcdDisplay {
	  static final int LCD_PIN    = CPU.pin15;

	  static final char[] MOUTH0  = {0x0E,0x1F,0x1F,0x1F,0x1F,0x1F,0x0E,0x00};
	  static final char[] MOUTH1  = {0x0E,0x1F,0x1F,0x18,0x1F,0x1F,0x0E,0x00};
	  static final char[] MOUTH2  = {0x0E,0x1F,0x1C,0x18,0x1C,0x1F,0x0E,0x00};
	  static final char[] SMILEY  = {0x00,0x0A,0x0A,0x00,0x11,0x0E,0x06,0x00};

	  static final int[] CELLS = {2, 1, 0, 1};  // animation sequence
	  static final Uart txOut = new Uart(Uart.dirTransmit, LCD_PIN, Uart.dontInvert, Uart.speed9600,Uart.stop1);
	  static final SerialLCD lcdDisplay = new SerialLCD(txOut);

	  public LcdDisplay() {
		    

	  }
	  
	  public static void initDisplay() {
		    lcdDisplay.write(SerialLCD.BL_Off);
		    lcdDisplay.clearScr();
		    lcdDisplay.displayOn();
		    lcdDisplay.write(SerialLCD.BL_On);
		    lcdDisplay.write("   SpiderBot 1.0 ");
		    lcdDisplay.moveTo(SerialLCD.LINE2, 0);
		    lcdDisplay.write("      Hello!");
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
	    lcdDisplay.moveTo(newLineNumber, 0);
	    lcdDisplay.write(message);
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
	    lcdDisplay.moveTo(newLineNumber, 0);
	    lcdDisplay.write("                   ");
	  }
	  
	  
	  public static void backlight(String lighStatus) {
		  if(lighStatus == "on") {
			  lcdDisplay.write(SerialLCD.BL_On);
		  } else {
			  lcdDisplay.write(SerialLCD.BL_Off);
		  }
	  }
}
