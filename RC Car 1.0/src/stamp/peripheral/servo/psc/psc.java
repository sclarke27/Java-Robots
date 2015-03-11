package stamp.peripheral.servo.psc;
//import org.SpiderBot.io.SerialLCD;

import stamp.core.*;
import stamp.math.*;

/**
 * Class for Parallax Servo Controller (aka PSC).
 * It allows servo angle control which is 0.0 to 180.0 degrees (0.1 degree resolution) for all servos.
 * The fact that different servos may have different position ranges, is hidden in the class.
 *
 * Javelin can connect to PSC via one pin or two pins:
 *
 * One pin:   o---JavPin--------PscPin---o (this pin has 220E pullup resistor with LED)
 *            In this case receive uart equals transmit uart
 *            static Uart pscIO = new Uart(Uart.dirReceive,CPU.pin0,Uart.dontInvert,Uart.speed2400,Uart.stop1);
 *            static psc myPsc = new psc(pscIO,pscIO,3); //single PSC board with 3 servos
 *
 * Two pins:  o---JavRxPin-+----PscPin---o (this pin has 220E pullup resistor with LED)
 *                         |
 *                  [220 ohm resistor]
 *                         |
 *            o---JavTxPin-+
 *            In this case there are seperate receive uart and transmit uart
 *            and anything sent is directly received (local echo)
 *            static Uart pscTx = new Uart(Uart.dirTransmit,CPU.pin0,Uart.dontInvert,Uart.speed2400,Uart.stop1);
 *            static Uart pscRx = new Uart(Uart.dirReceive,CPU.pin1,Uart.dontInvert,Uart.speed2400,Uart.stop1);
 *            static psc myPsc = new psc(pscTx,pscRx,3); //single PSC board with 3 servos
 *
 * In main() initialize servos
 *    myPsc.initChannel(0,250,1250,-750,0);
 *    myPsc.initChannel(1,250,1250,-750,0);
 *    myPsc.initChannel(2,250,1250,-750,0);
 *
 * @version 1.5
 * @author Peter Verkaik (peterverkaik@boselectro.nl)
 * Date 12-dec-2005
 */

public class psc {

  public int[] Pmin; //default 250
  public int[] Pmax; //default 1250
  public int[] Phome; //default 750
  public int cpuTPin;
  public int cpuRPin;
  private Uart rx;
  private Uart tx;
  private char[] reply = new char[6]; //holds replies
  private boolean networked;
  private StringBuffer pscResponse = new StringBuffer();

  /**
   * Constructor for PSC.
   *
   * @param tx Transmit uart (2400 baud, 8N1)
   * @param rx Receive uart (2400 baud, 8N1)
   * @param channels Number of servos
   *                 1-16 for single PSC board
   *                 17-32 for two networked PSC boards
   */
  public psc(Uart tx, Uart rx, int channels, int cpuTPin, int cpuRPin) {
    this.tx = tx;
    this.rx = rx;
    this.cpuTPin = cpuTPin;
    this.cpuRPin = cpuRPin;
    this.Pmin = new int[channels];
    this.Pmax = new int[channels];
    this.Phome = new int[channels];
    this.networked = true;//(channels>16); //two PSC boards are networked if more than 16 channels
  }
  
  public void initPsc(Uart pscIOT, Uart pscIOR) {
	StringBuffer tempStr = new StringBuffer();
	//Set PSC Baud rate to 38400
	CPU.delay(5000);               //Wait for servo controler to boot
//	SerialLCD.clearLine(2);
//	SerialLCD.writeLine("change T baud to 38.4", 2);
	pscIOT.sendString("!SCSBR");   //Set baud
	pscIOT.sendByte(1);            //Set baud to 38400
	pscIOT.sendByte(0x0D);
	CPU.delay(15000);               //Wait for servo controler to get new baud

	//Restart the Uart with the baud rate set to 38400
//	SerialLCD.clearLine(2);
//	SerialLCD.writeLine("restart UART", 2);
	pscIOT.restart(Uart.dirTransmit,cpuTPin,Uart.dontInvert,Uart.speed38400,Uart.stop1);	    	
	pscIOR.restart(Uart.dirReceive,cpuRPin,Uart.dontInvert,Uart.speed2400,Uart.stop1);
	CPU.delay(5000); 
	this.tx = pscIOT;
	this.rx = pscIOR;
  }  

  /**
   * Initialize PSC channel.
   *
   * @param channel Channel to initialize
   * @param Pmin Minimum position value, default 250.
   *             If your servos appear to strain when commanded to position 250, you should increase
   *             the 250 to 260 or so to prevent the servo from straining itself.
   * @param Pmax Maximum position value, default 1250.
   *             If your servos appear to strain when commanded to position 1250, you should decrease
   *             the 1250 to 1240 or so to prevent the servo from straining itself.
   * @param home Angle (0.1 degree units 0-1800) or position (-Pmin to -Pmax) to identify home position
   * @param ramp Specifies time to get to home position (0-63)
   *             If the ramp parameter is set to 0, ramping is disabled and the pulse width will be set to the
   *             position parameter. Ramp values of 1-63 correspond to speeds from 0.75 second up to 60 seconds
   *             for a full 500uSec to 2.50 mSec excursion.
   */
  public void initChannel(int channel, int Pmin, int Pmax, int home, int ramp) {
    this.Pmin[channel] = Pmin;
    this.Pmax[channel] = Pmax;
    this.Phome[channel] = (home < 0) ? -home : position(channel,home);
    setHome(channel,ramp); //move servo to home position at rate ramp
  }

