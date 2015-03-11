//  File....... LCD_AppMod_Demo.java
//  Purpose.... Demonstrates the LCD Terminal AppMod
//  Author..... Parallax, Inc.  (Copyright 2003-2004, All Rights Reserved)
//  E-mail..... support@parallax.com
//  Started....
//  Updated.... 13 JAN 2004
//
//  This program demonstrates the use of the Parallax LCD Terminal AppMod
//  with the Javelin microcontroller. Custom character generation and animation
//  is demonstrated.

import stamp.core.*;
import stamp.peripheral.appmod.LcdTerminal;

public class LCD_AppMod_Demo {

  // custom characters for Pac-Man graphic
  static final char[] CC0    = {0x0E, 0x1F, 0x1C, 0x18, 0x1C, 0x1F, 0x0E, 0x00};
  static final char[] CC1    = {0x0E, 0x1F, 0x1F, 0x18, 0x1F, 0x1F, 0x0E, 0x00};
  static final char[] CC2    = {0x0E, 0x1F, 0x1F, 0x1F, 0x1F, 0x1F, 0x0E, 0x00};
  static final char[] Smiley = {0x00, 0x0A, 0x0A, 0x00, 0x11, 0x0E, 0x06, 0x00};


  public static void main() {

    LcdTerminal display = new LcdTerminal();

    StringBuffer msg = new StringBuffer();

    int buttons;                                // AppMod buttons value
    int dispChar;                               // display character
    //int mask;

    // create custom characters
    display.createChar5x7(0, CC0);              // mouth frame 0
    display.createChar5x7(1, CC1);              // mouth frame 1
    display.createChar5x7(2, CC2);              // mouth frame 2
    display.createChar5x7(3, Smiley);           // goofy smile

    while (true) {
      display.clearScr();
      CPU.delay(5000);
      display.write("PARALLAX");

      // scroll 'Javelin' and Smiley on LINE2
      msg.clear();
      msg.append("Javelin  \u0003");
      display.scroll(display.LINE2, msg);

      // Pac-Man animation - eat PARALLAX
      for (int col = 0; col < 8; col++) {
        for (int frame = 0; frame < 4; frame++) {
          display.moveTo(display.LINE1, col);
          dispChar = (frame < 3) ? frame : 32;  // print frame char or space
          display.write(dispChar);
          CPU.delay(750);
        }
      }

      // show button inputs
      display.clearScr();
      display.write("Buttons:");
      for(int i = 0; i < 150; i++) {
        display.moveTo(display.LINE2, 2);
        buttons = display.readButtons();
        for (int mask, pos = 0x00; pos <= 0x03; pos += 1) {
          mask = 0x001 << pos;
          dispChar = ((buttons & mask) != 0) ? (65 + pos) : 45;
          display.write(dispChar);
        }
      }
    }
  }
}