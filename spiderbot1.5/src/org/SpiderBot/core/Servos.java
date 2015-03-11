package org.SpiderBot.core;



import org.SpiderBot.io.SerialLCD;

import stamp.core.CPU;
import stamp.core.Uart;
import stamp.peripheral.servo.psc.psc;

public class Servos {
		
	//private static Uart pscIO;
	private static psc myPscT;
	private static psc myPscR;
	private static int maxLift = 1800;
	private static int minLift = 0;
	private static int centerLift = 900;
	private static int maxSwivel = 1400;
	private static int minSwivel = 400;
	private static int centerSwivel = 900;
	private static int maxFlex = 1800;
	private static int minFlex = 0;
	private static int centerFlex = 850;
	private static int channels = 30;

	/* leg list */
	public static int[] LEG1 = {16,17,19,1};	
	public static int[] LEG2 = {20,21,23,2};
	public static int[] LEG3 = {24,25,27,3};
	public static int[] LEG4 = {0,1,3,6}; // channel, channel, channel, leg#
	public static int[] LEG5 = {4,5,7,5};
	public static int[] LEG6 = {8,9,11,4};	
	public static int PANSERVO = 14;
	public static int TILTSERVO = 15;
	public static int SCREENPANSERVO = 28;
	public static int SCREENTILTSERVO = 29;
	public static int[] HEADSERVOS = {PANSERVO,TILTSERVO,-1,7};
	private static int[] workLeg1 = LEG1;
	private static int[] workLeg2 = LEG3;
	private static int[] workLeg3 = LEG5;
	private static int[] restLeg1 = LEG2;
	private static int[] restLeg2 = LEG4;
	private static int[] restLeg3 = LEG6;
	private static int turnDirection = maxSwivel;
	private static int[] returnList = {0,1,2};
	
	public Servos(psc pscIn) {
		myPscT = pscIn;
		myPscR = pscIn;
	}
	
	public int[] getServoList(int legNumber) {
		switch(legNumber) {
		  case '1' :
			  returnList = LEG1;
			  break;
		
		  case '2' :
			  returnList = LEG2;
			  break;
			  
		  case '3' :
			  returnList = LEG3;
			  break;
			  
		  case '4' :
			  returnList = LEG4;
			  break;
		
		  case '5' :
			  returnList = LEG5;
			  break;
			  
		  case '6' :
			  returnList = LEG6;
			  break;	
		}
		return returnList;
	}
	
	public int getAngle(int servoChannel) {
		//return myPsc.getAngle(servoChannel);
		return 0;
	}

	public void setLeg(int[] servoList, int servoSpeed, int liftAngle, int swingAngle, int flexAngle) {
		myPscT.setAngle(servoList[0],servoSpeed,liftAngle);
		myPscT.setAngle(servoList[1],servoSpeed,swingAngle);
		myPscT.setAngle(servoList[2],servoSpeed,flexAngle);
	}	
	
	public void liftLeg(int[] servoList, int servoSpeed, int targetMove) {
		myPscT.setAngle(servoList[0],servoSpeed,targetMove);
	}
	
	public void liftLegTest(int[] servoList, int servoSpeed, int targetMove) {
		int footOffset = targetMove+90;
		StringBuffer tempStr = new StringBuffer();
		tempStr.append(footOffset);
		SerialLCD.writeLine(tempStr.toString(), 3);		
		myPscT.setAngle(servoList[0],servoSpeed,targetMove);
		myPscT.setAngle(servoList[2],servoSpeed,footOffset);
	}
	
	public void liftLegTest2(int[] servoList, int servoSpeed, int targetAngle) {
		int oppositeAngle = 0;
		if(targetAngle <= 900) {
			oppositeAngle = (1800 - targetAngle)/2;
		} else {
			oppositeAngle = (1800 - targetAngle)/3;
		}
		StringBuffer tempStr = new StringBuffer();
		tempStr.append("t:");
		tempStr.append(targetAngle);
		tempStr.append(" o:");
		tempStr.append(oppositeAngle);
		SerialLCD.writeLine(tempStr.toString(), 3);		
		myPscT.setAngle(servoList[0],servoSpeed,targetAngle);
		myPscT.setAngle(servoList[2],servoSpeed,oppositeAngle);
	}

	
	public void raiseLeg(int[] servoList, int servoSpeed) {
		myPscT.setAngle(servoList[0],servoSpeed,900);
		myPscT.setAngle(servoList[2],servoSpeed,600);
		
		myPscT.setAngle(servoList[0],servoSpeed,1350);
		myPscT.setAngle(servoList[2],servoSpeed,450);

		myPscT.setAngle(servoList[0],servoSpeed,1800);
		myPscT.setAngle(servoList[2],servoSpeed,300);
	}