  //VER? Command – Identify Firmware Version Number
  //Syntax: “!SCVER?” $0D
  //Reply: “1.3”
  /**
   * Get PSC firmware version.
   * In case of networked boards, this method returns the version of the first board.
   *
   * @return Version number expressed as ASCII characters in an int.
   *         If version is "1.3" then return value is 0x3133
   */
  /*  
  public int version() {
    if (tx == rx) tx.setDirection(Uart.dirTransmit);
    tx.sendString("!SCVER?\r");
    getReply(8,3);
    return (reply[0]<<8)|(reply[2]&0xFF);
  }
*/
  /**
   * Get PSC firmware version.
   * In case of networked boards, this method returns the version of the second board.
   * Method version() must be called prior to this method.
   *
   * @return Version number expressed as ASCII characters in an int.
   *         If version is "1.3" then return value is 0x3133
   */
/*
  public int version2() {
    return (reply[3]<<8)|(reply[5]&0xFF);
  }
*/
  //SBR Command – Set the Baudrate (to either 2400 or 38K4 Baud)
  //Syntax: “!SCSBR” x $0D
  //Reply: “BR” x (where x is either 0 for 2400, or 1 for 38K4)
  //As Javelin may have trouble receiving at 38K4, this is not available

  /**
   * Move servo to home position.
   *
   * @param channel Servo to move to home position
   * @param ramp Specifies time to get to position (0-63)
   *             If the ramp parameter is set to 0, ramping is disabled and the pulse width will be set to the
   *             position parameter. Ramp values of 1-63 correspond to speeds from 0.75 second up to 60 seconds
   *             for a full 500uSec to 2.50 mSec excursion.
   */
  public void setHome(int channel, int ramp) {
    setPosition(channel,ramp,Phome[channel]);
  }

  //Position Command – Set the Position of a Servo Channel
  //Syntax: “!SC” C R pw.LOWBYTE, pw.HIGHBYTE, $0D
  //Reply: none
  /**
   * Set PSC servo position
   *
   * @param channel Servo channel (0-31)
   * @param ramp Specifies time to get to position (0-63)
   *             If the ramp parameter is set to 0, ramping is disabled and the pulse width will be set to the
   *             position parameter. Ramp values of 1-63 correspond to speeds from 0.75 second up to 60 seconds
   *             for a full 500uSec to 2.50 mSec excursion.
   * @param position Servo position to get to (Pmin to Pmax)
   */
  public void setPosition(int channel, int ramp, int position) {
    //if (tx == rx) { 
    	//tx.restart(Uart.dirTransmit,cpuPin,Uart.dontInvert,Uart.speed38400,Uart.stop1);	
    //}
    tx.sendString("!SC");
    tx.sendByte(channel);
    tx.sendByte(ramp);
    tx.sendByte(position);
    tx.sendByte(position>>>8);
    tx.sendByte(0x0D);
    //getReply(8,0);
  }

  /**
   * Set PSC servo angle
   *
   * @param channel Servo channel (0-31)
   * @param ramp Specifies time to get to position (0-63)
   *             If the ramp parameter is set to 0, ramping is disabled and the pulse width will be set to the
   *             position parameter. Ramp values of 1-63 correspond to speeds from 0.75 second up to 60 seconds
   *             for a full 500uSec to 2.50 mSec excursion.
   * @param angle Servo angle (0.0-180.0 degrees) to get to (value 0-1800)
   */
  public void setAngle(int channel, int ramp, int angle) {
    setPosition(channel,ramp,position(channel,angle));
  }

  //RSP Command – Report the Position of a Servo Channel
  //Syntax: “!SCRSP” x $0D
  //Reply: x y z(where x is the channel number, and y:z is the value reported)
  /**
   * Get PSC servo position
   *
   * @param channel Servo channel (0-31)
   * @return Current servo position (Pmin to Pmax)
   */

  public int getPosition(int channel) {
	pscResponse.delete(0, pscResponse.length());
    //if (tx == rx) {
    	//tx.restart(Uart.dirReceive,cpuPin,Uart.dontInvert,Uart.speed19200,Uart.stop1);	
	//}
    tx.sendString("!SCRSP");
    tx.sendByte(channel);
    tx.sendByte(0x0D);
    getReply(8,3);
    int offset = (channel<16) ? 0 : 3;
    //SerialLCD.write(response);
    return (reply[offset+1]<<8)|(reply[offset+2]&0xFF);
    //return response;
  }

