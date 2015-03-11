package org.SpiderBot.sensors;

import stamp.core.*;
import stamp.math.IntegerMath;

/**
 * Class for HM55B compass.
 *
 * How to use this class:
 *
 *  import stamp.peripheral.mems.*;
 *
 *  //define the compass pins
 *  static final int cp1_DinDout = CPU.pin0;
 *  static final int cp1_Clk = CPU.pin1;
 *  static final int cp1_En = CPU.pin2;
 *
 *  //define your compass
 *  static Compass cp1 = new Compass(cp1_DinDout,cp1_Clk,cp1_En);
 *
 *  Start a measurement by calling cp1.start()
 *  Then do some other tasks.
 *  Call cp1.poll() to check if measurement is completed.
 *  If it is (poll returned true) then call getCompassX(), getCompassY and/or getCompassAngle().
 *  After poll returned true, start another measurement by calling cp1.start() again.
 *
 */

public class Compass {

  private static final int RESET = 0x0000;
  private static final int MEASURE = 0x0008;
  private static final int REPORT = 0x000c;
  private static final int READY = 0x000c;

  private int m_DinDout; //pin DinDout
  private int m_Clk;     //pin CLK
  private int m_En;      //pin /EN

  private int m_x;       //x-axis value
  private int m_y;       //y-axis value

  /**
   * Constructor.
   *
   * @param DinDout pin for bidirectional communication
   * @param Clk output pin for serial clock
   * @param En output pin for chip enable
   */
  public Compass(int DinDout, int Clk, int En) {
    this.m_DinDout = DinDout;
    this.m_Clk = Clk;
    this.m_En = En;
    CPU.writePin(m_Clk,false); //initialize clock pin to low output
    CPU.writePin(m_En,true); //initialize enable pin to high output
    reset();
  }

  /**
   * Get compass x-axis value.
   *
   * @return x-axis value.
   */
  public int getCompassX() {
    return m_x;
  }

  /**
   * Get compass y-axis value.
   *
   * @return y-axis value.
   */
  public int getCompassY() {
    return m_y;
  }

  /**
   * Get compass angle.
   *
   * @return angle in degrees (0-359).
   */
  public int getCompassAngle() {
    return IntegerMath.atan2(-m_y, m_x);
  }

  /**
   * Reset compass.
   */
  public void reset() {
    CPU.writePin(m_En, false);
    CPU.shiftOut(m_DinDout, m_Clk, 4, CPU.SHIFT_MSB,RESET);
    CPU.writePin(m_En, true);
  }

  /**
   * Start compass measurement.
   */
  public void start() {
    CPU.writePin(m_En, false);
    CPU.shiftOut(m_DinDout, m_Clk, 4, CPU.SHIFT_MSB, MEASURE << 12);
  }

  /**
   * Poll compass for completion of measurement.
   *
   * @return true if new data, false otherwise.
   */
  public boolean poll() {
    boolean result;
    int m_status;
    CPU.writePin(m_En, true);
    CPU.writePin(m_En, false);
    CPU.shiftOut(m_DinDout, m_Clk, 4, CPU.SHIFT_MSB, REPORT << 12);
    m_status = CPU.shiftIn(m_DinDout, m_Clk, 4, CPU.POST_CLOCK_MSB );
    result = (m_status == READY);
    if (result) {
      m_x = CPU.shiftIn(m_DinDout, m_Clk, 11, CPU.POST_CLOCK_MSB );
      m_y = CPU.shiftIn(m_DinDout, m_Clk, 11, CPU.POST_CLOCK_MSB );
      CPU.writePin(m_En, true);
      if ((m_x & 0x0400) != 0) m_x |= (short)0xF800;
      if ((m_y & 0x0400) != 0) m_y |= (short)0xF800;
    }
    return result;
  }

}

