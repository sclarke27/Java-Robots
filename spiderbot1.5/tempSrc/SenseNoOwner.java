package org.SpiderBot.core;

/**
 * A class to sense when the arbitrator has no owner so we can give it one.
 * Waits on the arbitrator and when notified checks to see if there is an owner.
 * If not it executes its actuator.
 */
public class SenseNoOwner extends Sense  {
	public SenseNoOwner(Actuator actuator) {
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