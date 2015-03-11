/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

public class Math {

  // Private constructor.
  Math() {};

  /**
   * Find the minimum of two values.
   *
   * @param a first value.
   * @param b second value.
   * @return the minimum value of a and b.
   */
  public static int min(int a, int b) {
    return (a<=b)?a:b;
  }

  /**
   * Find the maximum of two values.
   *
   * @param a first value.
   * @param b second value.
   * @return the maximum value of a and b.
   */
  public static int max(int a, int b) {
    return (a>=b)?a:b;
  }

  /**
   * Find the absolute value of a value.
   *
   * @param a value.
   * @return the absolute value of a.
   */
  public static int abs(int a) {
    return (a<0)?-a:a;
  }

}