	public void lowerLeg(int[] servoList, int servoSpeed) {
		myPscT.setAngle(servoList[0],servoSpeed,900);
		myPscT.setAngle(servoList[2],servoSpeed,600);
		
		myPscT.setAngle(servoList[0],servoSpeed,450);
		myPscT.setAngle(servoList[2],servoSpeed,750);

		myPscT.setAngle(servoList[0],servoSpeed,0);
		myPscT.setAngle(servoList[2],servoSpeed,900);
	}

	
	
	public void swingLeg(int[] servoList, int servoSpeed, int targetMove) {
		myPscT.setAngle(servoList[1],servoSpeed,targetMove);
	}
	
	public void pivotFoot(int[] servoList, int servoSpeed, int targetMove) {
		myPscT.setAngle(servoList[2],servoSpeed,targetMove);
	}
	
	public void waveHello() {
		setLeg(LEG1, 2, maxLift, 1100, 1400);
		CPU.delay(3000);		
		setLeg(LEG1, 7, 600, 1100, 800);
		CPU.delay(5000);		
		setLeg(LEG1, 7, maxLift, 1100, 1400);
		CPU.delay(5000);		
		setLeg(LEG1, 7, 600, 1100, 800);
		CPU.delay(5000);		
		setLeg(LEG1, 7, maxLift, 1100, 1400);
		CPU.delay(5000);		
		setLeg(LEG1, 3, minLift, centerSwivel, centerFlex);
	}

	public void checkFeet() {

		setLeg(LEG1, 4, maxLift, centerSwivel, minFlex);
		setLeg(LEG3, 4, maxLift, centerSwivel, minFlex);
		setLeg(LEG5, 4, maxLift, centerSwivel, minFlex);
		CPU.delay(2000);
		pivotFoot(LEG1, 10, centerFlex);
		liftLeg(LEG1, 8, minLift);
		pivotFoot(LEG3, 10, centerFlex);
		liftLeg(LEG3, 8, minLift);
		pivotFoot(LEG5, 10, centerFlex);
		liftLeg(LEG5, 8, minLift);
		CPU.delay(20000);

		setLeg(LEG2, 5, maxLift, centerSwivel, minFlex);
		setLeg(LEG4, 5, maxLift, centerSwivel, minFlex);
		setLeg(LEG6, 5, maxLift, centerSwivel, minFlex);
		CPU.delay(2000);
		pivotFoot(LEG2, 10, centerFlex);
		liftLeg(LEG2, 8, minLift);
		pivotFoot(LEG4, 10, centerFlex);
		liftLeg(LEG4, 8, minLift);
		pivotFoot(LEG6, 10, centerFlex);
		liftLeg(LEG6, 8, minLift);
		CPU.delay(20000);
		
	}


	public void proneStance() {
		//get into walking stance
		liftLeg(LEG1, 5, maxLift);
		pivotFoot(LEG1, 5, 300);
		liftLeg(LEG4, 5, maxLift);
		pivotFoot(LEG4, 5, 300);
		CPU.delay(5000);
		swingLeg(LEG1, 5, centerSwivel);
		swingLeg(LEG4, 5, centerSwivel);
		CPU.delay(5000);
		pivotFoot(LEG1, 3, centerFlex);
		liftLeg(LEG1, 6, minLift);
		pivotFoot(LEG4, 3, centerFlex);
		liftLeg(LEG4, 6, minLift);
		CPU.delay(5000);
		
		liftLeg(LEG6, 5, maxLift);
		pivotFoot(LEG6, 5, 300);
		liftLeg(LEG3, 5, maxLift);
		pivotFoot(LEG3, 5, 300);
		CPU.delay(5000);
		swingLeg(LEG6, 5, centerSwivel);
		swingLeg(LEG3, 5, centerSwivel);
		CPU.delay(5000);
		pivotFoot(LEG6, 3, centerFlex);
		liftLeg(LEG6, 6, minLift);
		pivotFoot(LEG3, 3, centerFlex);
		liftLeg(LEG3, 6, minLift);
		CPU.delay(5000);		
	}	
	
