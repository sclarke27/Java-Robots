package org.SpiderBot.core;



import org.SpiderBot.io.SerialLCD;

import stamp.core.CPU;
import stamp.core.Uart;
import stamp.peripheral.servo.psc.psc;

public class Servos {
		
	private static Uart pscIO;
	private static psc myPsc;
	private static Object legs[];
	private static int maxLift = 1800;
	private static int minLift = 0;
	private static int centerLift = 900;
	private static int maxSwivel = 1400;
	private static int minSwivel = 400;
	private static int centerSwivel = 900;
	private static int maxFlex = 1800;
	private static int minFlex = 0;
	private static int centerFlex = 900;
	private static int[] tempArr;

	/* leg list */
	public static int[] LEG1 = {16,17,19,1};	
	public static int[] LEG2 = {20,21,23,2};
	public static int[] LEG3 = {24,25,27,3};
	public static int[] LEG4 = {0,1,3,6}; // channel, channel, channel, leg#
	public static int[] LEG5 = {4,5,7,5};
	public static int[] LEG6 = {8,9,11,4};	
	public static int PANSERVO = 14;
	public static int TILTSERVO = 15;
	public static int[] HEADSERVOS = {PANSERVO,TILTSERVO,-1,7};
	private static int[] workLeg1 = LEG1;
	private static int[] workLeg2 = LEG3;
	private static int[] workLeg3 = LEG5;
	private static int[] restLeg1 = LEG2;
	private static int[] restLeg2 = LEG4;
	private static int[] restLeg3 = LEG6;
	private static int turnDirection = maxSwivel;
	private static int reverseTurnDirection = minSwivel;
	private static int[] returnList = {0,1,2};
	private static int[] servoList = {0,1,2};
	
	public Servos(Uart pscIOin, psc pscIn) {
		pscIO = pscIOin;
		myPsc = pscIn;
	}
	
