package org.SpiderBot.core;

import org.SpiderBot.io.SerialLCD;
import org.SpiderBot.sensors.Compass;
import org.SpiderBot.sensors.Ping;

import stamp.core.CPU;
import stamp.core.Uart;
import stamp.peripheral.servo.psc.psc;

public class Actions {

	// bot objects (servos, sensors, etc) - set by constructor
	public static psc psc;
	public static Servos servos;
	public static Uart pscIOT;
	public static Uart pscIOR;
	public static int panServo;
	public static int tiltServo;
	public static Compass compass;
	public static Ping ping;
	
	// bot states
	public StringBuffer messageLine = new StringBuffer();
	public boolean monitorRCon = false;
	public boolean inWalkingStance = false;
	public int currHeight = 0;
	public int currDirection = 0;
	public int stepCount = 0;
	public int turnAmount = 0;
	public int objectDistance = 10000;
	public int nearestObjAngle = 0; 
	public int nearestObjDist = 10000; 
	public int[] nearestObj = new int[2];
	public int iterations = 0;
	public int compassTimeout = 100;
	public int i;
	public int rcLeftXCenter = 2121;
	public int rcLeftYCenter = 2112;
	public int rcLeftXAxis = 2121;
	public int rcLeftYAxis = 2112;
	public int pulsePin1;
	public int pulsePin2;
	

	
	/**
	 * default constructor
	 * @param myPsc1
	 * @param servos1
	 * @param pscIO1
	 * @param panServo1
	 * @param tiltServo1
	 * @param compass1
	 * @param ping1
	 * @param pulsePin11
	 * @param pulsePin21
	 */
	public Actions(psc myPsc1, Servos servos1, Uart pscIOTran, Uart pscIORec, int panServo1, int tiltServo1, Compass compass1, Ping ping1, int pulsePin11, int pulsePin21) {
		psc = myPsc1;
		servos = servos1;
		pscIOT = pscIOTran;
		pscIOR = pscIORec;
		panServo = panServo1;
		tiltServo = tiltServo1;
		compass = compass1;
		ping = ping1;
		pulsePin1 = pulsePin11;
		pulsePin2 = pulsePin21;
	}
	
	/**
	 * initialize the servo controllers 
	 * and set all servos to default position
	 *
	 */
	public void initServoControl() {
		
		SerialLCD.writeLine("Init PSC...", 1);
		psc.initPsc(pscIOT, pscIOR);
		SerialLCD.writeLine("Init Servos...", 2);
		servos.init();
		psc.setAngle(panServo,15,900);
		psc.setAngle(tiltServo,15,575);
		servos.liftScreen(0);
		CPU.delay(2000);
	}

	/**
	 * standing action
	 *
	 */
	public void standUp() {
		servos.standUp();
		this.checkStandingHeight();
	}
	
	/**
	 * walking action
	 * @param steps
	 */
	public void walkForward(int steps) {
		if(!inWalkingStance) {
			checkStandingHeight();
		}
		inWalkingStance = false;
		servos.walkForward(steps);
		checkStandingHeight();
	}
	
	/**
	 * turn to angle
	 */
	public void turnToAngle(int angle) {
		if(inWalkingStance) {
			servos.proneStance();
		}
		inWalkingStance = false;
		if(angle < currDirection) {
			turnAmount = (currDirection - angle);
		} else {
			turnAmount = (angle - currDirection);
		}

		if(turnAmount < 0) { 
			turnAmount = turnAmount + 360; 
		}		

		if(turnAmount > 360) { 
			turnAmount = turnAmount - 360; 
		}		
		
		
		if(turnAmount > 180) { 
			turnAmount = 360 - turnAmount; 
		}
		
		turnAmount = turnAmount/10;
		checkStandingHeight();
	}
	
	/**
	 * initialize the compass
	 *
	 */
	public void initCompass() {
		SerialLCD.writeLine("Init Compass...", 3);
		compass.reset();
		
	}

	/**
	 * initalize the robot
	 *
	 */
	public void initializeBot() {
		SerialLCD.initDisplay();
		SerialLCD.clearScr();
		this.initServoControl();
		this.initCompass();
		SerialLCD.writeLine("Gizmo2 Ready", 4);
		CPU.delay(20000);		
	}

	/**
	 * checks to see if bot is standing using ping. 
	 * values less then 155 means the bot is sitting
	 * over 250 and the bot is at full hieght, anything 
	 * else means the bot is sinking
	 *
	 */
	public void checkStandingHeight() {
		psc.setAngle(tiltServo,0,50);
		psc.setAngle(panServo,0,900);
		CPU.delay(3000);
		currHeight = ping.getObjDistance('m');
		messageLine.delete(0,messageLine.length());
		messageLine.append("dist from floor= ");
		messageLine.append(currHeight);
		SerialLCD.clearLine(1);
		SerialLCD.writeLine(messageLine.toString(), 1);
		// if distance is around 150 then stand up
		if(currHeight < 160) {
			servos.standUp();
		}
		// while distance is greater then 160 (standing) but less then 250 (full height) then checkFeet() until correct
		while(currHeight > 160 && currHeight < 250) {
			messageLine.delete(0,messageLine.length());
			messageLine.append("dist from floor= ");
			messageLine.append(currHeight);
			SerialLCD.clearLine(1);
			SerialLCD.writeLine(messageLine.toString(), 1);
			servos.checkFeet();
			currHeight = ping.getObjDistance('m');
		}
		psc.setAngle(tiltServo,10,575);
		psc.setAngle(panServo,10,900);
		servos.walkingStance();
		inWalkingStance = true;
		
	}

