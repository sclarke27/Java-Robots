package org.SpiderBot.core;

import stamp.core.CPU;
import stamp.core.Uart;

public class Psc {

	private int pscPin = CPU.pin15;
	private Uart txUart;
	private Uart rxUart;
	private int channels = 30;
	private int[] Pmin = new int[channels];
	private int[] Pmax = new int[channels];
	private int[] Phome = new int[channels];
	private boolean networked = (channels>16); //two PSC boards are networked if more than 16 channels
	private char[] reply = new char[6]; //holds replies
	
	public Psc(Uart tx, Uart rx, int chnls, int cpuPin) {
		txUart = tx;
		rxUart = rx;
		channels = chnls;
		pscPin = cpuPin;
	}

	public void initPsc() {
		// Set PSC Baud rate to 38400
		CPU.delay(15000);               //Wait for servo controler to boot
		txUart.sendString("!SCSBR");   //Set baud
		txUart.sendByte(1);            //Set baud to 38400
		txUart.sendByte(0x0D);
		CPU.delay(15000);               //Wait for servo controler to get new baud

		// Restart the Uart with the baud rate set to 38400
		txUart.restart(Uart.dirTransmit,pscPin,Uart.dontInvert,Uart.speed38400,Uart.stop1); 
		
	}
	
    public void setPosition(int channel, int ramp, int pulseWidth){
    	txUart.setDirection(Uart.dirReceive);
        //Code to send new position for servo to PSC
        txUart.sendString("!SC");    //Servo Controller
        txUart.sendByte(channel);              //Channel that the Servo is on (0-30)
        txUart.sendByte(ramp);             //Ramp Value (speed at which servo will attempt to reach new position)
        txUart.sendByte(pulseWidth); //Value you want the servo to move to (250-1250,750=center)
        txUart.sendByte(pulseWidth>>>8);
        txUart.sendByte(0x0D);             //End command
     }

	public int getAngle(int servoChannel) {
	    txUart.setDirection(Uart.dirTransmit);
	    txUart.sendString("!SCRSP");
	    txUart.sendByte(servoChannel);
	    txUart.sendByte(0x0D);
	    getReply(8,3);
	    int offset = (servoChannel<16) ? 0 : 3;
	    return (reply[offset+1]<<8)|(reply[offset+2]&0xFF);
	    //return reply;
	}	
	
	public void initChannel(int channel, int Pmin, int Pmax, int home, int ramp) {
		this.Pmin[channel] = Pmin;
		this.Pmax[channel] = Pmax;
		this.Phome[channel] = (home < 0) ? -home : home;
		setHome(channel,ramp); //move servo to home position at rate ramp
	}
	
	public void setHome(int channel, int ramp) {
		setPosition(channel,ramp,Phome[channel]);
	}	
	
	private void getReply(int echoes, int databytes) {
		int i = 0;
		if (networked) databytes <<= 1; //0 remains 0, 3 becomes 6
		while (!txUart.sendBufferEmpty()) ; //wait until transmit buffer empty and nothing is sent
		txUart.setDirection(Uart.dirReceive);
		while (echoes > 0) {
			reply[0] = (char)txUart.receiveByte();
			echoes--;
		}
		while (i < databytes) {
			reply[i++] = (char)txUart.receiveByte();
		}
	}
}
