package stamp.math;
import stamp.core.*;

/**
 * This class provides a few math routines to deal with unsigned integers.
 * It provides unsigned division, multiplication of an integer with an unsigned fraction,
 * and calculates unsigned fraction for signed and unsigned integer division.
 * Also calculates the unsigned square root of an integer, the result being
 * an unsigned integer inclusive fraction.
 *
 * @author Peter Verkaik (peterverkaik@boselectro.nl)
 * @version 1.3 December 21, 2005
 */

public class UnsignedIntMath {

  /**
   * Get absolute value
   *
   * @param x Signed value -32767 to +32767
   * @return Positive value 0 to 32767
   *         if x=-32768 then -32768 is returned
   */
  public static int abs(int x) {
    return (x < 0) ? -x : x;
  }

  /*
     Calculate unsigned square root of x in 8.8 fixed point

     x = (I + F/256)*(I + F/256) = I*I + 2*I*F/256 + F*F/65536
                                 = I*I + (I*F>>>7) + (F*F>>>16)
     x<<shift = (I*I<<shift) + (I*F>>>7-shift) + (F*F>>>16-shift)   shift<=7
     x = 0 --> I=0, F=0
     x = 1 --> I=1, F=0
  */

  private static int low,high,guess;

  private static void usqrt_guess_test(int boundary, int x) {
    if (x>0) {
      if (guess<0 || guess>x) high=boundary; //guess too high
      else low=boundary; //guess too low
    }
    else {
      if (guess<0 && guess>x) high=boundary; //guess too high
      else low=boundary; //guess too low
    }
  }

  /**
   * Calculate unsigned square root
   * root = I + F/256
   *
   * @param x Unsigned value (0-65535)
   * @return Unsigned square root fixed point value I.F (I in highbyte, F in lowbyte)
   */
  public static int usqrt(int x) {
    if (x==0 || x==1) return x<<8;
    int i,result;
    //find integer part of root
    low=0;
    high=256;
    do {
      i = (low+high)>>1;
      guess = i*i;
      if (guess==x) return i<<8; //exact integer root, fraction=0
      usqrt_guess_test(i,x);
    } while (high-low>1);
    result=low;
    //find fraction part of root
    int shift=0;
    low=0;
    high=256;
    if (x>0) { //prevent overflow on guess value
      while ((x<<1)>0 && shift<7) {
        x<<=1;
        shift++;
      }
    }
    do {
      i = (low+high)>>1;
      guess = ((result*result)<<shift) + (((result*i)+(64>>>shift))>>>(7-shift)) + ((i*i)>>>(16-shift));
      usqrt_guess_test(i,x);
    } while (high-low>1);
    return (result<<8)|low;
  }

  /**
   * Divide two unsigned integers.
   * Q = N/D
   *
   * @param numerator Unsigned int 0-65535
   * @param denominator Unsigned int 1-65535
   * @return Unsigned quotient Q
   */
  public static int udiv(int numerator, int denominator) {
    return umulf(numerator,ufrac(1,denominator));
  }

  /**
   * Multiply signed integer with unsigned fraction.
   *
   * @param value Signed int to multiply
   * @param fraction Unsigned multiplier (represents Multiplier / 65536)
   * @return value * fraction rounded off signed integer result
   */
  public static int mulf(int value, int fraction) {
    return (value < 0) ? -umulf(-value,fraction) : umulf(value,fraction);
  }

  /**
   * Multiply unsigned integer with unsigned fraction.
   *
   * @param value Unsigned int to multiply
   * @param fraction Unsigned multiplier (represents Multiplier / 65536)
   * @return value * fraction rounded off unsigned integer result
   */
  public static int umulf(int value, int fraction) {
    int i, result=0, rest=0;
    for (i=1; i<17; i++) {
      if (fraction < 0) { //b15 set
        result += (value >>> i);                    //accumulate integer result
        rest += ( (value & ((1<<i)-1)) << (16-i) ); //accumulate rest R/65536
        if (CPU.carry()) result++;                  //update result if rest overflows
      }
      fraction <<= 1; //next bit
    }
    if (rest < 0) result++; //roundoff
    return result;
  }

