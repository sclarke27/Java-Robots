package org.SpiderBot;


import org.SpiderBot.core.*;
import org.SpiderBot.io.SerialLCD;
import org.SpiderBot.sensors.*;

import stamp.core.CPU;
import stamp.core.PWM;
import stamp.core.Uart;
import stamp.core.Terminal;
import stamp.peripheral.servo.psc.psc;

//import stamp.peripheral.wireless.eb500.eb500;

class SpiderMain {

	static final int PING_TRIG_PIN = CPU.pin14;
	static final int PING_ECHO_PIN = CPU.pin14;
	static final int LCD_PIN = CPU.pin12;
	//static final int PAN_PIN = CPU.pin13;
	//static final int TILT_PIN = CPU.pin10;
	static final int PSC_PIN = CPU.pin15;
	
	static int currentPan = 173;
	static int currentTilt = 130;
	static int panSpeed = 100;

	static Uart pscIO = new Uart(Uart.dirReceive,PSC_PIN,Uart.dontInvert,Uart.speed2400,Uart.stop1);
	static psc myPsc = new psc(pscIO,pscIO,30);

	static Uart lcdTxOut = new Uart(Uart.dirTransmit, LCD_PIN, Uart.dontInvert, Uart.speed19200,Uart.stop1);
	static SerialLCD lcdDisplay = new SerialLCD(lcdTxOut);
	static Ping pingObj = new Ping(PING_TRIG_PIN, PING_ECHO_PIN);
	//static PWM panServo = new PWM(PAN_PIN,173,2304);
	//static PWM tiltServo = new PWM(TILT_PIN,173,2304);
	
    public static void main() {
    	
    	int testLeg = 0;
    	int servoSpeed = 7;
    	Servos servos = new Servos(pscIO, myPsc);
    	
       while (true) {
    	      switch ( Terminal.getChar() ) {
				//stand up
			  case 't' :
				  servos.standUp(1);

				  break;    	      
				//stand up
				  case 'q' :
					  SerialLCD.clearLine(3);
					  SerialLCD.writeLine("Stand up", 3);
					  servos.standUp(1);

					  break;

				//sit down
				  case 'z' :
					  SerialLCD.clearLine(3);
					  SerialLCD.writeLine("sit down", 3);
					  servos.sitDown(10);

					  break;					  
					  
					//turn
				  case ',' :
					  SerialLCD.clearLine(3);
					  SerialLCD.writeLine("turn left", 3);
					  servos.turnLeft(5, 6);
					  break;					  
					  
						//turn
				  case '.' :
					  SerialLCD.clearLine(3);
					  SerialLCD.writeLine("turn right", 3);
					  servos.turnRight(5, 6);
					  break;

				//check feet
				  case 'y' :
					  SerialLCD.clearLine(3);
					  SerialLCD.writeLine("turn left", 3);
					  servos.checkFeet();
					  break;					  
					  
				// pan head left
				case 'j':
					//panServo.update(100,2304);
					break;
					
				// center pan
				case 'o':
					currentPan = 173;
					//panServo.update(173,2304);
					break;
				
				// pan head right
				case 'l':
					//panServo.update(270,2304);
					break;
				
				// tilt head up
				case 'i':
					currentTilt = currentTilt + panSpeed;
					//tiltServo.update(currentTilt,2304);
					break;
				
				// center tilt
				case 'u':
					currentTilt = 130;
					//tiltServo.update(currentTilt,2304);
					break;			          

				// tilt head down
				case 'k':
					currentTilt = currentTilt - panSpeed;
					//tiltServo.update(currentTilt,2304);
					break;
					

			//other commands
	    	      case 'r':
						SpiderActions.initSpider();
						servos.init();
						currentPan = 173;
						//panServo.update(173,2304);
						currentTilt = 130;
						//tiltServo.update(currentTilt,2304);
						
						break;
					
					case 'p':
						Ping.testPing();
						break;
						
					case 'h':
						//GaitControl.homeLegs();
						break;
					
					case '0': 
						myPsc.setAngle(0,7,1800); //move servo to far left at rate 7
						CPU.delay(10500);         //wait 1 second
						break;
					
					case 'c':
						SpiderActions.systemCheck();
						break;
					
				
    	      }    
    	      
    	    }       

    }


}