/*
 * Created on Jul 18, 2004
 *
 */

package org.hailfire;

import josx.platform.rcx.MinLCD;

/**
 * The <tt>actuator</tt> class handles thread execution for indiviual actions[]?
 * @author not me
 * @version 0.1 - 07/17/04 - created (copied from lejos example subsumption2)
 */
public class actuator extends Thread {

	// Any old object will do as the 'arbitrator'
	Object arbitrator = new Object();
	
	// This must be set to the one actuator allowed to execute whilst the
	// monitor of 'arbitrator' is owned.
	actuator owner;
	
	protected Action actions[];
	protected int state = Action.END;
	
	// Useful for debugging.	
	public static int tcount = 0;
	public int task;

	/**
	 * Constructor. Sets a task id that can be used to identify this instance.
	 * Sets the thread daemon flag to true.
	 *
	 * @param actions an array of Action items to be executed.
	 */
	public actuator(Action[] actions) {
		this.actions = actions;
		task = ++tcount;
		setDaemon(true);
	}

	public actuator() {
	    
	}
	/**
	 * The thread entry point. Runs the Actuator's FSM to completion or until
	 * it looses ownership.  Wait on the arbitrator's monitor between each state.
	 * It might be nice if tasks were left running and just had their access to
	 * the actuators gated, but the same effect can be achieved by their just
	 * running a worker thread if they need some background processing done.
	 * <P>
	 * FSM is really a bit of a misnomer as there are no input events so
	 * there is only one transition from each state to the next.
	 */
	public void run() {
		// Keep running until the program should exit.
		synchronized (arbitrator) {
			do {
				// Wait until we get ownership.
				while (owner != this) {
					try  {
						arbitrator.wait();	// Release arbitrator until notified
					} catch (InterruptedException ie) {
					}
				}
				
				// Set state to start because we might have been terminated
				// prematurely and we always start from the beginning.
				state = Action.START;
				
				// Loop until we end or we loose ownership.				
				while (owner == this && state != Action.END) {
					//MinLCD.setNumber(0x301f,(state+1)*10+task,0x3002);
					//MinLCD.refresh();
					try  {
						// Call wait() because it releases the arbitrator.
						arbitrator.wait(actions[state].act());
					} catch (InterruptedException ie) {
					}
					state = actions[state].nextState();
				}

				// If we ran to completion signify no owner.				
				if (state == Action.END)
					owner = null;
				
				arbitrator.notifyAll();	
			} while (true);
		}
	}

	/**
	 * Attempt to run this Actuator.
	 */	
	public void execute() {
		synchronized (arbitrator) {
			// Basically, set a global flag that all threads can test
		// to see if they should stop running their FSM.
			owner = this;
			
			// Wake up anything waiting on 'arbitrator'.
			arbitrator.notifyAll();
		}
	}
}

/*
* Functor interface. Or, to put it another way, the interface to
* actions stored in a finite state machine (fsm).
*/
interface Action {
	public static final int END = -1;
	public static final int START = 0;

	/**
	 * Perform some sequence of actions.
	 */
	public int act();

	/**
	 * Return what the next state should be.
	 */
	public int nextState();
}


