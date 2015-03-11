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
public class psc_onepin_test {

  static Uart pscIO = new Uart(Uart.dirReceive,CPU.pin0,Uart.dontInvert,Uart.speed2400,Uart.stop1);
  static psc myPsc = new psc(pscIO,pscIO,1); //single PSC board with 1 servo

  static void initServos() {
    myPsc.initChannel(0,250,1250,-750,3); //52428 is 65536*fraction(1800/(1250-250))
  }

  static void main() {
    int c,angle;
    System.out.println("PSC test program - one pin connection\n");
    initServos();
    CPU.delay(10500);         //wait 1 second
    while (true) {
      //perform terminal task
      if (Terminal.byteAvailable()) {
        c = Terminal.getChar(); //get character from IDE message window
        switch (c) {
          case 'l': myPsc.setAngle(0,7,1800); //move servo to far left at rate 7
                    CPU.delay(10500);         //wait 1 second
                    break;
          case 'c': myPsc.setAngle(0,7,900); //move servo to center at rate 7
                    CPU.delay(10500);        //wait 1 second
                    break;
          case 'r': myPsc.setAngle(0,7,0);   //move servo to far right at rate 7
                    CPU.delay(10500);        //wait 1 second
                    break;
          case 'm': myPsc.setAngle(0,7,1350); //move servo to center left at rate 7
                    CPU.delay(10500);         //wait 1 second
                    break;
          case 'p': myPsc.setAngle(0,7,450); //move servo to center right at rate 7
                    CPU.delay(10500);        //wait 1 second
                    break;
        }
        //test bidirectional connection
        angle = myPsc.getAngle(0);
        System.out.print(" Angle = ");
        System.out.print(angle/10);     //should display 0.0 to 180.0
        System.out.print(".");
        System.out.println(angle%10);
      }
    }
  }

}