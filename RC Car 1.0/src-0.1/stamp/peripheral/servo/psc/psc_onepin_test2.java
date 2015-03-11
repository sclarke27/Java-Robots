import stamp.core.*;
import stamp.peripheral.servo.psc.*;

/**
 * Test program for Parallax Servo Controller (aka PSC).
 * A servo must be connected to channel 0. The program accepts 'commands' l,c,r,m and p
 * from the IDE message window to move the servo to a specific position.
 *   l: move servo to far left at rate 7
 *   c: move servo to center at rate 7
 *   r: move servo to far right at rate 7
 *   m: move servo to center left at rate 7
 *   p: move servo to center right at rate 7
 *
 * The javelin connects to PSC via one pin:
 *
 * One pin:   o---JavPin--------PscPin---o (this pin has 220E pullup resistor with LED)
 *            In this case receive uart equals transmit uart
 *            static Uart pscIO = new Uart(Uart.dirReceive,CPU.pin0,Uart.dontInvert,Uart.speed2400,Uart.stop1);
 *            static psc myPsc = new psc(pscIO,pscIO,3); //single PSC board with 3 servos
 *
 * @version 1.3
 * @author Peter Verkaik (peterverkaik@boselectro.nl)
 * Date 12-dec-2005
 */
public class psc_onepin_test2 {

  static Uart pscIO = new Uart(Uart.dirReceive,CPU.pin0,Uart.dontInvert,Uart.speed2400,Uart.stop1);
  static psc myPsc = new psc(pscIO,pscIO,1); //single PSC board with 1 servo

  static void initServos() {
    myPsc.initChannel(0,250,1250,-750,3);
  }

  static void main() {
    int c,pos, preset=0;
    System.out.println("PSC test program - one pin connection\n");
    initServos();
    CPU.delay(10500);         //wait 1 second
    while (true) {
      //perform terminal task
      if (Terminal.byteAvailable()) {
        c = Terminal.getChar(); //get character from IDE message window
        switch (c) {
          case 'l': preset = 1250;
                    myPsc.setPosition(0,7,1250); //move servo to far left at rate 7
                    CPU.delay(10500);            //wait 1 second
                    break;
          case 'c': preset = 750;
                    myPsc.setPosition(0,7,750);  //move servo to center at rate 7
                    CPU.delay(10500);            //wait 1 second
                    break;
          case 'r': preset = 250;
                    myPsc.setPosition(0,7,250);  //move servo to far right at rate 7
                    CPU.delay(10500);            //wait 1 second
                    break;
          case 'm': preset = 1000;
                    myPsc.setPosition(0,7,1000); //move servo to center left at rate 7
                    CPU.delay(10500);            //wait 1 second
                    break;
          case 'p': preset = 500;
                    myPsc.setPosition(0,7,500);  //move servo to center right at rate 7
                    CPU.delay(10500);            //wait 1 second
                    break;
          default:  c = 0;
        }
        //test bidirectional connection
        if (c != 0) {
          System.out.print(" Preset = ");
          System.out.print(preset);       //what position should be
          pos = myPsc.getPosition(0);
          System.out.print("   Position = ");
          System.out.println(pos);        //should display preset value
        }
      }
    }
  }

}