	public static int[] getServoList(int legNumber) {
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
	
	public static int getAngle(int servoChannel) {
		//return myPsc.getAngle(servoChannel);
		return 0;
	}

	public static void setLeg(int[] servoList, int servoSpeed, int liftAngle, int swingAngle, int flexAngle) {
		myPsc.setAngle(servoList[0],servoSpeed,liftAngle);
		myPsc.setAngle(servoList[1],servoSpeed,swingAngle);
		myPsc.setAngle(servoList[2],servoSpeed,flexAngle);
	}	
	
	public static void liftLeg(int[] servoList, int servoSpeed, int targetMove) {
		myPsc.setAngle(servoList[0],servoSpeed,targetMove);
	}
	
	public static void swingLeg(int[] servoList, int servoSpeed, int targetMove) {
		myPsc.setAngle(servoList[1],servoSpeed,targetMove);
	}
	
	public static void pivotFoot(int[] servoList, int servoSpeed, int targetMove) {
		myPsc.setAngle(servoList[2],servoSpeed,targetMove);
	}
	
	public static void waveHello() {
		Servos.setLeg(LEG1, 2, maxLift, 1100, 1400);
		CPU.delay(3000);		
		Servos.setLeg(LEG1, 7, 600, 1100, 800);
		CPU.delay(5000);		
		Servos.setLeg(LEG1, 7, maxLift, 1100, 1400);
		CPU.delay(5000);		
		Servos.setLeg(LEG1, 7, 600, 1100, 800);
		CPU.delay(5000);		
		Servos.setLeg(LEG1, 7, maxLift, 1100, 1400);
		CPU.delay(5000);		
		Servos.setLeg(LEG1, 3, minLift, centerSwivel, centerFlex);
	}

	public static void checkFeet() {

		Servos.setLeg(LEG1, 4, maxLift, centerSwivel, minFlex);
		Servos.setLeg(LEG3, 4, maxLift, centerSwivel, minFlex);
		Servos.setLeg(LEG5, 4, maxLift, centerSwivel, minFlex);
		CPU.delay(2000);
		Servos.pivotFoot(LEG1, 10, centerFlex);
		Servos.liftLeg(LEG1, 8, minLift);
		Servos.pivotFoot(LEG3, 10, centerFlex);
		Servos.liftLeg(LEG3, 8, minLift);
		Servos.pivotFoot(LEG5, 10, centerFlex);
		Servos.liftLeg(LEG5, 8, minLift);
		CPU.delay(20000);

		Servos.setLeg(LEG2, 5, maxLift, centerSwivel, minFlex);
		Servos.setLeg(LEG4, 5, maxLift, centerSwivel, minFlex);
		Servos.setLeg(LEG6, 5, maxLift, centerSwivel, minFlex);
		CPU.delay(2000);
		Servos.pivotFoot(LEG2, 10, centerFlex);
		Servos.liftLeg(LEG2, 8, minLift);
		Servos.pivotFoot(LEG4, 10, centerFlex);
		Servos.liftLeg(LEG4, 8, minLift);
		Servos.pivotFoot(LEG6, 10, centerFlex);
		Servos.liftLeg(LEG6, 8, minLift);
		CPU.delay(20000);
		
		Servos.walkingStance();
		
	}


	private static void proneStance() {
		//get into walking stance
		Servos.liftLeg(LEG1, 5, maxLift);
		Servos.pivotFoot(LEG1, 5, 300);
		Servos.liftLeg(LEG4, 5, maxLift);
		Servos.pivotFoot(LEG4, 5, 300);
		CPU.delay(5000);
		Servos.swingLeg(LEG1, 5, centerSwivel);
		Servos.swingLeg(LEG4, 5, centerSwivel);
		CPU.delay(5000);
		Servos.pivotFoot(LEG1, 3, centerFlex);
		Servos.liftLeg(LEG1, 6, minLift);
		Servos.pivotFoot(LEG4, 3, centerFlex);
		Servos.liftLeg(LEG4, 6, minLift);
		CPU.delay(5000);
		
		Servos.liftLeg(LEG6, 5, maxLift);
		Servos.pivotFoot(LEG6, 5, 300);
		Servos.liftLeg(LEG3, 5, maxLift);
		Servos.pivotFoot(LEG3, 5, 300);
		CPU.delay(5000);
		Servos.swingLeg(LEG6, 5, centerSwivel);
		Servos.swingLeg(LEG3, 5, centerSwivel);
		CPU.delay(5000);
		Servos.pivotFoot(LEG6, 3, centerFlex);
		Servos.liftLeg(LEG6, 6, minLift);
		Servos.pivotFoot(LEG3, 3, centerFlex);
		Servos.liftLeg(LEG3, 6, minLift);
		CPU.delay(5000);		
	}	
	
	private static void walkingStance() {
		//get into walking stance
		Servos.liftLeg(LEG1, 5, maxLift);
		Servos.pivotFoot(LEG1, 5, 300);
		Servos.liftLeg(LEG4, 5, maxLift);
		Servos.pivotFoot(LEG4, 5, 300);
		CPU.delay(5000);
		Servos.swingLeg(LEG1, 5, 600);
		Servos.swingLeg(LEG4, 5, 600);
		CPU.delay(5000);
		Servos.pivotFoot(LEG1, 3, centerFlex);
		Servos.liftLeg(LEG1, 6, minLift);
		Servos.pivotFoot(LEG4, 3, centerFlex);
		Servos.liftLeg(LEG4, 6, minLift);
		CPU.delay(5000);
		
		Servos.liftLeg(LEG6, 5, maxLift);
		Servos.pivotFoot(LEG6, 5, 300);
		Servos.liftLeg(LEG3, 5, maxLift);
		Servos.pivotFoot(LEG3, 5, 300);
		CPU.delay(5000);
		Servos.swingLeg(LEG6, 5, 1200);
		Servos.swingLeg(LEG3, 5, 1200);
		CPU.delay(5000);
		Servos.pivotFoot(LEG6, 3, centerFlex);
		Servos.liftLeg(LEG6, 6, minLift);
		Servos.pivotFoot(LEG3, 3, centerFlex);
		Servos.liftLeg(LEG3, 6, minLift);
		CPU.delay(5000);			
	}

	public static void walkForward(int count) {
		
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
			
			Servos.liftLeg(workLeg1,5,maxLift);
			Servos.pivotFoot(workLeg1, 3, 300);
			Servos.liftLeg(workLeg2,5,maxLift);
			Servos.pivotFoot(workLeg2, 3, 300);
			Servos.liftLeg(workLeg3,5,maxLift);
			Servos.pivotFoot(workLeg3, 3, 300);
			CPU.delay(5000);
			Servos.swingLeg(restLeg1,8,1350);
			Servos.swingLeg(restLeg2,8,750);
			Servos.swingLeg(restLeg3,8,750);
			CPU.delay(10000);
			Servos.pivotFoot(workLeg1, 3, centerFlex);
			Servos.liftLeg(workLeg1, 6, minLift);
			Servos.pivotFoot(workLeg2, 3, centerFlex);
			Servos.liftLeg(workLeg2, 6, minLift);
			Servos.pivotFoot(workLeg3, 3, centerFlex);
			Servos.liftLeg(workLeg3, 6, minLift);
			CPU.delay(5000);
			Servos.liftLeg(restLeg1,5,maxLift);
			Servos.pivotFoot(restLeg1, 3, 300);
			Servos.liftLeg(restLeg2,5,maxLift);
			Servos.pivotFoot(restLeg2, 3, 300);
			Servos.liftLeg(restLeg3,5,maxLift);
			Servos.pivotFoot(restLeg3, 3, 300);
			CPU.delay(2000);
			Servos.swingLeg(restLeg1,5,1200);
			Servos.swingLeg(restLeg2,5,900);
			Servos.swingLeg(restLeg3,5,600);
			CPU.delay(5000);
			Servos.pivotFoot(restLeg1, 3, centerFlex);
			Servos.liftLeg(restLeg1, 6, minLift);
			Servos.pivotFoot(restLeg2, 3, centerFlex);
			Servos.liftLeg(restLeg2, 6, minLift);
			Servos.pivotFoot(restLeg3, 3, centerFlex);
			Servos.liftLeg(restLeg3, 6, minLift);
			CPU.delay(5000);
			
		}
		
		

		
		//CPU.delay(30000);
		//Servos.checkFeet();
		//Servos.walkingStance();

	}
	
