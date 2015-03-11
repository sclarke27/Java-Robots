package org.hailfire;

/**
 * Created on Jul 17, 2004
 *
 */

import josx.platform.rcx.*;

/**
 * @author scott
 * @version 1.5 - 08/19/06 recreating again after several drive failures
 */
class BotMain {
    
    //private static actuator actuator;
    private static BotActions botActions;
    //private static Sense Sense;
    
    public static void main(String[] args) 
    {
        // init bot
        initBot();
        
        // run the bot.
        runBot();

        // wait for run button to be pressed
        try{Button.RUN.waitForPressAndRelease();} catch(InterruptedException ie) {}
        
    }

    public static void runBot() {
    	final BotValues mainBotValObj = new BotValues();

    	ButtonListener bListen = new ButtonListener() {
            private int count = mainBotValObj.getViewButtonIndex();

            public void buttonPressed( Button button) {
            	count++;
            	if(count <= 7) {
            		mainBotValObj.setViewButtonIndex(count);
            	} else {
            		mainBotValObj.setViewButtonIndex(0);
            		count = 0;
            	}
            }
            public void buttonReleased( Button button) {
            }
        };
        Button.VIEW.addButtonListener(bListen);
          

        /**
         * create memory behavior thread from sense abstract class in sensors.java
         * 
         */
        Sense memoryMonitor = new SenseNoOwner(new actuator(botActions.checkMemory(mainBotValObj)));
        memoryMonitor.setPri(Thread.MIN_PRIORITY + 1);

        /**
         * create memory behavior thread from sense abstract class in sensors.java
         * 
         */
        Sense batteryMonitor = new SenseNoOwner(new actuator(botActions.checkBattery(mainBotValObj)));
        batteryMonitor.setPri(Thread.MIN_PRIORITY + 1);

        /** 
         * create long distance object detection behavior thread from sense abstract class in sensors.java
         */
        Sense senseObjects = new SenseDistance(Sensor.S1, Sensor.S2,new actuator(botActions.senseObjects(mainBotValObj)));
        senseObjects.setPri(Thread.MIN_PRIORITY + 1);

        /** 
         * create collision behavior thread from sense abstract class in sensors.java
         */
        Sense collisionFound = new SenseBumper(Sensor.S3, new actuator(botActions.collision("right")));
        collisionFound.setPri(Thread.MIN_PRIORITY + 1);

        /**
         * create main bot monitor
         * 
         */
        Sense monitorBot = new SenseNoOwner(new actuator(botActions.monitorBotState(mainBotValObj)));
        monitorBot.setPri(Thread.MIN_PRIORITY + 1);

        /** 
         * create drive behavior thread 
         */
        Sense driveAction = new SenseNoOwner(new actuator(botActions.doDriveForward(mainBotValObj)));
        driveAction.setPri(Thread.MIN_PRIORITY + 1);

        
        // set current thread to max priority
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        
        // run behavior threads
        monitorBot.runIt();
        memoryMonitor.runIt();
        batteryMonitor.runIt();
        driveAction.runIt();
        senseObjects.runIt();
        collisionFound.runIt();
        
    }
    
    private static void initBot() {
		Sound.playTone(100, 10);
		Sound.playTone(420, 40);
		Sensor.S1.setTypeAndMode ( (SensorConstants.SENSOR_TYPE_TOUCH), (SensorConstants.SENSOR_MODE_RAW) );
        Sensor.S2.setTypeAndMode ( (SensorConstants.SENSOR_TYPE_TOUCH), (SensorConstants.SENSOR_MODE_RAW) );
        Sensor.S1.activate();
        Sensor.S2.activate();
        int i = 0;
        int timeStep = 250;
        int waitTime = 5000;
        do {
        	LCD.showNumber(i);
        	i = i + timeStep;
        	try { Thread.sleep(timeStep); } catch (InterruptedException e) { }
        } while (i <= waitTime);
        LCD.clear();
        Sensor.S1.passivate();
        Sensor.S2.passivate();
        //Sound.playTone(420, 10);
        Sound.buzz();

    }
    
}

