package org.SpiderBot;



import org.SpiderBot.core.*;
import org.SpiderBot.io.SerialLCD;
import org.SpiderBot.sensors.Compass;
import org.SpiderBot.sensors.Ping;

import stamp.core.CPU;
import stamp.core.PWM;
import stamp.core.Uart;
import stamp.core.Terminal;
import stamp.peripheral.servo.psc.psc;
import stamp.peripheral.devantech.*;

//import stamp.peripheral.wireless.eb500.eb500;

class PoserMain {

	static final int LCD_PIN = CPU.pin12;
	static final int PSC_PIN = CPU.pin9;
	static final int PING_TRIG_PIN = CPU.pin14;
	static final int PING_ECHO_PIN = CPU.pin14;
	static final int pulsePin1 = CPU.pin13;
	static final int pulsePin2 = CPU.pin15;
	static final int compassDinDout = CPU.pin1;
	static final int compassClk = CPU.pin2;
	static final int compassEn = CPU.pin3;
	
	static int xCenter = 2121;
	static int yCenter = 2112;

	static int centerPan = 900;
	static int centerTilt = 400;
	static int currentPan = centerPan;
	static int currentTilt = centerTilt;
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
	static int channels = 30;
	static int currentSwivel = centerSwivel;
	static int currentLift = centerLift;
	static int currentFlex = centerFlex;
	static boolean monitorRC = false;	
	
	static Compass compass = new Compass(compassDinDout,compassClk,compassEn);
	
	static Uart pscIO = new Uart(Uart.dirTransmit,PSC_PIN,Uart.dontInvert,Uart.speed2400,Uart.stop2);
	static psc myPsc = new psc(pscIO,pscIO,30,PSC_PIN);
	static Ping pingObj = new Ping(PING_TRIG_PIN, PING_ECHO_PIN);
	static Uart lcdTxOut = new Uart(Uart.dirTransmit, LCD_PIN, Uart.dontInvert, Uart.speed19200,Uart.stop1);
	static SerialLCD lcdDisplay = new SerialLCD(lcdTxOut);
	static int panServo = 14;
	static int tiltServo = 15;
	
	static int[] CURRENTLEG = Servos.LEG1;
	static Servos servos = new Servos(pscIO, myPsc);
	static int servoSpeed = 7;
	static int liftPos = 0;
	static int swivelPos = 0;
	static int footPos = 0;
	static int legNumber = 1;
	static int iterations = 0;
	static int compassTimeout = 100;
	static int i;
	static StringBuffer messageLine1 = new StringBuffer();
	static StringBuffer messageLine2 = new StringBuffer();
	static StringBuffer messageLine3 = new StringBuffer();
	static StringBuffer messageLine4 = new StringBuffer();
	static int objectDistance = 10000;
	static int nearestObj = 0; 
	static int nearestObjDist = 10000; 
	
