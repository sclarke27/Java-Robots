package org.RCCar;

import stamp.core.CPU;
import stamp.core.Timer;
import stamp.core.Uart;
import stamp.core.Terminal;
import stamp.peripheral.servo.psc.psc;
import stamp.peripheral.appmod.LcdTerminal;

class Main {

    public static void main() {
    	
		LcdTerminal display = new LcdTerminal();
        
		//begin main loop
		while (true) {

            display.clearScr();
            CPU.delay(5000);
            display.write("PARALLAX");
			
            
		} //end loop
		
    } //end main

} // end class