  /**
   * Get PSC servo angle
   *
   * @param channel Servo channel (0-31)
   * @return Current servo angle 0.0-180.0 degrees (value 0-1800)
   */

  public int getAngle(int channel) {
	//return getPosition(channel);
    return angle(channel,getPosition(channel));
  }

  //convert position to angle
  //default position range 1250-250=1000 equals 180 degrees, so angle resolution is 0.18 degrees
  //the conversion uses 0.1 degree resolution
  // A = (p-pmin)/(pmax-pmin)*1800
  //   = (p-pmin)*(1800/(pmax-pmin))
  //   = (p-pmin)*(I + F/65536)
  //   = (p-pmin)*I + UnsignedIntMath(p-pmin,F)
  /**
   * Convert positions Pmin to Pmax to degrees 0-180.
   *
   * @param channel Servo channel (0-31)
   * @param position Value Pmin[channel] to Pmax[channel].
   * @return Corresponding angle 0.0-180.0 degrees (value 0-1800).
   */
  public int angle(int channel, int position) {
    //formulae: A = (position-Pmin[channel])/(Pmax[channel]-Pmin[channel])*1800;
    int d = Pmax[channel] - Pmin[channel];
    int I = 1800/d;
    int offset = position - Pmin[channel];
    int frac = UnsignedIntMath.ufrac(1800,d);
    return (offset*I) + UnsignedIntMath.umulf(offset,frac);
  }

  //convert angle to position
  //default position range 1250-250=1000 equals 180 degrees, so angle resolution is 0.18 degrees
  //the conversion uses 0.1 degree resolution
  // P = (A/1800)*(pmax-pmin) + pmin
  //   = A * (I + F/65536) + pmin
  // (pmax-pmin)/1800 = I + (F/65536)
  // I = (pmax-pmin)/1800
  // R = pmax - pmin - 1800*I
  // F = 65536 * R / 1800 = 36.40888 * R
  //   = 36*R + 0.40888*R
  //   = ((9*R)<<2) + (26796.94222/65536)*R
  //   = ((9*R)<<2) + UnsignedIntMath.umulf(R,26797)
  // P = A * (I + F / 65536) + pmin
  //   = A*I + UnsignedIntMath(A,F) + pmin
  /**
   * Convert angle 0.0-180.0 degrees (value 0-1800) to position Pmin to Pmax.
   *
   * @param channel Servo channel (0-31)
   * @param angle Angle 0.0-180.0 (value 0 to 1800).
   * @return Corresponding position Pmin[channel] to Pmax[channel].
   */
  public int position(int channel, int angle) {
    //formulae: P = (angle*(Pmax[channel]-Pmin[channel])/1800)+Pmin[channel];
    int range = Pmax[channel]-Pmin[channel];
    int I = range/1800;
    int R = range - (1800*I);
    int F = ((9*R)<<2) + UnsignedIntMath.umulf(R,26797);
    return (angle*I) + UnsignedIntMath.umulf(angle,F) + Pmin[channel];
  }

  //If a command causes the PSC to reply, a three-byte reply is sent after a 1.5 mS delay.
  //It is possible to network two PSCs together to control up to 32 servos. Please note that in this configuration
  //when you send a serial command to one unit, you send it to both. Commands that set servo positions are the
  //only commands ignored by the other unit. All other commands invoke a reply.
  //Unit 0 replies first. Unit 1 replies approximately 3 mS later.
  /**
   * Get reply from PSC.
   * This method removes any local echoes that may have been received.
   *
   * @param echoes Number of bytes of the prior command
   * @param databytes Number of bytes in PSC reply
   */

  private void getReply(int echoes, int databytes) {
	    int i = 0;
	    if (networked) databytes <<= 1; //0 remains 0, 3 becomes 6
	    while (!tx.sendBufferEmpty()) ; //wait until transmit buffer empty and nothing is sent
	    //if (tx == rx) {
	    //	rx.setDirection(Uart.dirReceive);
	    //} else { //local echoes
	      while (echoes > 0) {
	        reply[0] = (char)rx.receiveByte();
	        echoes--;
	        if(i > 35) {
	        	echoes = 0;
	        } else {
	        	i++;
	        }
	      }
	    //}
	    i = 0;
	    //SerialLCD.clearScr();
	    //SerialLCD.writeLine("PSC Reply:", SerialLCD.LINE1);
	    while (i < databytes) {
	      reply[i] = (char)rx.receiveByte();
	      pscResponse.append(reply[i]);
	      i++;
	        if(i > 35) {
	        	i = databytes;
	        } 
	    }
	   // SerialLCD.moveTo(SerialLCD.LINE2, 0);
	   // SerialLCD.write(pscResponse);
	  }
  
  public int version() {
	    if (tx == rx) tx.setDirection(Uart.dirTransmit);
	    tx.sendString("!SCVER?\r");
	    getReply(8,3);
	    return (reply[0]<<8)|(reply[2]&0xFF);
	  }  
  		
}