  /*
     Unsigned fraction calculation using signed integer math.

     N/D =  I + F/65536

     We want to calculate F
     Notice that N,D,I and F are signed integers -32768 to +32767
     but we treat them as unsigned integers 0x0000 to 0xFFFF

     if N=0 or D=1 then F=0

     N>0 and D>0
     ===========
     use the signed fraction function: F = frac(N,D)

     N<0 and D>0
     ===========
     treat N as unsigned: N = (0x4000<<1) + (N&0x7FFF)
     use the signed fraction function: F = (frac(0x4000,D)<<1) + frac(N&0x7FFF,D)

     N<0 and D<0
     ============
     make N and D positive by shifting right

     N   (N>>>1) + (N&1)/2   (N>>>1)   1 + (N&1)/(N>>>1)/2
     - = ----------------- = ------- * -------------------
     D   (D>>>1) + (D&1)/2   (D>>>1)   1 + (D&1)/(D>>>1)/2

     0x8000 <= D <= 0xFFFF, so 0x4000 <= (D>>>1) <= 0x7FFF
     but then 1/(1 + (D&1)/(D>>>1)/2) equals approximate 1 - (D&1)/(D>>>1)/2
     Proof: D = 0x8001
            D&1 = 1
            D>>>1 = 0x4000
            1 + (D&1)/(D>>>1)/2 = 1 + 1/0x8000 = 1.000030517578125
                           1/1.000030517578125 = 0.999969483353169
            1 - (D&1)/(D>>>1)/2 = 1 - 1/0x8000 = 0.999969482421875

     N   (N>>>1)        1  (N&1)          1  (D&1)
     - = ------- * (1 + -*-------) * (1 - -*-------)
     D   (D>>>1)        2 (N>>>1)         2 (D>>>1)

       N = 0x8001 then (N&1)/(N>>>1)/2 = 1/0x4000/2 = 1/0x8000
       N = 0xFFFF then (N&1)/(N>>>1)/2 = 1/0x7FFF/2 = 1/0xFFFE

       D = 0x8001 then (D&1)/(D>>>1)/2 = 1/0x4000/2 = 1/0x8000
       D = 0xFFFF then (D&1)/(D>>>1)/2 = 1/0x7FFF/2 = 1/0xFFFE

     N   (N>>>1)
     - = ------- * K where 1-1/0x8000 <= K <= 1+1/0x8000
     D   (D>>>1)           0.99996948 <= K <= 1.00003051

       We approximate K=1 for all N<0 and D<0

     N   (N>>>1)         F
     - = ------- = I + -----
     D   (D>>>1)       65536

     since (N>>>1) > 0 and (D>>>1) > 0
     use the signed fraction function: F = frac(N>>>1,D>>>1)

     N>0 and D<0
     ===========
     make D positive by shifting right

     N   (N>>>1) + (N&1)/2   (N>>>1)   1 + (N&1)/(N>>>1)/2
     - = ----------------- = ------- * -------------------
     D   (D>>>1) + (D&1)/2   (D>>>1)   1 + (D&1)/(D>>>1)/2

           N   (N>>>1) + (N&1)/2      1             1/2
     N=1   - = ----------------- = ------- * -------------------
           D   (D>>>1) + (D&1)/2   (D>>>1)   1 + (D&1)/(D>>>1)/2

           D<0 so (see section above for proof)

           N      1      1        1  (D&1)                    1      1
           - = ------- * - * (1 - -*-------) = approximate ------- * -
           D   (D>>>1)   2        2 (D>>>1)                (D>>>1)   2

           use the signed fraction function: F = frac(1,D>>>1)>>>1

           N   (N>>>1) + (N&1)/2   (N>>>1)   1 + (N&1)/(N>>>1)/2
     N>1   - = ----------------- = ------- * -------------------
           D   (D>>>1) + (D&1)/2   (D>>>1)   1 + (D&1)/(D>>>1)/2

           N   (N>>>1)        1  (N&1)          1  (D&1)
           - = ------- * (1 + -*-------) * (1 - -*-------)
           D   (D>>>1)        2 (N>>>1)         2 (D>>>1)

                           (N>>>1)        1  (N&1)
             = approximate ------- * (1 + -*-------)
                           (D>>>1)        2 (N>>>1)

           N>0 and D<0 so                        F1  1
                                               -----*-*F2
           N     F1         1   F2       F1    65536 2         F
           - = ----- * (1 + -*-----) = ----- + ----------- = -----
           D   65536        2 65536    65536      65536      65536

                    F1*F2 1        F1  F2  1
           F = F1 + -----*- = F1 + ---*---*-
                    65536 2        256 256 2

           F1 = frac(N>>>1,D>>>1)
           F2 = frac(N&1,N>>>1)
           F = ((F1>>>8)*(F2>>>8)>>>1) + F1
  */