    public static void main() {
    	
    	//init bot
    	SerialLCD.initDisplay();
    	SerialLCD.clearScr();
    	SerialLCD.writeLine("Init PSC...", 1);
    	myPsc.initPsc(pscIO);
    	SerialLCD.writeLine("Init Servos...", 2);
		servos.init(channels);
    	SerialLCD.writeLine("Init Compass...", 3);
    	compass.reset();
		currentPan = 200;
		myPsc.setAngle(panServo,0,currentPan);
		currentTilt = 200;
		myPsc.setAngle(tiltServo,0,currentTilt);
		SerialLCD.writeLine("Gizmo2 Ready", 4);
		myPsc.setAngle(panServo,15,900);
		myPsc.setAngle(tiltServo,15,575);
		Servos.standUp();
		CPU.delay(20000);
    	
    	
/*
	   	messageLine1.append("Leg: ");
		messageLine1.append(legNumber);
		messageLine1.append(" Speed: ");
		messageLine1.append(panSpeed);
		SerialLCD.clearLine(1);
		SerialLCD.writeLine(messageLine1.toString(),1);
	   
		messageLine2.append("Lift:  ");
		messageLine2.append(liftPos);
		SerialLCD.clearLine(2);
		SerialLCD.writeLine(messageLine2.toString(),2);

		messageLine3.append("Swivel: ");
		messageLine3.append(swivelPos);
		SerialLCD.clearLine(3);
		SerialLCD.writeLine(messageLine3.toString(),3);
		
		messageLine4.append("Flex:  ");
		messageLine4.append(footPos);
		SerialLCD.clearLine(4);
		SerialLCD.writeLine(messageLine4.toString(),4);    	
*/
	   int xAxis,yAxis;  
		 
       while (true) {


    	   if(Terminal.byteAvailable()) {
	      
	      switch ( Terminal.getChar() ) {
	      
	      case 't':
	    	  myPsc.setAngle(tiltServo,0,50);
	    	  myPsc.setAngle(panServo,0,900);
	    	  CPU.delay(3000);
		      messageLine1.delete(0,messageLine1.length());
		      messageLine1.append("dist from floor= ");
		      messageLine1.append(pingObj.getObjDistance('m'));
		      SerialLCD.clearLine(1);
		      SerialLCD.writeLine(messageLine1.toString(), 1);
	    	  break;
	      
	      case 'y':
	    	  myPsc.setAngle(tiltServo,3,575);
    		  nearestObjDist = 1000;
	    	  for(i=40; i<=160; i=i+3) {
	    		  myPsc.setAngle(panServo,3,i*10);
	    		  objectDistance = pingObj.getObjDistance('r');
	    		  if(objectDistance <= nearestObjDist) {
	    			  nearestObj = i*10;
	    			  nearestObjDist = objectDistance;
	    		  }
	    		  
	    	  }
	    	  for(i=160; i>=40; i=i-3) {
	    		  myPsc.setAngle(panServo,3,i*10);
	    		  objectDistance = pingObj.getObjDistance('r');
	    		  if(objectDistance <= nearestObjDist) {
	    			  nearestObj = i*10;
	    			  nearestObjDist = objectDistance;
	    		  }
	    		  
	    	  }
	    	  
	    	  myPsc.setAngle(panServo,4,nearestObj);
	    	  CPU.delay(10000);
	    	  break;
			  //increase speed
			  case '0' :
				panSpeed = panSpeed + 100;
				if(panSpeed >= 1000) {
					panSpeed = 0;
				}
				break;

				  //decrease speed
			  case '9' :
				panSpeed = panSpeed - 100;
				if(panSpeed <= 0) {
					panSpeed = 0;
				}
				break;

			//leg up
			  case 'w' :
				currentLift = servos.getAngle(CURRENTLEG[0]);
				currentLift = currentLift + panSpeed;
				if(currentLift > maxLift) {
					currentLift = maxLift;
				}
			    servos.liftLeg(CURRENTLEG,servoSpeed,currentLift);
				break;


			  //leg down
			  case 's' :
				currentLift = servos.getAngle(CURRENTLEG[0]);
				currentLift = currentLift - panSpeed;
				if(currentLift > maxLift) {
					currentLift = maxLift;
				}
			    servos.liftLeg(CURRENTLEG,servoSpeed,currentLift);
				break;
				  
			  //swing leg left
			  case 'a' :
				currentSwivel = servos.getAngle(CURRENTLEG[1]);
				currentSwivel = currentSwivel + panSpeed;
				if(currentSwivel > maxLift) {
					currentSwivel = maxLift;
				}
				servos.swingLeg(CURRENTLEG,servoSpeed,currentSwivel);
				break;
			  
			  //swing leg right
			  case 'd' :
				currentSwivel = servos.getAngle(CURRENTLEG[1]);
				currentSwivel = currentSwivel - panSpeed;
				if(currentSwivel > maxLift) {
					currentSwivel = maxLift;
				}
				servos.swingLeg(CURRENTLEG,servoSpeed,currentSwivel);
				break;

			  //foot up
			  case 'f' :
				if(CURRENTLEG[2] >= 0) {
					currentFlex = servos.getAngle(CURRENTLEG[2]);
					currentFlex = currentFlex + panSpeed;
					if(currentFlex > maxLift) {
						currentFlex = maxLift;
					}
					servos.pivotFoot(CURRENTLEG,servoSpeed,currentFlex);
				}
				break;
				  
			  // foot down
			  case 'v' :
				if(CURRENTLEG[2] >= 0) {
					currentFlex = servos.getAngle(CURRENTLEG[2]);
					currentFlex = currentFlex - panSpeed;
					if(currentFlex > maxLift) {
						currentFlex = maxLift;
					}
					servos.pivotFoot(CURRENTLEG,servoSpeed,currentFlex);
				}
				break;
				  
			//walk forward
			  case 'x' :
				  servos.walkForward(1);
				  break;

			//stand up
			  case 'q' :
				  servos.standUp();

				  break;

			//sit down
			  case 'z' :
				  servos.sitDown();

				  break;					  
			//stand up
			  case 'c' :
				  servos.checkFeet();

				  break;
			//wave hello
			  case 'h' :
				  servos.waveHello();

				  break;

				  // pan head left
				case 'j':
					currentPan = servos.getAngle(panServo);
					currentPan = currentPan - panSpeed;
					if(currentPan > maxLift) {
						currentPan = maxLift;
					}
					myPsc.setAngle(panServo,0,currentPan);
					break;
					
					  // pan head left
				case 'l':
					currentPan = servos.getAngle(panServo);
					currentPan = currentPan + panSpeed;
					if(currentPan > maxLift) {
						currentPan = maxLift;
					}
					myPsc.setAngle(panServo,0,currentPan);
					break;

					  // tilt head up
				case 'i':
					currentTilt = servos.getAngle(tiltServo);
					currentTilt = currentTilt + panSpeed;
					if(currentTilt > maxLift) {
						currentTilt = maxLift;
					}
					myPsc.setAngle(tiltServo,0,currentTilt);
					break;
					
					  // tilt head down
				case 'm':
					currentTilt = servos.getAngle(tiltServo);
					currentTilt = currentTilt - panSpeed;
					if(currentTilt > maxLift) {
						currentTilt = maxLift;
					}
					myPsc.setAngle(tiltServo,0,currentTilt);
					break;

					// center pan/tilt
				case 'k':
					myPsc.setAngle(panServo,15,900);
					myPsc.setAngle(tiltServo,15,575);
					break;

					
					
					// get message from PSC test
				case 'g':
					SerialLCD.clearScr();
					SerialLCD.moveTo(2, 0);
					SerialLCD.write(Servos.getAngle(0));
					CPU.delay(10000);
					break;					
					
					//turn
				  case ',' :
					  servos.turnBot("left",0, 1);
					  break;					  
					  
						//turn
				  case '.' :
					  servos.turnBot("right",0, 2);
					  break;					

			//other commands
	    	      case 'r':
	    	    	  	SerialLCD.clearScr();
						Servos.init(channels);
						break;
					
					  case '1' :
						  CURRENTLEG = servos.LEG1;
						  break;
					
					  case '2' :
						  CURRENTLEG = servos.LEG2;
						  break;
						  
					  case '3' :
						  CURRENTLEG = servos.LEG3;
						  break;
						  
					  case '4' :
						  CURRENTLEG = servos.LEG4;
						  break;
					
					  case '5' :
						  CURRENTLEG = servos.LEG5;
						  break;
						  
					  case '6' :
						  CURRENTLEG = servos.LEG6;
						  break;

					  case '7' :
						  CURRENTLEG = servos.HEADSERVOS;
						  break;
						
				
    	      }//end switch 
	      
    	   } else {
 	    	  compass.start();
	    	  i = 0;
	    	  while(!compass.poll() && i < compassTimeout) {
	    		i++;  
	    	  }
	    	  
	    	  if(i < compassTimeout) {
			      messageLine1.delete(0,messageLine1.length());
			      messageLine1.append("Degr. from N.= ");
			      messageLine1.append(compass.getCompassAngle());
			      SerialLCD.clearLine(4);
			      SerialLCD.writeLine(messageLine1.toString(), 4);
	    		  
	    	  } else {
			      messageLine1.delete(0,messageLine1.length());
			      messageLine1.append("no measurement");
			      SerialLCD.clearLine(4);
			      SerialLCD.writeLine(messageLine1.toString(), 4);
	    		  
	    	  }    		   

	    	  if(monitorRC) {
			      xAxis = xCenter - CPU.pulseIn(5000,pulsePin1,false);
			      yAxis = yCenter - CPU.pulseIn(5000,pulsePin2,false);
			      messageLine1.delete(0,messageLine1.length());
			      messageLine1.append("RC X:");
			      messageLine1.append(xAxis);
			      messageLine1.append("  Y:");
			      messageLine1.append(yAxis);
			      SerialLCD.clearLine(3);
			      SerialLCD.writeLine(messageLine1.toString(), 3);
	
			      if(yAxis > 20) {
			    	  Servos.checkFeet();
			    	 // Servos.sitDown();
			      }
			      
			      if(yAxis < -20) {
			    	  Servos.walkForward(1);
			      }
		    	      
			      if(xAxis > 20) {
			    	  Servos.turnBot("left", 10, 1);
			      }
			      if(xAxis < -20) {
			    	  Servos.turnBot("right", 10, 1);
			      }
	    	   }
    	   }

   	    }       

    }

}