	public static void standUp() {
		//all legs up and in first position
		Servos.pivotFoot(LEG1, 11, 300);
		Servos.swingLeg(LEG1, 7, 600);
		Servos.liftLeg(LEG1, 7, maxLift);

		Servos.pivotFoot(LEG2, 11, 300);
		Servos.swingLeg(LEG2, 7, centerSwivel);
		Servos.liftLeg(LEG2, 7, maxLift);
		
		Servos.pivotFoot(LEG3, 11, 300);
		Servos.swingLeg(LEG3, 7, 1200);
		Servos.liftLeg(LEG3, 7, maxLift);
		
		Servos.pivotFoot(LEG6, 11, 300);
		Servos.swingLeg(LEG6, 7, 1200);
		Servos.liftLeg(LEG6, 7, maxLift);

		Servos.pivotFoot(LEG5, 11, 300);
		Servos.swingLeg(LEG5, 7, centerSwivel);
		Servos.liftLeg(LEG2, 7, maxLift);
		
		Servos.pivotFoot(LEG4, 11, 300);
		Servos.swingLeg(LEG4, 7, 600);
		Servos.liftLeg(LEG4, 7, maxLift);

		CPU.delay(12000);

		Servos.pivotFoot(LEG1, 12, centerFlex);
		Servos.swingLeg(LEG1, 7, 600);
		Servos.liftLeg(LEG1, 7, minLift);

		Servos.pivotFoot(LEG2, 12, centerFlex);
		Servos.swingLeg(LEG2, 7, centerSwivel);
		Servos.liftLeg(LEG2, 7, minLift);
		
		Servos.pivotFoot(LEG3, 12, centerFlex);
		Servos.swingLeg(LEG3, 7, 1200);
		Servos.liftLeg(LEG3, 7, minLift);

		Servos.pivotFoot(LEG4, 12, centerFlex);
		Servos.swingLeg(LEG4, 7, 600);
		Servos.liftLeg(LEG4, 7, minLift);

		Servos.pivotFoot(LEG5, 12, centerFlex);
		Servos.swingLeg(LEG5, 7, centerSwivel);
		Servos.liftLeg(LEG5, 7, minLift);
		
		Servos.pivotFoot(LEG6, 12, centerFlex);
		Servos.swingLeg(LEG6, 7, 1200);
		Servos.liftLeg(LEG6, 7, minLift);
		
		CPU.delay(30000);
		
		//Servos.checkFeet();
		Servos.walkingStance();

	}
	public static void sitDown() {
		//checkFeet();
		Servos.setLeg(LEG1, 12, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG2, 12, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG3, 12, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG4, 12, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG5, 12, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG6, 12, maxLift, centerSwivel, 300);
		CPU.delay(15000);
		Servos.setLeg(LEG1, 8, maxLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG2, 8, maxLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG3, 8, maxLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG4, 8, maxLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG5, 8, maxLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG6, 8, maxLift, centerSwivel, centerFlex);
	}