  /**
   * Calculate unsigned 65536 based fraction for unsigned integer division.
   * N/D = I + F/65536
   *
   * @param numerator Unsigned integer 0 to 65535
   * @param denominator Unsigned integer 1 to 65535
   * @return Unsigned 65536 based fraction F
   */
  public static int ufrac(int numerator, int denominator) {
    int f1,f2;
    if (denominator>0) {
      f1 = frac(numerator&0x7FFF,denominator);
      f2 = (numerator<0) ? frac(0x4000,denominator)<<1 : 0;
      return f1+f2;
    }
    else {
      if (numerator<0) return frac(numerator>>>1,denominator>>>1);
      else {
        if (numerator==1) return frac(1,denominator>>>1)>>>1;
        else {
          f1 = frac(numerator>>>1,denominator>>>1);
          f2 = frac(numerator&1,numerator>>>1);
          return (((f1>>>8)*(f2>>>8))>>>1) + f1;
        }
      }
    }
  }

  /**
   * Calculate unsigned 65536 based fraction for signed integer division.
   * N/D = S*(I + F/65536) where S=-1 or S=+1 (determined by signs of N and D)
   *
   * @param numerator Signed integer -32768 to 32767
   * @param denominator Signed integer (must not be 0)
   * @return Unsigned 65536 based fraction F
   */
  public static int frac(int numerator, int denominator) {
    int j,F=0;
    if ((numerator==0) || (denominator==1)) return 0;
    if (numerator<0) numerator = -numerator; //make positive
    if (denominator<0) denominator = -denominator; //make positive
    for (j=15; j>=0; j--) {
      if (numerator<0) numerator = ((numerator>>>1)%(denominator>>>1))<<2;
      else numerator = (numerator%denominator)<<1; //remainder*2
      if (numerator<0) F |= ((numerator>>>1)/(denominator>>>1))<<j;
      else F |= (numerator/denominator)<<j;
    }
    return F;
  }

  /**
   * Convert unsigned 65536 based fraction to unsigned 10000 based fraction (0.0000 to 0.9999)
   * D = F*(10000/65536)
   *
   * @param fraction Unsigned 65536 based fraction
   * @return Unsigned 10000 based fraction D (0-9999)
   */
  public static int frac2dec(int fraction) {
    return umulf(fraction,10000);
  }

  /**
   * Convert unsigned 10000 based fraction (0-9999) to unsigned 65536 based fraction
   * F = D*(65536/10000) = D*(6 + 36281/65536)
   *
   * @param decimal Unsigned 10000 based fraction 0.0000-0.9999
   * @return Unsigned 65536 based fraction F
   */
  public static int dec2frac(int decimal) {
    return (decimal<<2) + (decimal<<1) + umulf(decimal,(short)0x8DB9);
  }

}