	public void walkingStance() {
		//get into walking stance
		liftLeg(LEG1, 2, centerLift);
		pivotFoot(LEG1, 2, 300);
		liftLeg(LEG4, 2, centerLift);
		pivotFoot(LEG4, 2, 300);
		CPU.delay(2000);
		swingLeg(LEG1, 2, 600);
		swingLeg(LEG4, 2, 600);
		CPU.delay(2000);
		pivotFoot(LEG1, 1, centerFlex);
		liftLeg(LEG1, 4, minLift);
		pivotFoot(LEG4, 1, centerFlex);
		liftLeg(LEG4, 4, minLift);
		CPU.delay(4000);
		
		liftLeg(LEG2, 2, centerLift);
		pivotFoot(LEG2, 2, 300);
		liftLeg(LEG5, 2, centerLift);
		pivotFoot(LEG5, 2, 300);
		CPU.delay(2000);
		swingLeg(LEG2, 2, centerSwivel);
		swingLeg(LEG5, 2, centerSwivel);
		CPU.delay(2000);
		pivotFoot(LEG5, 1, centerFlex);
		liftLeg(LEG5, 4, minLift);
		pivotFoot(LEG2, 1, centerFlex);
		liftLeg(LEG2, 4, minLift);
		CPU.delay(4000);

		liftLeg(LEG6, 2, centerLift);
		pivotFoot(LEG6, 2, 300);
		liftLeg(LEG3, 2, centerLift);
		pivotFoot(LEG3, 2, 300);
		CPU.delay(2000);
		swingLeg(LEG6, 2, 1200);
		swingLeg(LEG3, 2, 1200);
		CPU.delay(2000);
		pivotFoot(LEG6, 1, centerFlex);
		liftLeg(LEG6, 4, minLift);
		pivotFoot(LEG3, 1, centerFlex);
		liftLeg(LEG3, 4, minLift);
		CPU.delay(3000);			
	}
	
	public void walkForward(int count) {
		workLeg1 = LEG1;
		workLeg2 = LEG3;
		workLeg3 = LEG5;
		restLeg1 = LEG6;
		restLeg2 = LEG2;
		restLeg3 = LEG4;
		
		setLeg(workLeg1, 4, 1000, centerSwivel, minFlex);
		setLeg(workLeg2, 4, 1000, centerSwivel, minFlex);
		setLeg(workLeg3, 4, 1000, centerSwivel, minFlex);
		CPU.delay(2000);
		pivotFoot(workLeg1, 10, centerFlex);
		liftLeg(workLeg1, 8, minLift);
		pivotFoot(workLeg2, 10, centerFlex);
		liftLeg(workLeg2, 8, minLift);
		pivotFoot(workLeg3, 10, centerFlex);
		liftLeg(workLeg3, 8, minLift);
		CPU.delay(20000);

		setLeg(restLeg1, 5, 1000, centerSwivel, minFlex);
		setLeg(restLeg2, 5, 1000, centerSwivel, minFlex);
		setLeg(restLeg3, 5, 1000, centerSwivel, minFlex);
		CPU.delay(2000);
		pivotFoot(restLeg1, 10, centerFlex);
		liftLeg(restLeg1, 8, minLift);
		pivotFoot(restLeg2, 10, centerFlex);
		liftLeg(restLeg2, 8, minLift);
		pivotFoot(restLeg3, 10, centerFlex);
		liftLeg(restLeg3, 8, minLift);
		CPU.delay(20000);		
		
	}