	/**
	 * pan back and forth to find nearest object using ping sensor
	 *
	 */
	public int[] findNearestObj() {
		psc.setAngle(tiltServo,3,575);
		nearestObjDist = 1000;
		for(i=70; i<=110; i=i+1) {
			psc.setAngle(panServo,3,i*10);
			objectDistance = ping.getObjDistance('r');
			if(objectDistance <= nearestObjDist) {
				nearestObjAngle = i*10;
				nearestObjDist = objectDistance;
			}
		}
		/*
		for(i=160; i>=40; i=i-3) {
			psc.setAngle(panServo,3,i*10);
			objectDistance = ping.getObjDistance('r');
			if(objectDistance <= nearestObjDist) {
				nearestObjAngle = i*10;
				nearestObjDist = objectDistance;
			}
		}
		*/
		psc.setAngle(panServo,4,nearestObjAngle);
		nearestObj[0] = nearestObjAngle;
		nearestObj[1] = objectDistance;
		//CPU.delay(10000);
		return nearestObj;
		
	}
	
	/**
	 * look for objects and turn away
	 *
	 */
	public void avoidObjects() {
		this.findNearestObj();
		if(nearestObj[0] <= 70 && nearestObj[1] <= 50) {
			servos.turnBot("left",0, 1);
		}
		if(nearestObj[0] >= 110 && nearestObj[1] <= 50) {
			servos.turnBot("right",0, 1);
		}
	}
	
	/**
	 * check what directon the bot is facing using the compass
	 *
	 */
	public void checkFacingDirection() {
		compass.start();
		i = 0;
		while(!compass.poll() && i < compassTimeout) {
			i++;  
		}
		if(i < compassTimeout) {
			currDirection = compass.getCompassAngle();
		} else {
			currDirection = 400; // make greater then 360 so we can just check for 'out of range' if true compass value not found
		}
		messageLine = new StringBuffer();
		messageLine.append("Face Dir:");
		messageLine.append(currDirection);
		SerialLCD.moveTo(SerialLCD.LINE4, 0);
		SerialLCD.write(messageLine);
		
	}

	/**
	 * monitor the values sent from RC controller
	 *
	 */
	public void monitorRC() {
		rcLeftXAxis = rcLeftXCenter - CPU.pulseIn(5000,pulsePin1,false);
		rcLeftYAxis = rcLeftYCenter - CPU.pulseIn(5000,pulsePin2,false);
		
		if(rcLeftYAxis > 50) {
			servos.checkFeet();
		}
		if(rcLeftYAxis < -50) {
			servos.walkForward(1);
		}
		if(rcLeftXAxis > 50) {
			servos.turnBot("left", 10, 1);
		}
		if(rcLeftXAxis < -50) {
			servos.turnBot("right", 10, 1);
		}		
	}
	
	
	/**
	 * handle when a key on the keyboard is pressed.
	 * @param keypressed
	 */
	public void handleKeyPress(char keypressed) {
		switch ( keypressed ) {

			case '8':
				servos.liftScreen(0);
				break;
			case '9':
				servos.liftScreen(1);
				break;
			case '0':
				servos.liftScreen(2);
				break;

			case 'p':
				ping.testPing();
				break;
		
			case 't':
				checkStandingHeight();
				break;
			  
			case 'y':
				findNearestObj();
				break;
			
			case 'x' :
				walkForward(3);
				break;
			
			case 'q' :
				standUp();
				break;
			
			case 'z' :
				servos.sitDown();
				break;
			
			case 'c' :
				servos.checkFeet();
				break;
				
			case 'h' :
				servos.waveHello();
				break;

			case 'k':
				psc.setAngle(panServo,15,900);
				psc.setAngle(tiltServo,15,575);
				break;
			
			// get message from PSC test
			case 'g':
				SerialLCD.clearScr();
				SerialLCD.moveTo(2, 0);
				SerialLCD.write(servos.getAngle(0));
				CPU.delay(10000);
				break;					
			
			case ',' :
				servos.turnBot("left",0, 1);
				break;					  
			  
			case '.' :
				servos.turnBot("right",0, 2);
				break;					
			
			case 'i':
				SerialLCD.clearScr();
				servos.init();
				break;
				
			case '1':
				SerialLCD.clearScr();
				servos.liftLegTest2(servos.LEG1, 1, 0);
				break;				
			case '2':
				SerialLCD.clearScr();
				servos.liftLegTest2(servos.LEG1, 5, 1250);
				break;				
			
			case '3':
				SerialLCD.clearScr();
				servos.liftLegTest2(servos.LEG1, 1, 1800);
				break;				
			case '4':
				SerialLCD.clearScr();
				servos.liftLegTest2(servos.LEG1, 5, 350);
				break;				
			case '5':
				SerialLCD.clearScr();
				psc.getPosition(servos.LEG1[0]);
				
				break;	
				
			case '6':
				SerialLCD.clearScr();
				psc.version();
				
				break;

			case '7':
				SerialLCD.clearScr();
				psc.getPosition(servos.LEG1[1]);
				
				break;	
				
			case 'r':
				if(monitorRCon) {
					monitorRCon = false;
				} else {
					monitorRCon = true;
				}
				break;
		}//end switch 		
	}

}
