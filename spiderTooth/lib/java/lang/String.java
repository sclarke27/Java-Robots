/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

/**
 * The <code>String</code> class represents strings of characters.
 *
 * @author Chris Waters
 * @version 1.0
 */

public class String {

  // Do not change the ordering of these fields. They are used by the JVM.
  int length; // The number of characters in the s array that are actually used.
  char value[]; // The string data.

  /**
   * Create a new <code>String</code> of zero length.
   */
  public String() {
    length = 0;
  }

  /**
   * Create a new <code>String</code> that is a copy of the parameter
   * <code>value</code>.
   *
   * @param value the <code>String</code> to make a copy of.
   */
  public String(String value) {
    this(value.value);
  }

  /**
   * Create a new <code>String</code> that is a copy of an array of
   * characters. The array of characters is copied, so that any changes
   * to the array are not reflected in the new string.
   *
   * @param data the array of characters to make into a string.
   */
  public String(char[] data) {
    length = data.length;
    this.value = new char[length];
    for (int i = 0; i < length; i++ )
      this.value[i] = data[i];
  }

  /**
   * Create a new <code>String</code> from a StringBuffer.
   *
   * @param str the StringBuffer to copy into the new String.
   */
  public String(StringBuffer str) {
    length = str.length();
    this.value = new char[length];
    for (int i = 0; i < length; i++ )
      this.value[i] = str.charAt(i);
  }

  /**
   * Return a reference to this <code>String</code>.
   *
   * @return a reference to this <code>String</code>.
   */
  public String toString() {
    return this;
  }

  /**
   * Return the length of this <code>String</code> measured in characters.
   *
   * @return length of the <code>String</code>.
   */
  public int length() {
    return length;
  }

  /**
   * Return the character at the specified offset from the start of the
   * <code>String</code>. An index of zero refers to the first character
   * in the string.
   *
   * @param index offset into the <code>String</code>.
   * @return the character at the specified offset into the <code>String</code>.
   * @throw IndexOutOfBoundsException
   */
  public char charAt(int index) throws IndexOutOfBoundsException {
    return value[index];
  }

  /**
   * Return the character array containing the characters in the string.
   *
   * Note that this array is a reference, not a copy, of the string data so
   * any changes to the characters will result in a change to the original
   * string.
   *
   * @return the character array containing the string.
   */
  public char[] toCharArray() {
    return value;
  }

  /**
   * Specify the array that should be used to hold the characters in the string.
   *
   * @param a an array of characters that should replace the current string.
   */
  public void setCharArray(char a[]) {
    value = a;
    length = a.length;
  }

  /**
   * Create a new string which is the decimal value of an integer.
   * <p>
   * Note that this method will allocate memory for a new string.
   *
   * @param i integer to convert to a string.
   */
  public static String valueOf(int i) {
    String val = new String();
    val.value = new char[6]; // Space for 5 digits and a sign bit.

    return valueOf(i, val );
  }

  /**
   * Convert an integer into a string and place the result into an existing
   * string.
   *
   * @param i integer to convert to a string.
   * @param val string to store the result in. <code>val</code> must be long enough to
   * store the string value of <code>i</code>.
   */
  public static String valueOf(int i, String val) {
    int pos = 5;
    boolean negative = false;

    if ( i == (short)0x8000 ) {
      val.value[0] = '-';
      val.value[1] = '3';
      val.value[2] = '2';
      val.value[3] = '7';
      val.value[4] = '6';
      val.value[5] = '8';
      val.length = 6;
      return val;
    }

    if ( i < 0 ) {
      negative = true;
      i = -i;
    }
    if ( i == 0 )
      val.value[pos--] = '0';
    else
      while ( i > 0 ) {
        val.value[pos--] = (char)('0' + i%10);
        i = i/10;
      }
    if ( negative )
      val.value[pos--] = '-';

    // Left justify the number
    for ( int j = 0; j < (5-pos); j++ )
      val.value[j] = val.value[pos+j+1];
    val.length = 5-pos;

    return val;
  }

  /**
   * Test the string with equality with another string. The comparison is
   * performed respecting case.
   *
   * @param anotherString the string to compare with this string.
   */
  public boolean equals(String anotherString) {
    if ( length != anotherString.length )
      return false;

    for ( int i = 0; i < length; i++ )
      if ( value[i] != anotherString.value[i] )
        return false;

    return true;
  }

  /**
   * Perform a case-insensitive comparison this string and another string.
   *
   * @param anotherString the string to compare with this string.
   */
  public boolean equalsIgnoreCase(String anotherString) {
    if ( length != anotherString.length )
      return false;

    for ( int i = 0; i < length; i++ ) {
      char c1 = value[i], c2 = anotherString.value[i];

      // Convert to upper case.
      if ( c1 >= 'a' && c1 <= 'z' )
        c1 -= 'a'-'A';
      if ( c2 >= 'a' && c2 <= 'z' )
        c2 -= 'a'-'A';

      if ( c1 != c2  )
        return false;
    }

    return true;
  }

  /**
   * Finds the first occurance of the specified String in this String.
   *
   * @param str the String to search for.
   * @return the index of the first character matching the String or -1 if the
   *   String is not found.
   */
  public int indexOf(String str) {
    int start = -1;
    int count = 0;
    for ( int i = 0; i < length; i++ ) {
      if ( str.value[count] == value[i] ) {
        if ( start == -1 ) {
          start = i;
        }
        count++;
        if ( count == str.length ) {
          return start;
        }
      }
      else {
        count = 0;
        start = -1;
      }
    }

    return -1;
  }

// TODO: Create routines that use a static buffer for type conversion.
}