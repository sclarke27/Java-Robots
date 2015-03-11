/*
 * Created on Jul 17, 2004
 *
 */
package org.hailfire;

import josx.platform.rcx.*;

/**
 * The <tt>motors</tt> classes send the actual commands to the motors via the Motor class.
 * All other classes call this class to move the bot.
 * left motor = C
 * center motor = B //motor B is not used for locomotion
 * right motor = A
 * 
 * @author scott
 * @version 0.1 - 07/17/04 created
 * @version 0.2 - 07/18/04 added constructor, documentation
 * @version 1.5 - 08/19/06 recreating again after several drive failures 
 */
public class motors {
    
    private Motor leftMotor = Motor.A;
    private Motor rightMotor = Motor.C;

    /*
     * default contstuctor to create <tt>motors</tt> object.
     */
    public motors() {
        super();
    }


    /*
     * The <tt>goForward</tt> method tells the A and C motors to go foward at power level <tt>speed</tt> (0-8)
     * @params direction "forwards" or "backward"
     * @params speed integer value from 0 to 8
     */
    public void drive(int lSpeed, int rSpeed) {
        //TextLCD.print("drive");
        //try{Thread.sleep(1000);} catch(Exception e) {}
        if(lSpeed < 0) {
            leftMotor.backward();
            lSpeed = lSpeed * -1;
        } else if(lSpeed == 0) {
            leftMotor.stop();
        } else {
            leftMotor.forward();
        }
        
        if(rSpeed < 0) {
            rightMotor.backward();
            rSpeed = rSpeed * -1;
        } else if(rSpeed == 0) {
            rightMotor.stop();
        } else {
            rightMotor.forward();
        }
        

        leftMotor.setPower(lSpeed);
        rightMotor.setPower(rSpeed);
        
    }
    
    /*
     * The <tt>turn</tt> method tells the A and C motors to go foward at different power levels
     * Used for making wide turns. Use rotate() for turning in place. 
     * using <tt>lSpeed</tt> and <tt>rSpeed</tt>.
     * @params direction "forwards" or "backward"
     * @params lSpeed integer value from 0 to 8
     * @params rSpeed integer value from 0 to 8
     */
    public void turn(String direction, int lSpeed, int rSpeed) {
        //TextLCD.print("turn");
        if(direction.equals("backward")) {
            leftMotor.backward();
            rightMotor.backward();
        } else {
            leftMotor.forward();
            rightMotor.forward();
        }
        
        leftMotor.setPower(lSpeed);
        rightMotor.setPower(rSpeed);
        
    }    

    /*
     * The <tt>rotate</tt> method tells the A and C motors to go opposite directions at the same power level.
     * Used for to rotate the bot in place.  
     * using <tt>lSpeed</tt> and <tt>rSpeed</tt>.
     * @params direction "left" or "right"
     * @params lSpeed integer value from 0 to 8
     * @params rSpeed integer value from 0 to 8
     */
    public void rotate(String direction, int speed) {
        //TextLCD.print(direction);
        if(direction.equals("left")) {
            leftMotor.backward();
            rightMotor.forward();
        } else {
            leftMotor.forward();
            rightMotor.backward();
        }
        
        leftMotor.setPower(speed);
        rightMotor.setPower(speed);
        
    }    
    
    /*
     * The <tt>allStop</tt> method tells the all 3 motors to stop and cuts all power.
     */    
    public void allStop() {
        leftMotor.setPower(0);
        rightMotor.setPower(0);
        
        leftMotor.stop();
        rightMotor.stop();
    }


    /**
     * @param string
     * @return
     */
    public int getCurrentPower(String motor) {
        int currPower = 7;
        if(motor == "A") {
            currPower = leftMotor.getPower();
        }
        if(motor == "C") {
            currPower = rightMotor.getPower();
        }

        return currPower;
    }


    /**
     * @param newPower
     */
    public void changeSpeed(int newPower) {
        leftMotor.setPower(newPower);
        Motor.B.setPower(newPower);
        rightMotor.setPower(newPower);
        
    }
  
}
