package org.SpiderBot.sensors;

import org.SpiderBot.io.SerialLCD;

import stamp.peripheral.devantech.*;
import stamp.core.CPU;

public class Ping {
	
    // create SRF04 object (trigger on P0, echo on P1)
    private static SRF04 range;
    // create message buffer for screen display
    private static StringBuffer msg = new StringBuffer();

    private static int distance;
    
    public Ping(int ping_trigger_pin, int ping_echo_pin) {
    	//create SRF04 object (trigger on P0, echo on P1)
    	range = new SRF04(ping_trigger_pin, ping_echo_pin);
	}

	public static void testPing() {

		SerialLCD.clearScr();

        // display raw return
        distance = range.getRaw();
        msg.append("Raw  = ");
        if (distance > 0)
          msg.append(distance);
        else
          msg.append("Out of Range");

        SerialLCD.writeLine(msg.toString(),1);
        msg.delete(0,msg.length());

        // display whole inches
        distance = range.getIn();
        msg.append("In   = ");
        if (distance > 0)
          msg.append(distance);
        else
          msg.append("Out of Range");

        SerialLCD.writeLine(msg.toString(),2);
        msg.delete(0,msg.length());

        // display centimeters
        distance = range.getCm();
        msg.append("cm   = ");
        if (distance > 0)
          msg.append(distance);
        else
          msg.append("Out of Range");

        SerialLCD.writeLine(msg.toString(),3);
        msg.delete(0,msg.length());

        // display millimeters
        distance = range.getMm();
        msg.append("mm   = ");
        if (distance > 0)
          msg.append(distance);
        else
          msg.append("Out of Range");

        SerialLCD.writeLine(msg.toString(),4);
        msg.delete(0,msg.length());

	}

	public int getObjDistance(char mesurementType) {
		// TODO Auto-generated method stub
		switch(mesurementType) {
			case 'r':
				distance = range.getRaw();
				break;
			case 'm':
				distance = range.getMm();
				break;
			case 'c':
				distance = range.getCm();
				break;
			case 'i':
				distance = range.getIn();
				break;
			default:
				distance = range.getRaw();
				break;
				
		}
		return distance;
	}
	

}
