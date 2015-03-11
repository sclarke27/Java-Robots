/*
 * Created on Jul 17, 2004
 *
 */
package org.hailfire;

import josx.platform.rcx.*;

/**
 * The <tt>sensors</tt> class should handle all communication from sensors.
 * All other classes should call this to get sensor data.
 * @author scott
 * @version 0.1 07/17/04 - created
 * @version 0.2 07/18/04 - copied Sensor classes from lejos subsumption2 exmple. 
 */
public class sensors {

	/*
    private Sensor leftTouch = Sensor.S1;
    private Sensor rightTouch = Sensor.S3;
    private Sensor centerData= Sensor.S2;
    private motors motor = new motors(); 
    */
	
    /**
     * 
     */
    public sensors() {
        super();
    }
   
}

/**
 * Base class for sensor listener thread. This is tightly coupled to
 * an actuator in this implementation. If its sensor listener is called
 * it grabs that sensor's monitor and calls notifuAll(). This should wake
 * up any threads wait()ing on that sensor.
 * <P>
 * Sub-classes should implement run() to wait on the sensor's monitor.
 */
abstract class Sense extends Thread implements SensorListener, SensorConstants {
	actuator actuator;
	
	Sense(actuator actuator) {
		this.actuator = actuator;
		setDaemon(true);
	}

	/**
	 * This is actually executed in a thread established by
	 * &lt;bumper&gt;.addSensorListener(). That thread executes at
	 * MAX_PRIORITY so just hand the call off.
	 */	
	public void stateChanged(Sensor bumper, int oldValue, int newValue) {
		synchronized (bumper) {
			bumper.notifyAll();
		}
	}
	
	public void setPri(int priority) {
		actuator.setPriority(priority);
		setPriority(priority);
	}
	
	public void runIt() {
		actuator.start();
		start();
	}
} 

/**
 * A class to sense when the arbitrator has no owner so we can give it one.
 * Waits on the arbitrator and when notified checks to see if there is an owner.
 * If not it executes its actuator.
 */
class SenseNoOwner extends Sense  {
	public SenseNoOwner(actuator actuator) {
		super(actuator);
	}
	
	public void run() {
		while (true) {
			synchronized (actuator.arbitrator) {
				// If there is no owner, we'll take it.
				if (actuator.owner == null)
					actuator.execute();

				try {
					actuator.arbitrator.wait();	// Wait until notified
				} catch (InterruptedException ie) {
				}
			}	
		}
	}
}

/**
 * Defines a thread to detect an obstacle on the left. Waits on its bumper
 * and, when notified, will execute its actuator if the bumper's value is true.
 */
class SenseBumper extends Sense {
	Sensor bumper;

	SenseBumper(Sensor bumper, actuator actuator) {
		super(actuator);

		this.bumper = bumper;
		bumper.setTypeAndMode (SENSOR_TYPE_TOUCH, SENSOR_MODE_BOOL);
		bumper.activate();
		
		// Add a listener for the bumper
		bumper.addSensorListener(this);
	}
	
	public void run() {
		// Never exit the thread
		while (true) {
			// Grab the monitor of the bumper
			synchronized (bumper) {
			
				// While bumper isn't pressed wait.
				do {
					try {
						bumper.wait();
					} catch (InterruptedException ie) {
					}
					Sound.playTone(440, 10);
				} while (!bumper.readBooleanValue());
			}
			Sound.playTone(300, 40);
			
			// Execute our FSM
			actuator.execute();
		}
	}
}

/**
 * Defines a thread to detect an obstacle on the left. Waits on its bumper
 * and, when notified, will execute its actuator if the bumper's value is true.
 */
class SenseDistance extends Sense {
	Sensor distSensor;
	Sensor shortSensor;
	
	SenseDistance(Sensor distSensor, Sensor shortSensor, actuator actuator) {
		super(actuator);

		this.distSensor = distSensor;
		distSensor.setTypeAndMode(SENSOR_TYPE_LIGHT,SENSOR_MODE_PCT);
		distSensor.activate();

		this.shortSensor = shortSensor;
		shortSensor.setTypeAndMode(SENSOR_TYPE_LIGHT,SENSOR_MODE_PCT);
		shortSensor.activate();

		// Add a listener for the bumper
		distSensor.addSensorListener(this);
		shortSensor.addSensorListener(this);
		
	}

	public void run() {
		// Never exit the thread
		while (true) {
			// Grab the monitor of the bumper
			synchronized (distSensor) {
			    
				distSensor.readBooleanValue();
				shortSensor.readBooleanValue();
				
			}
			//Sound.playTone(500, 10);

			// Execute our FSM
			actuator.execute();
		}
	}
}

/**
 * Defines a thread to detect an obstacle on the left. Waits on its bumper
 * and, when notified, will execute its actuator if the bumper's value is true.
 */
class SenseButton extends Sense {
	Button button;
	ButtonListener bListen = new ButtonListener() {
        private int count = 0;

        public void buttonPressed( Button button) {
          count++;
          LCD.showProgramNumber( count);
        }
        public void buttonReleased( Button button) {
        }
      };
    
	SenseButton(Button button, actuator actuator) {
		super(actuator);

		this.button = button;
		
		button.addButtonListener(bListen);
		// Add a listener for the button
		//try{button.waitForPressAndRelease();} catch(InterruptedException ie) {}
		
	}
	
	public void run() {
		// Never exit the thread
		while (true) {
			// Grab the monitor of the bumper
			synchronized (button) {
			
				// While bumper isn't pressed wait.
				do {
					TextLCD.print("button");
					Sound.playTone(440, 10);
					try{button.waitForPressAndRelease();} catch(InterruptedException ie) {}
				} while (!button.isPressed());				
				
				
				
			}
			
			
			// Execute our FSM
			actuator.execute();
		}
	}
}
