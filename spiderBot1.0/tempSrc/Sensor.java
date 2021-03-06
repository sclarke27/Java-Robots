package org.SpiderBot.core;

/**
 * Abstraction for a sensor (<i>considerably changed since alpha5</i>).
 * There are three Sensor instances available: Sensor.S1, Sensor.S2 and
 * Sensor.S3. They correspond to sensor inputs labeled 1, 2 and 3 in the
 * RCX, respectively. Before using a sensor, you should set its mode
 * and type with <code>setTypeAndMode</code> using constants defined in <code>SensorConstants</code>. 
 * You should also activate the sensor.
 * <p>
 * You can poll for sensor values in a loop using the readValue method
 * or one of the other read methods. There is also a low level method which
 * can be used when maximum performance is required. Another way to
 * monitor sensor values is to add a <code>SensorListener</code>. All sensor events
 * are dispatched to listeners by a single thread created by this class. The
 * thread is a daemon thread and so will not prevent termination of an
 * application if all other threads have exited.
 * <p>
 * Example:<p>
 * <code><pre>
 *   Sensor.S1.setTypeAndMode (3, 0x80);
 *   Sensor.S1.activate();
 *   Sensor.S1.addSensorListener (new SensorListener() {
 *     public void stateChanged (Sensor src, int oldValue, int newValue) {
 *       // Will be called whenever sensor value changes
 *       LCD.showNumber (newValue);
 *       try {
 *         Thread.sleep (100);
 *       } catch (InterruptedException e) {
 *         // ignore
 *       }
 *     }
 *   });
 *     
 * </pre></code>
 *
 * @see josx.platform.rcx.SensorConstants
 * @see josx.platform.rcx.SensorListener
 */
public class Sensor implements ListenerCaller
{
  private int iSensorId;
  private short iNumListeners = 0;
  private SensorListener[] iListeners;
  private int iPreviousValue;
  
  /**
   * Sensor labeled 1 on RCX.
   */
  public static final Sensor S1 = new Sensor (0);

  /**
   * Sensor labeled 2 on RCX.
   */   
  public static final Sensor S2 = new Sensor (1);
  
  /**
   * Sensor labeled 3 on RCX.
   */
  public static final Sensor S3 = new Sensor (2);

  /**
   * Array containing all three sensors [0..2].
   */
  public static final Sensor[] SENSORS = { Sensor.S1, Sensor.S2, Sensor.S3 };

  /**
   * Reads the canonical value of the sensor.
   */
  public final int readValue()
  {
    return readSensorValue (iSensorId, 1);
  }

  /**
   * Reads the raw value of the sensor.
   */
  public final int readRawValue()
  {
    return readSensorValue (iSensorId, 0);
  }

  /**
   * Reads the boolean value of the sensor.
   */
  public final boolean readBooleanValue()
  {
    return readSensorValue (iSensorId, 2) != 0;
  }

  private Sensor (int aId)
  {
    iSensorId = aId;
    setTypeAndMode (3, 0x80);
  }

  /**
   * Return the ID of the sensor. One of 0, 1 or 2.
   */
  public final int getId()
  {
    return iSensorId;
  }
    
  /**
   * Adds a sensor listener.
   * <p>
   * <b>
   * NOTE 1: You can add at most 8 listeners.<br>
   * NOTE 2: Synchronizing inside listener methods could result
   * in a deadlock.
   * </b>
   * @see josx.platform.rcx.SensorListener
   */
  public synchronized void addSensorListener (SensorListener aListener)
  {
    if (iListeners == null)
    {
        iListeners = new SensorListener[8];
    }
    iListeners[iNumListeners++] = aListener;
    ListenerThread.get().addSensorToMask(iSensorId, this);
  }

  /**
   * Activates the sensor. This method should be called
   * if you want to get accurate values from the
   * sensor. In the case of light sensors, you should see
   * the led go on when you call this method.
   */
  public final void activate()
  {
    //ROM.call ((short) 0x1946, (short) (0x1000 + iSensorId));
  }

  /**
   * Passivates the sensor. 
   */
  public final void passivate()
  {
    //ROM.call ((short) 0x19C4, (short) (0x1000 + iSensorId));
  }

  /**
   * Sets the sensor's mode and type. If this method isn't called,
   * the default type is 3 (LIGHT) and the default mode is 0x80 (PERCENT).
   * @param aType 0 = RAW, 1 = TOUCH, 2 = TEMP, 3 = LIGHT, 4 = ROT.
   * @param aMode 0x00 = RAW, 0x20 = BOOL, 0x40 = EDGE, 0x60 = PULSE, 0x80 = PERCENT,
   *              0xA0 = DEGC,
   *              0xC0 = DEGF, 0xE0 = ANGLE. Also, mode can be OR'd with slope (0..31).
   * @see josx.platform.rcx.SensorConstants
   */
  public final void setTypeAndMode (int aType, int aMode)
  {
    setSensorValue (iSensorId, aType, 1);
    setSensorValue (iSensorId, aMode, 0);     
  }

  /**
   * Resets the canonical sensor value. This may be useful for rotation sensors. 
   */
  public final void setPreviousValue (int aValue)
  {
    setSensorValue (iSensorId, aValue, 2);    
  }
  
  /**
   * <i>Low-level API</i> for reading sensor values.
   * @param aSensorId Sensor ID (0..2).
   * @param aRequestType 0 = raw value, 1 = canonical value, 2 = boolean value.
   */
  public static native int readSensorValue (int aSensorId, int aRequestType);
  
  private static native void setSensorValue (int aSensorId, int aVal, int aRequestType);
  
  public synchronized void callListeners() {
    int newValue = readSensorValue( iSensorId, 1);
    for (int i = 0; i < iNumListeners; i++) {
      iListeners[i].stateChanged( this, iPreviousValue, newValue);
    }
    iPreviousValue = newValue;
  }
}
