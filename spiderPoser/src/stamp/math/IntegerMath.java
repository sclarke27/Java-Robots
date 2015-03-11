package stamp.math;

/**
 * Credit where credit is due:
 * These functions I found at http://www.acroname.com/brainstem/examples/math/math.html
 * Converted to java for the Javelin.
 *
 * This class provides some handy math functions.
 * It includes an integer absolute value function, an integer sign or "signum" function,
 * and approximations for cosine, sine, and the "atan2" function.
 *
 * The sign function returns 1 if a value is positive, 0 if a value is 0,
 * and -1 if a value is negative.  It is useful in calculations where the sign of
 * one value affects the sign of another value.
 *
 * The sine and cosine functions are integer-based approximations.  Input to these
 * functions is an angle in degrees from 0 to 359.  The result is a scaled sine or
 * cosine in the range -100 to 100.  In a floating point processor using degrees
 * for angle measurement, sin(90.0) = 1.0.  In these routines, sine and cosine values
 * between -1 and 1 are "mapped" to integers in the range -100 to 100.  So the sine of
 * 90 degrees would be 100.
 *
 * The cosine function is based on a 2-degree polynomial curve fit for cosines from
 * 0 degrees to 90 degrees.  Additional comparisons fit the values to the proper quadrant,
 * 90 to 180, 180 to 270, and 270 to 360 degrees.  The sine function exploits the fact
 * that sine values are identical to cosine values with a 90 degree phase shift.
 * The integer approximations may vary from actual scaled and trunctated sine and
 * cosine values by up to 3 units.  So they're not great, but they do provide some usable
 * results with a fairly small amount of code.
 *
 * The atan2 function is the four-quadrant arctangent of y/x.  The approximation takes
 * x and y coordinate inputs and returns an angle from 0 to 359.  The inputs must be in
 * the range -1000 to 1000 to prevent overflow.  The approximation uses two curve fits
 * to handle inputs that produce angles in the range of 0 to 45 degrees.
 * The routine relies on symmetry and proper integer scaling to provide results
 * from 0 to 359 degrees.  Additional checks handle inputs of 0.  If both input parameters
 * are 0, the calling program will halt with a divide-by-zero error.
 * The extra logic needed to apply the symmetry, do quadrant checks, and detect 0 inputs
 * make the atan2 routine rather expensive in terms of code size and run time.
 *
 * @version 1.0 Date 03-24-2005
 * @author Peter Verkaik (peterverkaik@boselectro.nl)
 */

public class IntegerMath {

  /**
   * Calculate absolute value of x
   *
   * @param x integer value to convert
   *        if x = -32768 then 0 is returned
   * @result |x|
   */
  public static int abs(int x) {
    if (x == -32768) x = 0;
    return (x < 0) ? -x : x;
  }

  /**
   * Calculate sign of x
   *
   * @param x integer value -32768 to +32767
   * @result -1 if x < 0
   *          0 if x = 0
   *         +1 if x > 0
   */
  public static int sign(int x) {
    return (x == 0) ? 0 : (x<0) ? -1 : 1;
  }

  /**
   * Calculate sine value for angle
   *
   * @param angle Angle in degrees (0-359)
   * @result truncated int(100*sin(angle))
   *         result range is -100 to +100
   */
  public static int sin(int angle) {
    return cos((angle+270)%360);
  }

  /**
   * Calculate cosine value for angle
   *
   * @param angle Angle in degrees (0-359)
   * @result truncated int(100*cos(angle))
   *         result range is -100 to +100
   */
  public static int cos(int angle) {
    int qop = angle;
    int div = 101;

    /* perform quadrant conversion */
    /* (input is 0 to 359) */

    if (angle > 90) {
      qop = 180 - angle;
      if (angle < 270) div = -101;
    }
    if (angle > 180) qop = angle - 180;
    if (angle > 270) qop = 360 - angle;

    /* quadratic fit for quadrant */
    return ((10188 - qop * (qop + 23)) / div);
  }

  /**
   * Calculate arctan value of y/x
   *
   * @param y y-value
   * @param x x-value (should not be 0 !!!)
   * @result arctan(y/x)
   */
  /* absolute value of parameters should */
  /* be less than 1000 to prevent overflow */
  public static int atan2(int y, int x) {
    int ax;
    int ay;
    int aq=0;
    boolean flip = false;

    /* work in first quadrant */
    ax = abs(x);
    ay = abs(y);

    /* approximation works when ax >= ay */
    if (ax < ay) {
      aq = ax;
      ax = ay;
      ay = aq;
      flip = true;
    }

    /* crank the numbers and handle 0 values */
    if (ay != 0) {
      aq = (30 * ax) / ay;
      if (aq == 0) {
        aq = 90;
      }
      else {
        if (aq >= 82) {
          aq = 1720 / aq;
        }
        else {
          aq = 1720 / (aq + 8);
        }
      }
    }
    else {
      /* atan2(0,0) makes the program abort */
      if (ax == 0) aq = aq / 0;
      aq = 0;
    }

    /* adjust if ax and ay were flipped */
    if (flip) aq = 90 - aq;

    /* now get proper quadrant */
    /* based on sign bits of x and y */
    /* (default is quadrant 1) */
    if (x < 0) {
      if (y < 0) {
        /* quadrant 3 */
        aq = 180 + aq;
      }
      else {
        /* quadrant 2 */
        aq = 180 - aq;
      }
    }
    else {
      if (y < 0) aq = 360 - aq;
    }

    /* force result to be 0 to 359 */
    return (aq % 360);
  }

}