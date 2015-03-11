package org.SpiderBot;


import org.SpiderBot.core.*;
import org.SpiderBot.io.SerialLCD;

import stamp.core.CPU;
import stamp.core.PWM;
import stamp.core.Uart;
import stamp.core.Terminal;
import stamp.peripheral.servo.psc.psc;
import stamp.peripheral.wireless.eb500.eb500;

//import stamp.peripheral.wireless.eb500.eb500;

class MainTooth {

	static final int LCD_PIN = CPU.pin12;
	static final int PAN_PIN = CPU.pin13;
	static final int TILT_PIN = CPU.pin14;
	static final int PSC_PIN = CPU.pin15;
	static final int BT_STATUS_PIN = CPU.pin5;
	static final int BT_COMMAND_PIN = CPU.pin6;
	static final int BT_TX_PIN = CPU.pin0;
	static final int BT_RX_PIN = CPU.pin1;
	
	static int currentPan = 173;
	static int currentTilt = 130;
	static int panSpeed = 100;
	static int maxLift = 1800;
	static int minLift = 0;
	static int centerLift = 900;
	static int maxSwivel = 1400;
	static int minSwivel = 400;
	static int centerSwivel = 900;
	static int maxFlex = 1800;
	static int minFlex = 0;
	static int centerFlex = 900;
		
	static Uart pscIO = new Uart(Uart.dirReceive,PSC_PIN,Uart.dontInvert,Uart.speed2400,Uart.stop1);
	static psc myPsc = new psc(pscIO,pscIO,30);

	static Uart lcdTxOut = new Uart(Uart.dirTransmit, LCD_PIN, Uart.dontInvert, Uart.speed19200,Uart.stop1);
	static SerialLCD lcdDisplay = new SerialLCD(lcdTxOut);
	static PWM panServo = new PWM(PAN_PIN,173,2304);
	static PWM tiltServo = new PWM(TILT_PIN,173,2304);
	
	static int[] CURRENTLEG = Servos.LEG1;
	static Servos servos = new Servos(pscIO, myPsc);
	static int servoSpeed = 7;
	static int liftPos = 0;
	static int swivelPos = 0;
	static int footPos = 0;
	static int legNumber = 0;

	// bluetooth properties
	static Uart EB500RX = new Uart(Uart.dirReceive,BT_RX_PIN, Uart.dontInvert, Uart.speed9600, Uart.stop1);
	static Uart EB500TX = new Uart(Uart.dirTransmit,BT_TX_PIN, Uart.dontInvert, Uart.speed9600, Uart.stop1);
	static eb500 eb = new eb500(EB500RX,EB500TX,BT_STATUS_PIN,BT_COMMAND_PIN);
	static String localAddr = "00:01:02:03:04:05";
	static String remoteAddr = "00:10:60:a5:b1:17";
	static String btName = "SpiderBot";
	static String privateKey = "12345678";
	static int timeout = 30;
	
	public static void clearEB500Link()
	{
		StringBuffer messageLine1 = new StringBuffer();
	    // eb500 clear & Initialize
	    if (CPU.readPin(CPU.pin5) == true)
	    {
		   	messageLine1.append("EB500 in DATA mode, clearing.....");
			SerialLCD.clearScr();
			SerialLCD.writeLine(messageLine1.toString(),1);

	        // drive pin LOW
	        CPU.writePin(CPU.pin6,false);

	        CPU.delay(2000);

	        // disconnect
	        EB500TX.sendString("dis");
	        EB500TX.sendByte(13);

	        SerialLCD.writeLine("ok",3);
	        //System.out.println("ok");
	    }
	    else
	    {
		   	messageLine1.append("EB500 in CMD mode, clearing.....");
			SerialLCD.clearLine(1);
			SerialLCD.writeLine(messageLine1.toString(),1);

	        EB500TX.sendByte(13);
	        EB500TX.sendByte(13);

	        SerialLCD.writeLine("ok",3);
	    }
	}	
	
