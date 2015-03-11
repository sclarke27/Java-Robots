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
	
	public Servos(Uart pscIOin, psc pscIn) {
		pscIO = pscIOin;
		myPsc = pscIn;
	}
	
	public static int[] getServoList(int legNumber) {
		switch(legNumber) {
			case 1 :
				returnList[0] = 0;
				returnList[1] = 1;
				returnList[2] = 2;
				break;
				
			case 2 :
				returnList[0] = 4;
				returnList[1] = 5;
				returnList[2] = 6;
				break;
			
			case 3 :
				returnList[0] = 8;
				returnList[1] = 9;
				returnList[2] = 10;
				break;

			case 4 :
				returnList[0] = 24;
				returnList[1] = 25;
				returnList[2] = 26;
				break;				

			case 5 :
				returnList[0] = 20;
				returnList[1] = 21;
				returnList[2] = 22;
				break;				
				
			case 6 :
				returnList[0] = 16;
				returnList[1] = 17;
				returnList[2] = 18;
				break;				
				

		}
		return returnList;
	}
	
	public static int getAngle(int servoChannel) {
		return myPsc.getAngle(servoChannel);
	}

	public static void setLeg(int legNumber, int servoSpeed, int liftAngle, int swingAngle, int flexAngle) {
		servoList = getServoList(legNumber);
		myPsc.setAngle(servoList[0],servoSpeed,liftAngle);
		myPsc.setAngle(servoList[1],servoSpeed,swingAngle);
		myPsc.setAngle(servoList[2],servoSpeed,flexAngle);
		//CPU.delay(waitTime);
	}	
	
	public static void liftLeg(int legNumber, int servoSpeed, int targetMove) {
		servoList = getServoList(legNumber);
		myPsc.setAngle(servoList[0],servoSpeed,targetMove);
	}
	
	public static void swingLeg(int legNumber, int servoSpeed, int targetMove) {
		servoList = getServoList(legNumber);
		myPsc.setAngle(servoList[1],servoSpeed,targetMove);
	}
	
	public static void pivotFoot(int legNumber, int servoSpeed, int targetMove) {
		servoList = getServoList(legNumber);
		myPsc.setAngle(servoList[2],servoSpeed,targetMove);
	}

	public static void checkFeet() {
		int[] legList = {1,6,2,5,3,4};
		for(int i=0; i < 6; i++) {
			int currLeg = legList[i];
			Servos.liftLeg(currLeg, 7, 600);
			Servos.pivotFoot(currLeg, 7, 800);
			CPU.delay(10000);
			Servos.liftLeg(legList[i], 7, 0);
			Servos.pivotFoot(currLeg, 7, 900);
		}
	}
	
	public static void standUp(int speed) {
		//Servos.init();
		int[] legList = {1,2,3,4,5,6};
		for (int i=0; i  < legList.length; i=i+1) {
			Servos.setLeg(legList[i], speed, maxLift, centerSwivel, 300);
		}
		CPU.delay(5000);
		for (int i=0; i  < legList.length; i=i+1) {
			Servos.setLeg(legList[i], speed, 900, centerSwivel, 400);
		}
		CPU.delay(5000);
		for (int i=0; i  < legList.length; i=i+1) {
			Servos.setLeg(legList[i], speed, minLift, centerSwivel, centerFlex);
		}

	
	}
	public static void sitDown(int speed) {
		int[] legList = {1,6,2,5,3,4};
		for(int i=0; i < 6; i++) {
			Servos.setLeg(legList[i], speed, maxLift, centerSwivel, 300);
		}
		CPU.delay(20000);
		Servos.init();
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
				Servos.liftLeg(workSet[i], speed, 1400);
				Servos.pivotFoot(workSet[i], speed, 270);
			}
			// swing work leg 90ish degrees
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.swingLeg(workSet[i], speed, 420);
			}
			CPU.delay(5000);// wait for servos to finish
			// put work leg down pivot foot back out
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.liftLeg(workSet[i], speed, 0);
				Servos.pivotFoot(workSet[i], speed, 1000);
			}
			CPU.delay(5000); //wait for servos
			// lift resting leg a little and pivot foot in all the way
			for (int i=0; i  < restSet.length; i=i+1) {
				Servos.liftLeg(restSet[i], speed, 270);
				Servos.pivotFoot(restSet[i], speed, 0);
			}
			CPU.delay(5000); //wait for servos
			//swing work leg back to center
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.swingLeg(workSet[i], speed, 900);
			}
			currCount = currCount + 1;
			CPU.delay(5000); //wait for servos
			
		}
		CPU.delay(5000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.pivotFoot(restSet[i], speed, 900);
		}
		CPU.delay(2000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.liftLeg(restSet[i], speed, 0);
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
				Servos.setLeg(workSet[i], speed, maxLift, centerSwivel, 280);
			}
			// swing work leg 51 degrees
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(workSet[i], speed, 1400, 1400, 270);
			}
			CPU.delay(5000);
			// put work leg down pivot foot back out
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(workSet[i], speed, 0, 1400, 1000);
			}
			CPU.delay(5000);
			// lift resting leg a little and pivot foot in all the way
			for (int i=0; i  < restSet.length; i=i+1) {
				Servos.setLeg(restSet[i], speed, 280, 890, 0);
			}
			CPU.delay(5000);
			//swing work leg back to center
			for (int i=0; i  < workSet.length; i=i+1) {
				Servos.setLeg(workSet[i], speed, 0, 890, 1000);
			}
			currCount = currCount + 1;
			CPU.delay(5000); //wait for servos
			
		}
		CPU.delay(5000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.pivotFoot(restSet[i], speed, 900);
		}
		CPU.delay(2000);
		for (int i=0; i  < restSet.length; i=i+1) {
			Servos.liftLeg(restSet[i], speed, 0);
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
