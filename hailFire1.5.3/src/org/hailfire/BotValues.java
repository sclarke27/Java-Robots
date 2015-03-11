package org.hailfire;




public class BotValues {

	public BotValues() {
		super();
	}

	private int freeMemory = 420;
	private int mainSpeed = 7;
	private int leftSpeed = 7;
	private int rightSpeed = 7;
	private int currDistance = 100;
	private int currDirection = 1000;
	private int currentVoltage = 950;
	private int viewButtonIndex = 0;
	private int prgmButtonIndex = 0;
	private int clockCycles = 0;
	private String direction = "forward"; 
	
	/**
	 * @return the freeMemory
	 */
	public int getFreeMemory() {
		return freeMemory;
	}
	/**
	 * @param freeMemory the freeMemory to set
	 */
	public void setFreeMemory(int freeMemory) {
		this.freeMemory = freeMemory;
	}
	/**
	 * @return the leftSpeed
	 */
	public int getLeftSpeed() {
		return leftSpeed;
	}
	/**
	 * @param leftSpeed the leftSpeed to set
	 */
	public void setLeftSpeed(int leftSpeed) {
		this.leftSpeed = leftSpeed;
	}
	/**
	 * @return the mainSpeed
	 */
	public int getMainSpeed() {
		return mainSpeed;
	}
	/**
	 * @param mainSpeed the mainSpeed to set
	 */
	public void setMainSpeed(int mainSpeed) {
		this.mainSpeed = mainSpeed;
	}
	/**
	 * @return the rightSpeed
	 */
	public int getRightSpeed() {
		return rightSpeed;
	}
	/**
	 * @param rightSpeed the rightSpeed to set
	 */
	public void setRightSpeed(int rightSpeed) {
		this.rightSpeed = rightSpeed;
	}
	/**
	 * @return the currDirstance
	 */
	public int getCurrDistance() {
		return currDistance;
	}
	/**
	 * @param currDirstance the currDirstance to set
	 */
	public void setCurrDistance(int currDirstance) {
		this.currDistance = currDirstance;
	}
	/**
	 * @return the direction
	 */
	public String getDirection() {
		return direction;
	}
	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
	/**
	 * @return the currDirection
	 */
	public int getCurrDirection() {
		return currDirection;
	}
	/**
	 * @param currDirection the currDirection to set
	 */
	public void setCurrDirection(int currDirection) {
		this.currDirection = currDirection;
	}
	/**
	 * @return the currentVoltage
	 */
	public int getCurrentVoltage() {
		return currentVoltage;
	}
	/**
	 * @param currentVoltage the currentVoltage to set
	 */
	public void setCurrentVoltage(int currentVoltage) {
		this.currentVoltage = currentVoltage;
	}
	/**
	 * @return the prgmButtonIndex
	 */
	public int getPrgmButtonIndex() {
		return prgmButtonIndex;
	}
	/**
	 * @param prgmButtonIndex the prgmButtonIndex to set
	 */
	public void setPrgmButtonIndex(int prgmButtonIndex) {
		this.prgmButtonIndex = prgmButtonIndex;
	}
	/**
	 * @return the viewButtonIndex
	 */
	public int getViewButtonIndex() {
		return viewButtonIndex;
	}
	/**
	 * @param viewButtonIndex the viewButtonIndex to set
	 */
	public void setViewButtonIndex(int viewButtonIndex) {
		this.viewButtonIndex = viewButtonIndex;
	}
	/**
	 * @return the clockCycles
	 */
	public int getClockCycles() {
		return clockCycles;
	}
	/**
	 * @param clockCycles the clockCycles to set
	 */
	public void setClockCycles(int clockCycles) {
		this.clockCycles = clockCycles;
	}
	
	
}