	public static void connectLaptop() {
//		 connect
		StringBuffer messageLine1 = new StringBuffer();
		StringBuffer dataMessage = new StringBuffer();
		
		messageLine1.append("connect to laptop");
		SerialLCD.clearLine(1);
		SerialLCD.writeLine(messageLine1.toString(),1);
		
		EB500TX.sendString("con " + remoteAddr); // address of the remote device
		EB500TX.sendByte(13);

		messageLine1.delete(0,messageLine1.length());
		messageLine1.append("command sent");
		SerialLCD.clearLine(2);
		SerialLCD.writeLine(messageLine1.toString(),2);

		CPU.delay(5000);

		SerialLCD.clearScr();
		messageLine1.delete(0,messageLine1.length());
		messageLine1.append("reading bytes:");
		SerialLCD.clearLine(1);
		SerialLCD.writeLine(messageLine1.toString(),1);

		while (EB500RX.byteAvailable() ==true)
		{
			char data = (char) EB500RX.receiveByte();
		     if (data != ' ' && data != '\r')
		     {
		    	dataMessage.append(data);
		     }
		}

		messageLine1.delete(0, messageLine1.length());
		messageLine1.append("ok");
 		SerialLCD.clearScr();
		SerialLCD.writeLine(messageLine1.toString(),1);
		SerialLCD.writeLine(dataMessage.toString(),2);
		CPU.delay(5000);
//		 wait for connection
		int waitTimeout = 100;
		int i = 0;
		while (CPU.readPin(CPU.pin5) == false && i < waitTimeout)
		{
			SerialLCD.clearScr();
			SerialLCD.writeLine("wait for con??",1);
			i = i + 1;
			CPU.delay(1000);
		    // nothing to do here.  any errors from eb500 shown previously
		}
		SerialLCD.clearScr();
		SerialLCD.writeLine("ok....",2);
		//SerialLCD.writeLine("Connected",3);
		 
		if(CPU.readPin(CPU.pin5) == true) {
	//		 while connection is OK
			while (CPU.readPin(CPU.pin5) == true)
			{
				SerialLCD.writeLine("should be connected",4);
			    // here you are connected
			    // use EB500TX.sendString("xxx") and EB500RX.receiveByte() to send and recieve as if you have a serial cable between the devices
			    // need to work out your own command/data protocol.
			    // use the stringbuffer object/class to create strings to send between devices, easy to use and converts int into string's for you!
			    // if the link drops the loop will end, and you can perform you own actions, i.e. stop the robot!!
			}
		} else {
			SerialLCD.writeLine("connection failed.",4);
		}
		
	}
	
    public static void main() {
    	
    	//init bot
    	SerialLCD.initDisplay();
    	SerialLCD.writeLine("  Bot Ready", 3);
		//servos.init();
		currentPan = 173;
		panServo.update(173,2304);
		currentTilt = 130;
		tiltServo.update(currentTilt,2304);    	
		CPU.delay(10000);
    	
    	
    	StringBuffer messageLine1 = new StringBuffer();
    	StringBuffer messageLine2 = new StringBuffer();
    	StringBuffer messageLine3 = new StringBuffer();
    	StringBuffer messageLine4 = new StringBuffer();

	   	messageLine1.append("BlueTooth Test");
		SerialLCD.clearLine(1);
		SerialLCD.writeLine(messageLine1.toString(),1);
	   
		messageLine2.append("MAC");
		//messageLine2.append(eb.sendCommand(eb.GetAddress));
		messageLine2.append(localAddr);
		SerialLCD.clearLine(2);
		SerialLCD.writeLine(messageLine2.toString(),2);

		messageLine3.append("State: ");
		//messageLine3.append(swivelPos);
		SerialLCD.clearLine(3);
		SerialLCD.writeLine(messageLine3.toString(),3);
		
		messageLine4.append("Active:  ");
		//messageLine4.append(footPos);
		SerialLCD.clearLine(4);
		SerialLCD.writeLine(messageLine4.toString(),4);    	
    	
       while (true) {
    	   /*
			messageLine1 = new StringBuffer();
	    	messageLine2 = new StringBuffer();
	    	messageLine3 = new StringBuffer();
	    	messageLine4 = new StringBuffer();    	   	
    	   */
			switch ( Terminal.getChar() ) {
				case 'c' :
					//eb.sendCommand(eb500.Connect,remoteAddr,timeout);
					SerialLCD.clearScr();
					SerialLCD.writeLine("sending command",1);
					eb.sendCommand(eb500.VersionAll);

					SerialLCD.writeLine("buffering response",2);
					while (!eb.response()) ;
				    eb.print();
				    //eb.sendCommand(eb500.Connect,remoteAddr,timeout);
				    //eb.sendCommand(eb500.GetAddress);
				    //
				    //eb.sendCommand(eb500.VersionAll);
				    //while (true) ;
				    
					//connectLaptop();
					break;
				case 'i' :
					clearEB500Link();
					eb.commandmode();
					
					break;
    	      }//end switch    
    	    }       

    }


}