	public void walkForward_Old(int count) {
		
		//start walking
		for(int i=0;i<count;i++) {
			
			//if(i%2==0) {
				workLeg1 = LEG1;
				workLeg2 = LEG3;
				workLeg3 = LEG5;
				restLeg1 = LEG6;
				restLeg2 = LEG2;
				restLeg3 = LEG4;
			/*} else {
				workLeg1 = LEG1;
				workLeg2 = LEG3;
				workLeg3 = LEG5;
				restLeg1 = LEG2;
				restLeg2 = LEG4;
				restLeg3 = LEG6;
			}*/
			
			liftLeg(workLeg1,2,centerLift);
			pivotFoot(workLeg1, 2, 300);
			liftLeg(workLeg2,2,centerLift);
			pivotFoot(workLeg2, 2, 300);
			liftLeg(workLeg3,2,centerLift);
			pivotFoot(workLeg3, 2, 300);
			CPU.delay(1000);
			swingLeg(restLeg1,3,1350);
			//swingLeg(workLeg1,3,750);
			swingLeg(restLeg2,3,750);
			//swingLeg(workLeg2,3,1350);
			swingLeg(restLeg3,3,800);
			//swingLeg(workLeg3,3,1350);
			CPU.delay(3000);
			pivotFoot(workLeg1, 1, centerFlex);
			liftLeg(workLeg1, 4, minLift);
			pivotFoot(workLeg2, 1, centerFlex);
			liftLeg(workLeg2, 4, minLift);
			pivotFoot(workLeg3, 1, centerFlex);
			liftLeg(workLeg3, 4, minLift);
			CPU.delay(3000);
			liftLeg(restLeg1,2,centerLift);
			pivotFoot(restLeg1, 2, 300);
			liftLeg(restLeg2,2,centerLift);
			pivotFoot(restLeg2, 2, 300);
			liftLeg(restLeg3,2,centerLift);
			pivotFoot(restLeg3, 2, 300);
			CPU.delay(2000);
			swingLeg(restLeg1,3,1200);
			// swingLeg(workLeg1,3,600);
			swingLeg(restLeg2,3,900);
			// swingLeg(workLeg2,3,900);
			swingLeg(restLeg3,3,600);
			// swingLeg(workLeg3,3,1200);
			CPU.delay(5000);
			pivotFoot(restLeg1, 1, centerFlex);
			liftLeg(restLeg1, 4, minLift);
			pivotFoot(restLeg2, 1, centerFlex);
			liftLeg(restLeg2, 4, minLift);
			pivotFoot(restLeg3, 1, centerFlex);
			liftLeg(restLeg3, 4, minLift);
			CPU.delay(3000);
			
		}
		
		

		
		//CPU.delay(30000);
		//checkFeet();
		//walkingStance();

	}
	
	public void standUp() {
		//all legs up and in first position
		//pivotFoot(LEG1, 11, 300);
		swingLeg(LEG1, 7, 600);
		liftLeg(LEG1, 7, maxLift);

		//pivotFoot(LEG2, 11, 300);
		swingLeg(LEG2, 7, centerSwivel);
		liftLeg(LEG2, 7, maxLift);
		
		//pivotFoot(LEG3, 11, 300);
		swingLeg(LEG3, 7, 1200);
		liftLeg(LEG3, 7, maxLift);
		
		//pivotFoot(LEG6, 11, 300);
		swingLeg(LEG6, 7, 1200);
		liftLeg(LEG6, 7, maxLift);

		//pivotFoot(LEG5, 11, 300);
		swingLeg(LEG5, 7, centerSwivel);
		liftLeg(LEG5, 7, maxLift);
		
		//pivotFoot(LEG4, 11, 300);
		swingLeg(LEG4, 7, 600);
		liftLeg(LEG4, 7, maxLift);

		CPU.delay(12000);

		//pivotFoot(LEG1, 12, centerFlex);
		swingLeg(LEG1, 7, 600);
		liftLeg(LEG1, 7, minLift);

		//pivotFoot(LEG6, 12, centerFlex);
		swingLeg(LEG6, 7, 1200);
		liftLeg(LEG6, 7, minLift);

		//pivotFoot(LEG2, 12, centerFlex);
		swingLeg(LEG2, 7, centerSwivel);
		liftLeg(LEG2, 7, minLift);
		
		//pivotFoot(LEG5, 12, centerFlex);
		swingLeg(LEG5, 7, centerSwivel);
		liftLeg(LEG5, 7, minLift);

		//pivotFoot(LEG3, 12, centerFlex);
		swingLeg(LEG3, 7, 1200);
		liftLeg(LEG3, 7, minLift);

		//pivotFoot(LEG4, 12, centerFlex);
		swingLeg(LEG4, 7, 600);
		liftLeg(LEG4, 7, minLift);
		
		CPU.delay(30000);

	}
	public void sitDown() {
		//checkFeet();
		setLeg(LEG1, 12, maxLift, centerSwivel, 300);
		setLeg(LEG2, 12, maxLift, centerSwivel, 300);
		setLeg(LEG3, 12, maxLift, centerSwivel, 300);
		setLeg(LEG4, 12, maxLift, centerSwivel, 300);
		setLeg(LEG5, 12, maxLift, centerSwivel, 300);
		setLeg(LEG6, 12, maxLift, centerSwivel, 300);
		CPU.delay(15000);
		setLeg(LEG1, 8, maxLift, centerSwivel, centerFlex);
		setLeg(LEG2, 8, maxLift, centerSwivel, centerFlex);
		setLeg(LEG3, 8, maxLift, centerSwivel, centerFlex);
		setLeg(LEG4, 8, maxLift, centerSwivel, centerFlex);
		setLeg(LEG5, 8, maxLift, centerSwivel, centerFlex);
		setLeg(LEG6, 8, maxLift, centerSwivel, centerFlex);
	}