	public static void turnBot(String direction, int speed, int count) {

		Servos.proneStance();
		
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
			Servos.pivotFoot(workLeg1, 10, minFlex);
			Servos.liftLeg(workLeg1, 8, maxLift);
			Servos.pivotFoot(workLeg2, 10, minFlex);
			Servos.liftLeg(workLeg2, 8, maxLift);
			Servos.pivotFoot(workLeg3, 10, minFlex);
			Servos.liftLeg(workLeg3, 8, maxLift);
			CPU.delay(5000);
			Servos.swingLeg(restLeg1, 10, turnDirection);
			Servos.swingLeg(restLeg2, 10, turnDirection);
			Servos.swingLeg(restLeg3, 10, turnDirection);
			CPU.delay(7000);
			Servos.pivotFoot(workLeg1, 10, centerFlex);
			Servos.liftLeg(workLeg1, 8, minLift);
			Servos.pivotFoot(workLeg2, 10, centerFlex);
			Servos.liftLeg(workLeg2, 8, minLift);
			Servos.pivotFoot(workLeg3, 10, centerFlex);
			Servos.liftLeg(workLeg3, 8, minLift);
			CPU.delay(20000);
			Servos.setLeg(restLeg1, 8, maxLift, centerSwivel, minFlex);
			Servos.setLeg(restLeg2, 8, maxLift, centerSwivel, minFlex);
			Servos.setLeg(restLeg3, 8, maxLift, centerSwivel, minFlex);
			CPU.delay(8000);
			Servos.pivotFoot(restLeg1, 4, 800);
			Servos.liftLeg(restLeg1, 8, minLift);
			Servos.pivotFoot(restLeg2, 4, 800);
			Servos.liftLeg(restLeg2, 8, minLift);
			Servos.pivotFoot(restLeg3, 4, 800);
			Servos.liftLeg(restLeg3, 8, minLift);
			CPU.delay(20000);
		}
		
		
	}

	public static void init(int channels) {
		for(int i=0; i< channels; i++) {
			StringBuffer tempStr = new StringBuffer();
			tempStr.append("channel: ");
			tempStr.append(i);
			SerialLCD.clearLine(3);
			SerialLCD.writeLine(tempStr.toString(), 3);
			myPsc.initChannel(i,270,1240,-750,0); //52428 is 65536*fraction(1800/(1250-250))
		}
	}

}
