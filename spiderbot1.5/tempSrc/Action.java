package org.SpiderBot.core;


/*
* Functor interface. Or, to put it another way, the interface to
* actions stored in a finite state machine (fsm).
*/
public interface Action {
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
