/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

/**
 * The <code>Integer</code> class represents the <code>int</code>
 * primitive.
 *
 * @author Chris Waters
 * @version 1.0
 */

public class Integer {

  // The int value represented by this class.
  int value;

  /**
   * The minimum value that can be represented in an int variable.
   */
  public static final int MIN_VALUE = 0x8000;
  /**
   * The maximum value that can be represented in an int variable.
   */
  public static final int MAX_VALUE = 0x7fff;

  /**
   * Create an <code>Integer</code> object with the specified value.
   *
   * @param value the primitive value to represent.
   */
  public Integer(int value) {
    this.value = value;
  }

  /**
   * Convert a string to an integer.
   *
   * @param s the String which contains the integer.
   */
  public static int parseInt(String s) {
    int val = 0;
    boolean neg = false;
    int i = 0;
    char c = ' ';

    while ( i < s.length() ) {
      c = s.charAt(i);
      if ( c != ' ' && c != '\n' && c != '\t' )
        break;
      i++;
    }

    if ( c == '-' ) {
      neg = true;
      i++;
    }

    while ( i < s.length() ) {
      c = s.charAt(i++);
      if ( c < '0' || c > '9' )
        break;

      val *= 10;
      val += c - '0';
    }

    if ( neg ) {
      val = -val;
    }
    return val;
  }

  /**
   * Convert a string to an integer.
   *
   * @param s the StringBuffer which contains the integer.
   */
  public static int parseInt(StringBuffer s) {
    int val = 0;
    boolean neg = false;
    int i = 0;
    char c = ' ';

    while ( i < s.length() ) {
      c = s.charAt(i);
      if ( c != ' ' && c != '\n' && c != '\t' )
        break;
      i++;
    }

    if ( c == '-' ) {
      neg = true;
      i++;
    }

    while ( i < s.length() ) {
      c = s.charAt(i++);
      if ( c < '0' || c > '9' )
        break;

      val *= 10;
      val += c - '0';
    }

    if ( neg ) {
      val = -val;
    }
    return val;
  }

  /**
   * Convert an integer to a string.
   *
   * @param i the integer to be converted to a string.
   */
  public static String toString(int i) {
    return String.valueOf(i);
  }

}