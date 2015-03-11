/**
 * 
 */
package org.SpiderBot.io;

import stamp.core.CPU;
import stamp.core.Uart;
import stamp.peripheral.wireless.eb500.eb500;


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

	  static Uart tx = new Uart(Uart.dirTransmit,CPU.pin2,Uart.dontInvert,Uart.speed9600,Uart.stop1);
	  static Uart rx = new Uart(Uart.dirReceive,CPU.pin3,Uart.dontInvert,Uart.speed9600,Uart.stop1);

	  static eb500 eb = new eb500(rx,tx,CPU.pin0,CPU.pin1);
	  static String addr = "00:01:02:03:04:05";
	  static String name = "devicename";
	  static String key = "1234567890";
	  static int timeout = 30;
	  
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

		   eb.sendCommand(eb.Connect,addr);
		    while (!eb.response()) ;
		    eb.print();
		    eb.sendCommand(eb.Connect,addr,timeout);
		    eb.sendCommand(eb.DeleteTrustedDevice,addr);
		    eb.sendCommand(eb.DeleteTrustedAll);
		    eb.sendCommand(eb.Disconnect);
		    eb.sendCommand(eb.GetAddress);
		    eb.sendCommand(eb.GetConnectableMode);
		    eb.sendCommand(eb.GetEncryptMode);
		    eb.sendCommand(eb.GetEscapeCharacter);
		    eb.sendCommand(eb.GetFlowControl);
		    eb.sendCommand(eb.GetLinkTimeout);
		    eb.sendCommand(eb.GetName);
		    eb.sendCommand(eb.GetSecurityMode);
		    eb.sendCommand(eb.GetVisibleMode);
		    eb.sendCommand(eb.Help);
		    eb.sendCommand(eb.HelpCon);
		    eb.sendCommand(eb.HelpDel);
		    eb.sendCommand(eb.HelpDis);
		    eb.sendCommand(eb.HelpGet);
		    eb.sendCommand(eb.HelpLst);
		    eb.sendCommand(eb.HelpRst);
		    eb.sendCommand(eb.HelpSet);
		    eb.sendCommand(eb.HelpVer);
		    eb.sendCommand(eb.ListTrustedDevices);
		    eb.sendCommand(eb.ListVisibleDevices);
		    eb.sendCommand(eb.ListVisibleDevices,timeout);
		    eb.sendCommand(eb.ResetFactoryDefaults);
		    eb.sendCommand(eb.ReturnToDataMode);
		    eb.sendCommand(eb.SetBaud19200);
		    eb.sendCommand(eb.SetBaud19200Persistent);
		    eb.sendCommand(eb.SetBaud9600);
		    eb.sendCommand(eb.SetBaud9600Persistent);
		    eb.sendCommand(eb.SetConnectableModeOff);
		    eb.sendCommand(eb.SetConnectableModeOffPersistent);
		    eb.sendCommand(eb.SetConnectableModeOn);
		    eb.sendCommand(eb.SetConnectableModeOnPersistent);
		    eb.sendCommand(eb.SetEncryptModeOff);
		    eb.sendCommand(eb.SetEncryptModeOffPersistent);
		    eb.sendCommand(eb.SetEncryptModeOn);
		    eb.sendCommand(eb.SetEncryptModeOnPersistent);
		    eb.sendCommand(eb.SetEscapeCharacter,'&');
		    eb.sendCommand(eb.SetEscapeCharacterPersistent,'&');
		    eb.sendCommand(eb.SetFlowControlHardware);
		    eb.sendCommand(eb.SetFlowControlHardwarePersistent);
		    eb.sendCommand(eb.SetFlowControlNone);
		    eb.sendCommand(eb.SetFlowControlNonePersistent);
		    eb.sendCommand(eb.SetLinkTimeout,timeout);
		    eb.sendCommand(eb.SetLinkTimeoutPersistent,timeout);
		    eb.sendCommand(eb.SetName,name);
		    eb.sendCommand(eb.SetNamePersistent,name);
		    eb.sendCommand(eb.SetPasskey,key);
		    eb.sendCommand(eb.SetPasskeyPersistent,key);
		    eb.sendCommand(eb.SetSecurityModeClosed);
		    eb.sendCommand(eb.SetSecurityModeClosedPersistent);
		    eb.sendCommand(eb.SetSecurityModeOff);
		    eb.sendCommand(eb.SetSecurityModeOffPersistent);
		    eb.sendCommand(eb.SetSecurityModeOpen);
		    eb.sendCommand(eb.SetSecurityModeOpenPersistent);
		    eb.sendCommand(eb.SetVisibleModeOff);
		    eb.sendCommand(eb.SetVisibleModeOffPersistent);
		    eb.sendCommand(eb.SetVisibleModeOn);
		    eb.sendCommand(eb.SetVisibleModeOnPersistent);
		    eb.sendCommand(eb.Version);
		    eb.sendCommand(eb.VersionAll);
		    while (true) ;
	}

}
