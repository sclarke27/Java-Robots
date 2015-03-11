package stamp.peripheral.sensor;
import stamp.core.*;
/**
* This class provides an interface to the Parallax PING))) ultrasonic
* range finder module.
* <p>
* <i>Usage:</i><br>
* <code>
* Ping range = new Ping(CPU.pin0); // trigger and echo on P0
* </code>
* <p>
* Detailed documentation for the PING))) Sensor can be found at: <br>
* http://www.parallax.com/detail.asp?product_id=28015
* <p>
*
* @version 1.0 03 FEB 2005
*/
public final class Ping {
private int ioPin;
/**
* Creates PING))) range finder object
*
* @param ioPin PING))) trigger and echo return pin
*/
public Ping (int ioPin) {
this.ioPin = ioPin;
}
/**
* Returns raw distance value from the PING))) sensor.
*
* @return Raw distance value from PING)))
*/
public int getRaw() {
int echoRaw = 0;
CPU.writePin(ioPin, false); // setup for high-going pulse
CPU.pulseOut(1, ioPin); // send trigger pulse
echoRaw = CPU.pulseIn(2171, ioPin, true); // measure echo return
// return echo pulse if in range; zero if out-of-range
return (echoRaw < 2131) ? echoRaw : 0;
}
/*
* The PING))) returns a pulse width of 73.746 uS per inch. Since the
* Javelin pulseIn() round-trip echo time is in 8.68 uS units, this is the
* same as a one-way trip in 4.34 uS units. Dividing 73.746 by 4.34 we
* get a time-per-inch conversion factor of 16.9922 (x 0.058851).
*
* Values to derive conversion factors are selected to prevent roll-over
* past the 15-bit positive values of Javelin Stamp integers.
*/
/**
* @return PING))) distance value in inches
*/
public int getIn() {
return (getRaw() * 3 / 51); // raw * 0.058824
}
/**
* @return PING))) distance value in tenths of inches
*/
public int getIn10() {
return (getRaw() * 3 / 5); // raw / 1.6667
}
/*
* The PING))) returns a pulse width of 29.033 uS per centimeter. As the
* Javelin pulseIn() round-trip echo time is in 8.68 uS units, this is the
* same as a one-way trip in 4.34 uS units. Dividing 29.033 by 4.34 we
* get a time-per-centimeter conversion factor of 6.6896.
*
* Values to derive conversion factors are selected to prevent roll-over
* past the 15-bit positive values of Javelin Stamp integers.
*/
/**
* @return PING))) distance value in centimeters
*/
public int getCm() {
return (getRaw() * 3 / 20); // raw / 6.6667
}
/**
* @return PING))) distance value in millimeters
*/
public int getMm() {
return (getRaw() * 3 / 2); // raw / 0.6667
}
}