	public void turnBot(String direction, int speed, int count) {

		proneStance();
		
		for(int i=0;i<count;i++) {
			
			//if(i%2==0) {
				workLeg1 = LEG1;
				workLeg2 = LEG3;
				workLeg3 = LEG5;
				restLeg1 = LEG2;
				restLeg2 = LEG4;
				restLeg3 = LEG6;
			/*} else {
				workLeg1 = LEG2;
				workLeg2 = LEG4;
				workLeg3 = LEG6;
				restLeg1 = LEG1;
				restLeg2 = LEG3;
				restLeg3 = LEG5;
			}*/

			if(direction == "left") {
				turnDirection = maxSwivel - 200;
			} else {
				turnDirection = minSwivel + 200;
			}
			pivotFoot(workLeg1, 10, minFlex);
			liftLeg(workLeg1, 8, maxLift);
			pivotFoot(workLeg2, 10, minFlex);
			liftLeg(workLeg2, 8, maxLift);
			pivotFoot(workLeg3, 10, minFlex);
			liftLeg(workLeg3, 8, maxLift);
			CPU.delay(5000);
			swingLeg(restLeg1, 10, turnDirection);
			swingLeg(restLeg2, 10, turnDirection);
			swingLeg(restLeg3, 10, turnDirection);
			CPU.delay(7000);
			pivotFoot(workLeg1, 10, centerFlex);
			liftLeg(workLeg1, 8, minLift);
			pivotFoot(workLeg2, 10, centerFlex);
			liftLeg(workLeg2, 8, minLift);
			pivotFoot(workLeg3, 10, centerFlex);
			liftLeg(workLeg3, 8, minLift);
			CPU.delay(20000);
			setLeg(restLeg1, 8, maxLift, centerSwivel, minFlex);
			setLeg(restLeg2, 8, maxLift, centerSwivel, minFlex);
			setLeg(restLeg3, 8, maxLift, centerSwivel, minFlex);
			CPU.delay(8000);
			pivotFoot(restLeg1, 4, 800);
			liftLeg(restLeg1, 8, minLift);
			pivotFoot(restLeg2, 4, 800);
			liftLeg(restLeg2, 8, minLift);
			pivotFoot(restLeg3, 4, 800);
			liftLeg(restLeg3, 8, minLift);
			CPU.delay(20000);
		}
		
		
	}

	public void init() {
		for(int i=0; i< channels; i++) {
			StringBuffer tempStr = new StringBuffer();
			tempStr.append("channel: ");
			tempStr.append(i);
			SerialLCD.clearLine(3);
			SerialLCD.writeLine(tempStr.toString(), 3);
			myPscT.initChannel(i,250,1250,900,0); //52428 is 65536*fraction(1800/(1250-250))
		}
	}
	
	public void liftScreen(int tiltLevel) {
		switch(tiltLevel) {
			case 0:
				myPscT.setAngle(SCREENTILTSERVO,14,900);
				myPscT.setAngle(SCREENPANSERVO,14,900);
				SerialLCD.backlight("off");
				break;
			case 1:
				myPscT.setAngle(SCREENTILTSERVO,14,1300);
				myPscT.setAngle(SCREENPANSERVO,14,0);
				SerialLCD.backlight("on");
				break;
			case 2:
				myPscT.setAngle(SCREENTILTSERVO,14,1700);
				myPscT.setAngle(SCREENPANSERVO,14,750);
				SerialLCD.backlight("on");
				break;

			default:
				myPscT.setAngle(SCREENTILTSERVO,14,900);
				myPscT.setAngle(SCREENPANSERVO,14,900);
				SerialLCD.backlight("off");
				break;
		
		}
	}

}
