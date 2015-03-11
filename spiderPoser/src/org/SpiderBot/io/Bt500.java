/**
 * 
 */
package org.SpiderBot.io;

import stamp.core.CPU;
import stamp.core.Uart;


/**
 * @author scott
 *
 */
public class Bt500 {

	/**
	 * 
	 */
	private static Uart blueToothOut = null;
	private static Uart blueToothIn = null;
	private static String currentMode = "unknown";
	
	public Bt500(Uart uartIn, Uart uartOut) {
        // drive pin LOW
		blueToothOut = uartOut;
		blueToothIn = uartIn;
		setMode(1);
	}
	
	public static void initBT() {

        // this doenst really init anything
		setMode(0);
		CPU.delay(25000);	
		SerialLCD.clearLine(4);
		SerialLCD.writeLine("Mode=" + getMode(),4);
		
	}
	
	public static void setMode(int modeType) {
		switch(modeType) {
			case 0:
				CPU.writePin(CPU.pin8,true);
		        System.out.print("EB500 set to DATA mode. ");
				break;
			case 1:
			CPU.writePin(CPU.pin8,false);
	        System.out.print("EB500 set to CMD mode. ");
				break;
		}
		
	}

	public static String getMode() {
		if(CPU.readPin(CPU.pin8)) {
			currentMode = "Data";
		} else {
			currentMode = "Command";
		}
		return currentMode;
	}
	
	public static void checkIncomingData() {
		if(blueToothIn.byteAvailable()) {
			SerialLCD.writeLine("data ready to receive", 3);
		}
	}
	
	public static void testBT500() {

       // eb500 clear & Initialize
        if (CPU.readPin(CPU.pin8) == true)
        {
            System.out.print("EB500 is alive in DATA mode, clearing.....");

            // drive pin LOW
            CPU.writePin(CPU.pin6,false);

            CPU.delay(2000);

            // disconnect
            blueToothOut.sendString("dis");
            blueToothOut.sendByte(13);

            System.out.println("ok");
        }
        else
        {
            System.out.print("EB500 is alive in CMD mode, clearing.....");

            blueToothOut.sendByte(13);
            blueToothOut.sendByte(13);

            System.out.println("ok");
        }

        blueToothOut.sendString("get addr");
        blueToothOut.sendByte(13); // return
         System.out.println("byte sent");
        // wait a tick
        CPU.delay(2500);

        while (blueToothOut.byteAvailable() == true)
        {
            System.out.print((char) blueToothOut.receiveByte());
        }

	}

}
