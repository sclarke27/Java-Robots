package org.SpiderBot.sensors;

import stamp.core.*;

public class Compass {
/*

	   private static void getCompassAxes(){
		      int statusHM55B;
		      int resetHM55B = 0x0;             // Reset command for HM55B
		      int measureHM55B = 0x8;   // Start measurement
		      int reportHM55B = 0xC;    // Get status/axis values
		      int readyHM55B = 0xC;             // 1100 -> Done, 00 -> no errors
		      int mask = (short) 0xF800;

		      int clkHM55B;
		      CPU.writePin(clkHM55B, false); // Initialize Clk for delivering positive pulses.
		      CPU.writePin(enableHM55B, ON);
		      CPU.writePin(enableHM55B, OFF);  // Send reset command to HM55B
		      CPU.shiftOut(dinDout, clkHM55B, 4, CPU.SHIFT_MSB, resetHM55B<<12);
		      CPU.writePin(enableHM55B, ON);
		      CPU.writePin(enableHM55B, OFF);  // Send reset command to HM55B
		      CPU.shiftOut(dinDout, clkHM55B, 4, CPU.SHIFT_MSB, measureHM55B<<12);
		      statusHM55B = 0x0;   // Clear previous status flags
		 
		      do {    // Status flag checking loop
		        CPU.writePin(enableHM55B, ON);
		        CPU.writePin(enableHM55B, OFF);  // Send reset command to HM55B
		        CPU.shiftOut(dinDout, clkHM55B, 4, CPU.SHIFT_MSB, reportHM55B<<12);
		        statusHM55B = CPU.shiftIn(dinDout, clkHM55B, 4, CPU.POST_CLOCK_MSB );
		      } while (statusHM55B != readyHM55B);

		      //CPU.delay(200); //not necessary

		      x = CPU.shiftIn(dinDout, clkHM55B, 11, CPU.POST_CLOCK_MSB);
		      y = CPU.shiftIn(dinDout, clkHM55B, 11, CPU.POST_CLOCK_MSB);
		      CPU.writePin(enableHM55B, ON); //disable module
		    
		      if ((x & 0x400) != 0) x |= mask; //store 11 bits as signed word (range -1024 to +1023)
		      if ((y & 0x400) != 0) y |= mask; //ditto
		    }
*/	
}
