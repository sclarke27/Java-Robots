package org.SpiderBot.core;

import stamp.core.EEPROM;

import org.SpiderBot.io.*;
import stamp.core.CPU;
import stamp.peripheral.servo.psc.psc;

public class SpiderActions {
	
	public static void runSpider() {
		
		SerialLCD.clearLine(2);
		SerialLCD.clearLine(3);
		SerialLCD.clearLine(4);
		SerialLCD.writeLine("    Bot Running", 3);
		

    	
    }
    
    public static void initSpider() {
    	SerialLCD.initDisplay();
    	SerialLCD.writeLine("  Initializing Bot", 3);
    	//SerialLCD.writeLine(" Starting BlooTooth", 4);
    	//Bt500.initBT();
    	
    }
  
    public static void systemCheck() {
    	SerialLCD.initDisplay();
    	SerialLCD.writeLine("start sys check", 1);
    	int totalMem = EEPROM.size();
    	StringBuffer messageStr = new StringBuffer();
    	messageStr.append("EEPROM Size: ");
    	messageStr.append(totalMem);
    	SerialLCD.clearLine(1);
    	SerialLCD.moveTo(2, 0);
    	SerialLCD.write(messageStr);
    	//Bt500.initBT();
    	messageStr = new StringBuffer();
    	messageStr.append("EEPROM Used: ");
    	messageStr.append(getEEProm());
    	SerialLCD.moveTo(3, 0);
    	SerialLCD.write(messageStr);
    	
    }
    
    public static int getEEProm() {
	    int x;
	    x=EEPROM.read(1);
	    x=((x<<8)+EEPROM.read(0));
	    return x;
    }    


}
