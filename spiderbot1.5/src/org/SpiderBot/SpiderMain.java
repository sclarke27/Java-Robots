package org.SpiderBot;

import stamp.core.CPU;
import stamp.core.Timer;
import stamp.core.Uart;
import stamp.core.Terminal;
import stamp.peripheral.servo.psc.psc;
import org.SpiderBot.core.*;
import org.SpiderBot.io.SerialLCD;
import org.SpiderBot.sensors.Compass;
import org.SpiderBot.sensors.Ping;

class SpiderMain {

	static final int LCD_PIN = CPU.pin12;
	static final int PSC_T_PIN = CPU.pin9;
	static final int PSC_R_PIN = CPU.pin8;
	static final int PING_TRIG_PIN = CPU.pin14;
	static final int PING_ECHO_PIN = CPU.pin14;
	static final int pulsePin1 = CPU.pin13;
	static final int pulsePin2 = CPU.pin15;
	static final int compassDinDout = CPU.pin1;
	static final int compassClk = CPU.pin2;
	static final int compassEn = CPU.pin3;
	
	static int panServo = 14; 	// set head pan servo channel
	static int tiltServo = 15; 	// set head tilt servo channel
	static StringBuffer message = new StringBuffer();
	static boolean monitorRC = false;	
	static int i;

	static Compass compass = new Compass(compassDinDout,compassClk,compassEn);
	static Uart pscIOT = new Uart(Uart.dirTransmit, PSC_T_PIN, Uart.dontInvert, Uart.speed2400, Uart.stop2);
	static Uart pscIOR = new Uart(Uart.dirReceive, PSC_R_PIN, Uart.dontInvert, Uart.speed2400, Uart.stop2);
	static psc myPsc = new psc(pscIOT, pscIOR, 30, PSC_T_PIN, PSC_R_PIN);
	static Ping pingObj = new Ping(PING_TRIG_PIN, PING_ECHO_PIN);
	static Uart lcdTxOut = new Uart(Uart.dirTransmit, LCD_PIN, Uart.dontInvert, Uart.speed19200, Uart.stop1);
	static SerialLCD lcdDisplay = new SerialLCD(lcdTxOut);
	static Servos servos = new Servos(myPsc);
	static Actions botActions = new Actions(myPsc, servos, pscIOT, pscIOR, panServo, tiltServo, compass, pingObj, pulsePin1, pulsePin2);
	
    public static void main() {
    	
		//init bot
		botActions.initializeBot();
		//servos.liftScreen(2);
		botActions.checkStandingHeight();
		//botActions.checkFacingDirection();
		//botActions.walkForward(1);
		//botActions.checkStandingHeight();
		
	    Timer t1 = new Timer();  // Create a timer VP.
	    Timer t2 = new Timer();  // Create a timer VP.
	    Timer t3 = new Timer();  // Create a timer VP.
	    t1.mark(); // Take a snapshot of the current time.
	    t2.mark(); // Take a snapshot of the current time.
	    t3.mark(); // Take a snapshot of the current time.
		
		
		//being main loop
		while (true) {
			
			// if keypress handle that, otherwise be autonomous
			if(Terminal.byteAvailable()) {
				//handle key press
				botActions.handleKeyPress(Terminal.getChar());
			} else {
//				 monitor RC controller if enabled
				if(botActions.monitorRCon) {
					//botActions.monitorRC();
					
				} else { // else run autonomously
					
					if ( t1.timeout(10000) ) { 
						// check direction
						//botActions.checkFacingDirection();
						t1.mark();
					} else if ( t2.timeout(30000) ) { 
						// check direction
						botActions.avoidObjects();
						t2.mark();
					//} else if ( t3.timeout(240000) ) { 
						// check direction
						//botActions.checkStandingHeight();
						//t3.mark();
					} else {
						botActions.walkForward(2);
						
					}
					
					
				}
			}
			
			
		} //end looop
		
    } //end main

} // end end