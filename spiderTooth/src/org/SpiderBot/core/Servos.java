package org.SpiderBot.core;



import org.SpiderBot.io.SerialLCD;

import stamp.core.CPU;
import stamp.core.Uart;
import stamp.peripheral.servo.psc.psc;
import stamp.util.List;

public class Servos {
		
	private static Uart pscIO;
	private static psc myPsc;
	private static Object legs[];
	private static int[] legTurnSet1 = {1,3,5};
	private static int[] legTurnSet2 = {2,4,6};
	private static int[] returnList = {0,1,2};
	private static int[] servoList = {0,1,2};
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
	public static int[] LEG1 = {0,1,2,1};
	public static int[] LEG2 = {4,5,6,2};
	public static int[] LEG3 = {8,9,10,3};	
	public static int[] LEG4 = {24,25,26,4};
	public static int[] LEG5 = {20,21,22,5};
	public static int[] LEG6 = {16,17,18,6};	
	
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
		return myPsc.getAngle(servoChannel);
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
		Servos.setLeg(LEG1, 3, maxLift, 1100, 1400);
		CPU.delay(3000);		
		Servos.setLeg(LEG1, 8, 700, 1100, 800);
		CPU.delay(10000);		
		Servos.setLeg(LEG1, 8, maxLift, 1100, 1400);
		CPU.delay(10000);		
		Servos.setLeg(LEG1, 8, 700, 1100, 800);
		CPU.delay(10000);		
		Servos.setLeg(LEG1, 8, maxLift, 1100, 1400);
		CPU.delay(10000);		
		Servos.setLeg(LEG1, 2, minLift, centerSwivel, centerFlex);
	}

	public static void checkFeet() {
		Servos.setLeg(LEG1, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG1, 2, centerLift, centerSwivel, 1200);
		CPU.delay(1000);
		Servos.setLeg(LEG1, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);

		Servos.setLeg(LEG3, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG3, 2, centerLift, centerSwivel, 1200);
		CPU.delay(1000);
		Servos.setLeg(LEG3, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);

		Servos.setLeg(LEG5, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG5, 2, centerLift, centerSwivel, 1200);
		CPU.delay(1000);
		Servos.setLeg(LEG5, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);
		
		Servos.setLeg(LEG2, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG2, 2, centerLift, centerSwivel, 1200);
		CPU.delay(1000);
		Servos.setLeg(LEG2, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);
		
		Servos.setLeg(LEG4, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG4, 2, centerLift, centerSwivel, 1200);
		CPU.delay(1000);
		Servos.setLeg(LEG4, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);
		
		Servos.setLeg(LEG6, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG6, 2, centerLift, centerSwivel, 1200);
		CPU.delay(1000);
		Servos.setLeg(LEG6, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);

		Servos.setLeg(LEG1, 2, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG3, 2, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG5, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG1, 2, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG3, 2, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG5, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);
		Servos.setLeg(LEG2, 2, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG4, 2, maxLift, centerSwivel, 300);
		Servos.setLeg(LEG6, 2, maxLift, centerSwivel, 300);
		CPU.delay(1000);
		Servos.setLeg(LEG2, 2, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG4, 2, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG6, 2, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);
		
	}
	
	public static void walkForward() {
		//lift first set forward
		Servos.setLeg(LEG6, 5, maxLift, 800, 1000);
		Servos.setLeg(LEG2, 5, maxLift, 1100, 400);
		Servos.setLeg(LEG4, 5, maxLift, 700, 700);
		CPU.delay(2000);
		// swing second set back
		Servos.setLeg(LEG1, 5, minLift, 650, 800);
		Servos.setLeg(LEG5, 5, minLift, 1100, centerFlex);
		Servos.setLeg(LEG3, 5, minLift, 800, 1000);
		CPU.delay(10000);
		
		//put first set down
		Servos.setLeg(LEG6, 5, minLift, 800, 1000);
		Servos.setLeg(LEG2, 5, minLift, 1100, centerFlex);
		Servos.setLeg(LEG4, 5, minLift, 700, centerFlex);
		CPU.delay(10000);
		// swing second set up and forward
		Servos.setLeg(LEG1, 5, maxLift, 800, 600);
		Servos.setLeg(LEG5, 5, maxLift, 500, 600);
		Servos.setLeg(LEG3, 5, maxLift, 1200, 600);
		CPU.delay(1000);
		// reset first set back to all center
		Servos.setLeg(LEG6, 4, minLift, 1100, 1000);
		Servos.setLeg(LEG2, 4, minLift, 700, centerFlex);
		Servos.setLeg(LEG4, 5, minLift, 950, 1000);
		
		/*
		
		Servos.setLeg(LEG4, 5, minLift, centerSwivel, 1000);
		Servos.setLeg(LEG2, 5, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG6, 5, minLift, centerSwivel, centerFlex);
		CPU.delay(5000);

		Servos.setLeg(LEG1, 1, minLift, 800, 1200);
		Servos.setLeg(LEG5, 1, minLift, 500, 1000);
		Servos.setLeg(LEG3, 1, minLift, 1200, 600);
		CPU.delay(1000);
		
		Servos.setLeg(LEG6, 4, centerLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG2, 4, centerLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG4, 4, centerLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG1, 4, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG5, 4, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG3, 4, minLift, centerSwivel, centerFlex);
		CPU.delay(1000);
		Servos.setLeg(LEG6, 4, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG2, 4, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG4, 4, minLift, centerSwivel, centerFlex);
		*/
	}
	
	public static void quickStand() {
		Servos.setLeg(LEG1, 10, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG2, 10, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG3, 10, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG4, 10, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG5, 10, minLift, centerSwivel, centerFlex);
		Servos.setLeg(LEG6, 10, minLift, centerSwivel, centerFlex);
		
	}
	
	public static void standUp(int speed) {
		//all legs up and in first position
		Servos.setLeg(LEG1, 8, maxLift, 1100, maxFlex);
		Servos.setLeg(LEG2, 8, maxLift, 1300, maxFlex);
		Servos.setLeg(LEG3, 8, maxLift, 600, 350);
		Servos.setLeg(LEG4, 8, maxLift, 1200, 350);
		Servos.setLeg(LEG5, 8, maxLift, 300, maxFlex);
		Servos.setLeg(LEG6, 8, maxLift, 680, maxFlex);
		CPU.delay(2000);
		
		//push down in back
		Servos.setLeg(LEG3, 0, minLift, 600, 350);
		Servos.setLeg(LEG4, 0, minLift, 1200, 350);
		//CPU.delay(2000);
		
		//put front legs down
		Servos.setLeg(LEG1, 0, minLift, 1100, maxFlex);
		Servos.setLeg(LEG2, 0, minLift, 1300, maxFlex);
		Servos.setLeg(LEG5, 0, minLift, 300, maxFlex);
		Servos.setLeg(LEG6, 0, minLift, 680, maxFlex);
		CPU.delay(2000);
		
		//push forward with back legs, pull with front
		Servos.setLeg(LEG3, 10, minLift, 600, 1350);
		Servos.setLeg(LEG4, 10, minLift, 1200, 1350);
		Servos.setLeg(LEG1, 10, minLift, 1100, centerFlex);
		Servos.setLeg(LEG2, 10, minLift, 1400, centerFlex);
		Servos.setLeg(LEG5, 10, minLift, 400, centerFlex);
		Servos.setLeg(LEG6, 10, minLift, 680, centerFlex);
		
		CPU.delay(30000);
		
		//lift middle legs and reposition
		Servos.setLeg(LEG2, 3, maxLift, 400, centerFlex);
		Servos.setLeg(LEG5, 3, maxLift, 1400, centerFlex);
		CPU.delay(3000);
		
		//push down on middle legs 
		Servos.setLeg(LEG2, 0, minLift, 400, 1000);
		Servos.setLeg(LEG5, 0, minLift, 1400, 1000);
		CPU.delay(2000);
		
		//pick up and reset back legs 
		Servos.setLeg(LEG3, 0, maxLift, 600, 400);
		Servos.setLeg(LEG4, 0, maxLift, 1200, 400);
		CPU.delay(2000);
		Servos.setLeg(LEG3, 0, minLift, 600, centerFlex);
		Servos.setLeg(LEG4, 0, minLift, 1200, centerFlex);
		CPU.delay(2000);

		//lift middle legs and reposition
		Servos.setLeg(LEG2, 0, maxLift, 400, 600);
		Servos.setLeg(LEG5, 0, maxLift, 1400, 300);
		Servos.setLeg(LEG2, 0, minLift, 400, centerFlex);
		Servos.setLeg(LEG5, 0, minLift, 1400, centerFlex);
		CPU.delay(2000);

		//pick up and reset back legs 
		Servos.setLeg(LEG3, 0, maxLift, 600, 400);
		Servos.setLeg(LEG4, 0, maxLift, 1200, 400);
		Servos.setLeg(LEG3, 0, minLift, 600, centerFlex);
		Servos.setLeg(LEG4, 0, minLift, 1200, centerFlex);
		CPU.delay(2000);

		Servos.setLeg(LEG2, 0, maxLift, centerSwivel, 300);
		CPU.delay(2000);
		Servos.setLeg(LEG2, 0, minLift, centerSwivel, centerFlex);
		CPU.delay(2000);
		Servos.setLeg(LEG5, 0, maxLift, centerSwivel, 300);
		CPU.delay(2000);
		Servos.setLeg(LEG5, 0, minLift, centerSwivel, centerFlex);
		CPU.delay(2000);
		Servos.setLeg(LEG1, 0, maxLift, centerSwivel, 300);
		CPU.delay(2000);
		Servos.setLeg(LEG1, 0, minLift, centerSwivel, centerFlex);
		CPU.delay(2000);
		Servos.setLeg(LEG6, 0, maxLift, centerSwivel, 300);
		CPU.delay(2000);
		Servos.setLeg(LEG6, 0, minLift, centerSwivel, centerFlex);
		CPU.delay(2000);
		Servos.setLeg(LEG3, 0, maxLift, centerSwivel, 300);
		CPU.delay(2000);
		Servos.setLeg(LEG3, 0, minLift, centerSwivel, centerFlex);
		CPU.delay(2000);
		Servos.setLeg(LEG4, 0, maxLift, centerSwivel, 300);
		CPU.delay(2000);
		Servos.setLeg(LEG4, 0, minLift, centerSwivel, centerFlex);
		
		//checkFeet();

	}
	public static void sitDown(int speed) {
		//checkFeet();
		Servos.setLeg(LEG1, 8, minLift, centerSwivel, 400);
		Servos.setLeg(LEG2, 8, minLift, centerSwivel, 400);
		Servos.setLeg(LEG3, 8, minLift, centerSwivel, 400);
		Servos.setLeg(LEG4, 8, minLift, centerSwivel, 400);
		Servos.setLeg(LEG5, 8, minLift, centerSwivel, 400);
		Servos.setLeg(LEG6, 8, minLift, centerSwivel, 400);
	}

	public static void turnRight(int count, int speed) {
		int currCount = 0;
		int[] workSet = legTurnSet1;
		int[] restSet = legTurnSet2;
		
		while(currCount <= count) {
			if(currCount%2==1) {
				workSet = legTurnSet2;
				restSet = legTurnSet1;
			} else {
				workSet = legTurnSet1;
				restSet = legTurnSet2;
			}
			// lift work leg pivot foot in
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.liftLeg(getServoList(workSet[i]), speed, 1400);
				Servos.pivotFoot(getServoList(workSet[i]), speed, 270);
			}
			// swing work leg 90ish degrees
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.swingLeg(getServoList(workSet[i]), speed, 420);
			}
			CPU.delay(5000);// wait for servos to finish
			// put work leg down pivot foot back out
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.liftLeg(getServoList(workSet[i]), speed, 0);
				Servos.pivotFoot(getServoList(workSet[i]), speed, 1000);
			}
			CPU.delay(5000); //wait for servos
			// lift resting leg a little and pivot foot in all the way
			for (int i=0; i  < restSet.length; i=i+1) {
				Servos.liftLeg(getServoList(restSet[i]), speed, 270);
				Servos.pivotFoot(getServoList(restSet[i]), speed, 0);
			}
			CPU.delay(5000); //wait for servos
			//swing work leg back to center
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.swingLeg(getServoList(workSet[i]), speed, 900);
			}
			currCount = currCount + 1;
			CPU.delay(5000); //wait for servos
			
		}
		CPU.delay(5000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.pivotFoot(getServoList(restSet[i]), speed, 900);
		}
		CPU.delay(2000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.liftLeg(getServoList(restSet[i]), speed, 0);
		}
		CPU.delay(5000);
		Servos.checkFeet();
		
	}

	public static void turnLeft(int count, int speed) {
		int currCount = 0;
		int[] workSet = legTurnSet1;
		int[] restSet = legTurnSet2;
		
		while(currCount <= count) {
			if(currCount%2==1) {
				workSet = legTurnSet2;
				restSet = legTurnSet1;
			} else {
				workSet = legTurnSet1;
				restSet = legTurnSet2;
			}
			// lift work leg pivot foot in
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(getServoList(workSet[i]), speed, 1400, 890, 280);
			}
			// swing work leg 51 degrees
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(getServoList(workSet[i]), speed, 1400, 1400, 270);
			}
			CPU.delay(5000);
			// put work leg down pivot foot back out
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(getServoList(workSet[i]), speed, 0, 1400, 1000);
			}
			CPU.delay(5000);
			// lift resting leg a little and pivot foot in all the way
			for (int i=0; i  < restSet.length; i=i+1) {
				Servos.setLeg(getServoList(restSet[i]), speed, 280, 890, 0);
			}
			CPU.delay(5000);
			//swing work leg back to center
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(getServoList(workSet[i]), speed, 0, 890, 1000);
			}
			currCount = currCount + 1;
			CPU.delay(5000); //wait for servos
			
		}
		CPU.delay(5000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.pivotFoot(getServoList(restSet[i]), speed, 900);
		}
		CPU.delay(2000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.liftLeg(getServoList(restSet[i]), speed, 0);
		}
		CPU.delay(5000);
		Servos.checkFeet();

	}	

	public static void init() {
		for(int i=0; i< 30; i++) {
			myPsc.initChannel(i,270,1240,-750,3); //52428 is 65536*fraction(1800/(1250-250))
		}
	}

}
