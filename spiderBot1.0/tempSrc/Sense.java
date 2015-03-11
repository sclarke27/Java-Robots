package org.SpiderBot.core;


/**
 * Base class for sensor listener thread. This is tightly coupled to
 * an actuator in this implementation. If its sensor listener is called
 * it grabs that sensor's monitor and calls notifuAll(). This should wake
 * up any threads wait()ing on that sensor.
 * <P>
 * Sub-classes should implement run() to wait on the sensor's monitor.
 */
public abstract class Sense extends Thread implements SensorConstants {
	Actuator actuator;
	
	Sense(Actuator actuator) {
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

