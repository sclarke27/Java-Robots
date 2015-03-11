package org.SpiderBot;


import org.SpiderBot.core.*;
import org.SpiderBot.io.SerialLCD;
import org.SpiderBot.sensors.*;

import stamp.core.CPU;
import stamp.core.PWM;
import stamp.core.Uart;
import stamp.core.Terminal;

//import stamp.peripheral.wireless.eb500.eb500;

class SpiderMain {

	static final int GAIT_BUSY_PIN = CPU.pin8;
	static final int GAIT_STOP_PIN = CPU.pin9;
	static final int GAIT_COMMAND_PIN = CPU.pin12;
	static final int PING_TRIG_PIN = CPU.pin15;
	static final int PING_ECHO_PIN = CPU.pin15;
	static final int LCD_PIN = CPU.pin12;
	static final int PAN_PIN = CPU.pin13;
	static final int TILT_PIN = CPU.pin14;
	static int currentPan = 173;
	static int currentTilt = 130;
	static int panSpeed = 5;

	static final Uart lcdTxOut = new Uart(Uart.dirTransmit, LCD_PIN, Uart.dontInvert, Uart.speed19200,Uart.stop1);
	static final Uart gaitBusy = new Uart(Uart.dirReceive,GAIT_BUSY_PIN, Uart.dontInvert, Uart.speed38400, Uart.stop1);
	static final Uart gaitAllStop = new Uart(Uart.dirTransmit,GAIT_STOP_PIN, Uart.dontInvert, Uart.speed38400, Uart.stop1);
	static final Uart gaitCommand = new Uart(Uart.dirTransmit,GAIT_COMMAND_PIN, Uart.dontInvert, Uart.speed38400, Uart.stop1);
	
	static final SerialLCD lcdDisplay = new SerialLCD(lcdTxOut);
	static final GaitControl gait = new GaitControl(gaitBusy, gaitAllStop, gaitCommand);
	static final Ping pingObj = new Ping(PING_TRIG_PIN, PING_ECHO_PIN);
	static PWM panServo = new PWM(PAN_PIN,173,2304);
	static PWM tiltServo = new PWM(TILT_PIN,173,2304);

	
    public static void main() {
       while (true) {
    	      switch ( Terminal.getChar() ) {
				case 'i':
				SpiderActions.initSpider();
				break;
				
				case 'p':
				Ping.testPing();
				break;
				
				case 'h':
				GaitControl.homeLegs();
				break;
				
				case 'w':
				GaitControl.walk(1, 0, 20);
				break;
				
				case 'l':
				GaitControl.rotateLeft(1, 20);
				break;
				
				case 'r':
				GaitControl.rotateRight(1, 20);
				break;
				  
				case 'c':
				SpiderActions.systemCheck();
				break;
				
				case 'g':
					GaitControl.checkPicStatus();
					break;				
				  
				case 'f':
					GaitControl.flushPic();
					break;				

				case '1':
					panServo.update(110,2304);
					break;

				case '2':
					currentPan = 173;
					panServo.update(193,2304);
					break;
				
				case '3':
					panServo.update(220,2304);
					break;
				
				  
				case '4':
					currentTilt = currentTilt + panSpeed;
					tiltServo.update(currentTilt,2304);
					break;
				
				case '5':
					currentTilt = 130;
					tiltServo.update(currentTilt,2304);
					break;			          

				case '6':
					currentTilt = currentTilt - panSpeed;
					tiltServo.update(currentTilt,2304);
					break;
					
				
    	      }    
    	      
    	    }       

    }


}