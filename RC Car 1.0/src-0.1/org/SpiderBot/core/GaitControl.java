package org.SpiderBot.core;

import org.SpiderBot.io.SerialLCD;

import stamp.core.CPU;
import stamp.core.Uart;

public class GaitControl {
	
	private static Uart gaitBusyPin;
	private static Uart gaitAllStopPin;
	private static Uart gaitCommandPin;
	public static int endCommand = 0x0C;

	public GaitControl(Uart gaitBusy, Uart gaitAllStop, Uart gaitCommand) {
		gaitBusyPin = gaitBusy;
		gaitAllStopPin = gaitAllStop;
		gaitCommandPin = gaitCommand;
	
	}
	
	public static void checkPicStatus() {
		int currStatus = gaitBusyPin.receiveByte();
		SerialLCD.clearScr();
		SerialLCD.writeLine("GaitControl",2);
		StringBuffer message = new StringBuffer();
		message.append("Curr PIC Status: ");
		message.append(currStatus);
		
		SerialLCD.writeLine(message.toString(),3);
	}
	
	public static void flushPic() {
		SerialLCD.clearScr();
		SerialLCD.writeLine("GaitControl",2);
		StringBuffer message = new StringBuffer();
		SerialLCD.writeLine("flushing pic",3);
		
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(0);
		gaitCommandPin.sendByte(endCommand);
		
		SerialLCD.writeLine("done.",4);
	}
	
	public static void walk(int speed, int direction, int steps) {
		SerialLCD.clearScr();
		SerialLCD.writeLine("GaitControl",2);
		SerialLCD.writeLine("walking",3);
				SerialLCD.writeLine("step",4);
				gaitCommandPin.sendString("!w");
				gaitCommandPin.sendByte(0);
				gaitCommandPin.sendByte(direction);
				gaitCommandPin.sendByte(speed);
				gaitCommandPin.sendByte(steps);
				gaitCommandPin.sendByte(endCommand);
				
		CPU.delay(5000);
	}
	
	public static void homeLegs() {
		SerialLCD.clearScr();
		SerialLCD.writeLine("GaitControl",2);
		SerialLCD.writeLine("homing legs",3);
		//if(gaitBusyPin.receiveByte() == 0) {
			SerialLCD.writeLine("testing legs",4);
			gaitCommandPin.sendString("!h");
			gaitCommandPin.sendByte(endCommand);
		//} else {
			//SerialLCD.writeLine("gait busy",3);
		//}
		CPU.delay(5000);
	}
	
	public static void rotateRight(int speed, int steps) {
		SerialLCD.clearScr();
		SerialLCD.writeLine("GaitControl",2);
		SerialLCD.writeLine("turn right",3);
			gaitCommandPin.sendString("!w 1 0 " + speed + " " + steps + " $FF");
		CPU.delay(500);
	}	

	public static void rotateLeft(int speed, int steps) {
		SerialLCD.clearScr();
		SerialLCD.writeLine("GaitControl",2);
		SerialLCD.writeLine("turn left",3);
		if(gaitBusyPin.receiveByte() == 0) {
			SerialLCD.writeLine("testing legs",3);
			gaitCommandPin.sendString("!w 1 0 " + speed + " " + steps + " $FF");
		} else {
			SerialLCD.writeLine("gait busy",3);
		}
		CPU.delay(500);
	}	
	

	public static void allStop() {
		SerialLCD.writeLine("Stop!",3);
		gaitAllStopPin.sendString("!s $FF");
		CPU.delay(500);
	}
	

}
