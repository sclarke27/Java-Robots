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
//   	 create SRF04 object (trigger on P0, echo on P1)
   	range = new SRF04(ping_trigger_pin, ping_echo_pin);
	}

	public static void testPing() {
        //while (true) {
            // create and display measurement message
            //msg.clear();
            //msg.append(CLR_SCR);
			SerialLCD.clearScr();

            // display raw return
            distance = range.getRaw();
            msg.append("Raw  = ");
            if (distance > 0)
              msg.append(distance);
            else
              msg.append("Out of Range");

            SerialLCD.writeLine(msg.toString(),1);
            msg = new StringBuffer();

            // display whole inches
            distance = range.getIn();
            msg.append("In   = ");
            if (distance > 0)
              msg.append(distance);
            else
              msg.append("Out of Range");

            SerialLCD.writeLine(msg.toString(),2);
            msg = new StringBuffer();
            /*
            // display fractional inches
            distance = range.getIn10();
            msg.append("In10 = ");
            if (distance > 0) {
              msg.append(distance / 10);    // whole part
              msg.append( ".");
              msg.append(distance % 10);    // fractional part
            }
            else
              msg.append("Out of Range");

            SerialLCD.writeLine(msg.toString(),4);
            msg = new StringBuffer();
			*/
            // display centimeters
            distance = range.getCm();
            msg.append("cm   = ");
            if (distance > 0)
              msg.append(distance);
            else
              msg.append("Out of Range");

            SerialLCD.writeLine(msg.toString(),3);
            msg = new StringBuffer();

            // display millimeters
            distance = range.getMm();
            msg.append("mm   = ");
            if (distance > 0)
              msg.append(distance);
            else
              msg.append("Out of Range");

            SerialLCD.writeLine(msg.toString(),4);
            msg = new StringBuffer();
            

            // wait 0.5 seconds between readings
            //CPU.delay(10000);
          //}    	
    }
	

}
