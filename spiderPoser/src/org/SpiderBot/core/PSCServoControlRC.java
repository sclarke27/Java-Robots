// Controling a Servo Motor on a Parallax Servo Controller via a RF Controller
// Combination of PSCServoControl.java and PulseClass1.java

import stamp.core.*;

public class PSCServoControlRC {

  // ----------Class Variables----------
  static Uart scBaud = new Uart(Uart.dirTransmit,CPU.pin13,Uart.dontInvert,Uart.speed2400,Uart.stop1);
  static Uart txUart = new Uart(Uart.dirTransmit,CPU.pin13,Uart.dontInvert,Uart.speed38400,Uart.stop1); //Creates a Uart for Communication
  static int stickPos;
  static char servoPos;

  // ----------Constructors----------


  public static void main() {
  //Set PSC Baud rate to 38400
  CPU.delay(5000);              //Wait for servo controler to boot
  scBaud.sendString("!SCSBR");   //Set baud
  scBaud.sendByte(1);            //Set baud to 38400
  scBaud.sendByte(0x0D);
  CPU.delay(5000);              //Wait for servo controler to get new baud

  System.out.println("Welcome to Servo Control: ");
 if (CPU.pulseIn(15000,CPU.pin0, true) < 100){
  System.out.println("RC Off ");
 }
 else{
  System.out.println("RC On  ");
 }

    while (true) {
      stickPos = CPU.pulseIn(15000,CPU.pin0, true);
//      System.out.println(n);
      if(stickPos <= 160){
        servoPos = 'l';
      }
      else if(stickPos >= 190){
        servoPos = 'r';
      }
      else{
        servoPos = 'c';
      }

      switch (servoPos){
      case 'r':
        PSCServoControlRC.update(1000);
        break;

      case 'l':
        PSCServoControlRC.update(500);
        break;

      case 'c':
        PSCServoControlRC.update(750);
        break;
      }
    }
  }
    static void update(int pulseWidth){
      //Code to send new position for servo to PSC
      txUart.sendString("!SC");    //Servo Controller
      txUart.sendByte(13);              //Channel that the Servo is on (0-15)
      txUart.sendByte(0);             //Ramp Value (speed at which servo will attempt to reach new position)
      txUart.sendByte(pulseWidth); //Value you want the servo to move to (250-1250,750=center)
      txUart.sendByte(pulseWidth>>>8);
      txUart.sendByte(0x0D);             //End command
      }
}