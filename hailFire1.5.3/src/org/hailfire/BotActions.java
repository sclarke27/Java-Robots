/*
 * Created on Jul 18, 2004
 *
 */

package org.hailfire;

import josx.platform.rcx.*;

/**
 * The <tt>botActions</tt> contains a library of actions the bot can take. 
 * Each method contains an Action[] array.
 * 
 * @author scott
 * @version 0.1 07/18/04 - created.
 * 
 */
public class BotActions {

    /**
     * default constructor class
     */
    public BotActions() {
    }

    //private motor object constructor
    private static motors motor = new motors();
    private static BotValues botValues;
    
    /**
     * The <tt>doDriveForward</tt> returns an Action[] array for driving the bot forward.
     * @return action list
     * @see org.hailfire.motor#goForward()
     * @version 1.5 - 08/19/06 recreating again after several drive failures
     * @param mainBotValObj 
     */
    public static Action[] doDriveForward(BotValues botval) {
        Action[] actions = new Action[1];
        botValues = botval;
        
        // go forward
        actions[0] = new Action() {
		    /* go forward at full speed.
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		        
		        return 5;
		    }
		
		    /* nothing else to do but go forward
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return  Action.END;
		    }
        };
        
        return actions;
    }
    
    /**
     * The <tt>senseDistObjects</tt> returns an Action[] array for driving the bot forward.
     * @return action list
     */
    public static Action[] monitorBotState(BotValues botval) {
        Action[] actions = new Action[4];
        botValues = botval;
        
        
        actions[0] = new Action() {
        	
    	    public int act() {
		        
		        if(botValues.getCurrDistance() > 80 && botValues.getCurrDistance() < 100) {
		        	botValues.setMainSpeed(5);
		        } else if(botValues.getCurrDistance() > 60 && botValues.getCurrDistance() < 80) {
		        	botValues.setMainSpeed(4);
		        } else if(botValues.getCurrDistance() > 20 && botValues.getCurrDistance() <= 60) {
		        	botValues.setMainSpeed(2);
		        } else if(botValues.getCurrDistance() > 10 && botValues.getCurrDistance() <= 20) {
		        	botValues.setMainSpeed(1);
		        } else if(botValues.getCurrDistance() < 10) {
		        	botValues.setMainSpeed(0);
		        	//this.waitTime = 1500;
		        } else {
		        	botValues.setMainSpeed(7);
		        }

		        botValues.setLeftSpeed(botValues.getMainSpeed());
		        botValues.setRightSpeed(botValues.getMainSpeed());
		        
		        //LCD.showNumber((int)(botValues.getMainSpeed()));
		        //LCD.refresh();	

		        
		        return 5;
    	    }
        	
    	    public int nextState() {
    	    	return 1;
    	    }
        };
        
        actions[1] = new Action() {
        	public int act() {
		        // adjust each wheel speed based on objects around bot
       	        if(botValues.getCurrDirection() == 37) {
    	        	//objDirection = "left";
       	        	botValues.setRightSpeed(botValues.getMainSpeed() - 2);
       	        	//TextLCD.print("left");
       	        	
    	        } else if(botValues.getCurrDirection() == 75) {
    	        	//objDirection = "center";
       	        	botValues.setLeftSpeed(botValues.getMainSpeed() - 4);
       	        	//TextLCD.print("cent");
    	        	
    	        } else if(botValues.getCurrDirection() <= 69 && botValues.getCurrDirection() >= 60) {
    	        	//objDirection = "right";
       	        	botValues.setLeftSpeed(botValues.getMainSpeed() - 2);
       	        	//TextLCD.print("rght");
       	        	
    	        } 
       	        return 5;
        	}
        	
        	public int nextState() {
        		return 2;
        	}
        };

        actions[2] = new Action() {
        	
    	    public int act() {
    	    	motor.drive(botValues.getLeftSpeed(),botValues.getRightSpeed());
		        
		        //try { Thread.sleep(250); } catch (InterruptedException e) { }    	    	
    	    	
    	    	return 5;
    	    }
    	    public int nextState() {
    	    	return 3;
    	    }
        };
        
        actions[3] = new Action() {
        	
    	    public int act() {
    	    	botValues.setClockCycles(botValues.getClockCycles()+1);
    	    	//LCD.showNumber(botValues.getCurrDirection());
    	    	if(botValues.getViewButtonIndex() == 0) {
   	    			LCD.showNumber(botValues.getCurrDistance());
    	    	} else if(botValues.getViewButtonIndex() == 1) {
   	    			LCD.showNumber(botValues.getCurrDirection());
    	    	} else if(botValues.getViewButtonIndex() == 2) {
   	    			LCD.showNumber(botValues.getMainSpeed());
    	    	} else if(botValues.getViewButtonIndex() == 3) {
   	    			LCD.showNumber(botValues.getLeftSpeed());
    	    	} else if(botValues.getViewButtonIndex() == 4) {
   	    			LCD.showNumber(botValues.getRightSpeed());
    	    	} else if(botValues.getViewButtonIndex() == 5) {
   	    			LCD.showNumber(botValues.getFreeMemory());
    	    	} else if(botValues.getViewButtonIndex() == 6) {
   	    			LCD.showNumber(botValues.getCurrentVoltage());
    	    	} else if(botValues.getViewButtonIndex() == 7) {
   	    			LCD.showNumber(botValues.getClockCycles());
    	    	} else {
   	    			TextLCD.print("none");
    	    	}
    	    	
    	    	LCD.showProgramNumber(botValues.getViewButtonIndex());
		        
		        //try { Thread.sleep(250); } catch (InterruptedException e) { }    	    	
    	    	
    	    	return 5;
    	    }
    	    public int nextState() {
    	    	return Action.END;
    	    }
        };
        return actions;
    } //end monitorBot()    

    /**
     * The <tt>checkMemory</tt> monitors the memory state.
     * @return
     */
    public static Action[] checkMemory(BotValues botval) {
        Action[] actions = new Action[1];
        botValues = botval;
        
        actions[0] = new Action() {
		    /* show available memory on lcd
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		    	botValues.setFreeMemory((int)Runtime.getRuntime().freeMemory());
		        return 5;
		    }
		
		    /* nothing else to do but go forward
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return Action.END;
		    }
        };
        
        return actions;
    }
    
    /**
     * The <tt>checkBattery</tt> monitors the battery state.
     * @return
     */
    public static Action[] checkBattery(BotValues botval) {
        Action[] actions = new Action[1];
        botValues = botval;
        
        actions[0] = new Action() {
		    /* show available memory on lcd
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		    	botValues.setCurrentVoltage(Battery.getVoltageMilliVolt()); 
		        return 5;
		    }
			
		    /* nothing else to do but go forward
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return Action.END;
		    }
        };
        
        return actions;

    }

    
    /**
     * The <tt>senseDistObjects</tt> returns an Action[] array for driving the bot forward.
     * @return action list
     */
    public static Action[] senseObjects(BotValues botval) {
        Action[] actions = new Action[1];
        botValues = botval;
        
        actions[0] = new Action() {
        
        
        	
    	    public int act() {
    	    	botValues.setCurrDistance(Sensor.S1.readValue());
    	    	botValues.setCurrDirection(Sensor.S2.readValue());
    	    	
    	    	return 10;
    	    }
        	
    	    public int nextState() {
    	    	return Action.END;
    	    }
        };
        
        return actions;
    } //end sendDistObjects()
	
    /**
     * The <tt>collision</tt> returns an Action[] array for driving the bot forward.
     * @return action list
     * @see org.hailfire.motor#turnBot()
     */
    public static Action[] collision(String side) {
        Action[] actions = new Action[4];
        
        // stop 
        actions[0] = new Action() {
		    /* turn all motor motors off and cut power.
		     * @see org.hailfire.Action#act()
		     * @see org.hailfire.motors#allStop()
		     */
		    public int act() {
		        motor.allStop();
		        try{Thread.sleep(1000);} catch(Exception e) {}
		        return 250;
		    }
		
		    /* goto next action step
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return 1;
		    }
        };
        
        // back up 
        actions[1] = new Action() {
		    /* go backwards at full speed.
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		        motor.drive(-7,-7);
		        try{Thread.sleep(1000);} catch(Exception e) {}
		        return 100;
		    }
		
		    /* goto next action step
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return 2;
		    }
        };        
        

        // turn 
        actions[2] = new Action() {
		    /* turn off right motor and set left motor to full power
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		        motor.rotate("left",7);
		        try{Thread.sleep(1500);} catch(Exception e) {}
		        return 500;
		    }
		
		    /* nothing else to do but go forward
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return 3;
		    }
        };

        // all stop
        actions[3] = new Action() {
		    /* call allStop()
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		        motor.allStop();
		        return 500;
		    }
		
		    /* nothing else to do but go forward
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return Action.END;
		    }
        };
        
        return actions;
    } //end action collision    
    
    /**
     * The <tt>doDriveForward</tt> returns an Action[] array for driving the bot forward.
     * @return action list
     * @see org.hailfire.motor#goForward()
     * @version 1.5 - 08/19/06 recreating again after several drive failures
     * @param mainBotValObj 
     */
    public static Action[] changeView(BotValues botval) {
        Action[] actions = new Action[1];
        botValues = botval;
        
        // go forward
        actions[0] = new Action() {
		    /* go forward at full speed.
		     * @see org.hailfire.Action#act()
		     */
		    public int act() {
		        TextLCD.print("view");
		        return 5;
		    }
		
		    /* nothing else to do but go forward
		     * @see org.hailfire.Action#nextState()
		     */
		    public int nextState() {
		        return  Action.END;
		    }
        };
        
        return actions;
    }    
    
}//end class
   	        
    	        
    	        
    	        
    	        
    	        
