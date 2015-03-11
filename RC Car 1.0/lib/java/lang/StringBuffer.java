/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

/**
 * The StringBuffer class represents strings of characters which can
 * change at runtime.
 *
 * A StringBuffer will grow dynamically as its contents change. Growing the
 * StringBuffer requires allocating an entirely new buffer and copying the old
 * string to the new buffer. This can result in a lot of unused memory so
 * it is recommended that when a StringBuffer is created the maximum length
 * is specified.
 *
 * @author Chris Waters
 * @version 1.0
 */
public class StringBuffer {

  final static int DEFAULT_CAPACITY = 64;

  int length;
  char s[]; // The string data.

  String returnString; // The string instance returned by toString().

  /**
   * Allocate a new empty StringBuffer using the default capacity of 64
   * characters.
   */
  public StringBuffer() {
    s = new char[DEFAULT_CAPACITY];
    length = 0;
  }

  /**
   * Allocate a new empty StringBuffer using the specified capacity.
   *
   * @param capacity the capacity of the new StringBuffer.
   */
  public StringBuffer(int capacity) {
    s = new char[capacity];
    length = 0;
  }

  /**
   * Allocate a new StringBuffer and copy its contents from a String.
   *
   * @param value the String to copy into the StringBuffer.
   */
  public StringBuffer(String value) {
    s = new char[Math.max(value.length(),DEFAULT_CAPACITY)];
    length = value.length();
    for (int i = 0; i < length; i++ )
      s[i] = value.value[i];
  }

  /**
   * Returns the current length of the StringBuffer.
   *
   * @return the length of the StringBuffer in characters.
   */
  public int length() {
    return length;
  }

  /**
   * Returns the current capacity of the StringBuffer. The capacity is the
   * maximum number of characters that can be stored in the StringBuffer
   * without needing to allocate any more memory.
   *
   * @return the capacity of the StringBuffer in characters.
   */
  public int capacity() {
    return s.length;
  }

  /**
   * Clear the content of StringBuffer. This is done by setting the length to
   * zero and does not affect the capacity.
   */
  public void clear() {
    length = 0;
  }

  /**
   * Delete a range of characters from the StringBuffer.
   *
   * @param start index of the first character to delete.
   * @param end index of the character after the last character to delete.
   * @return the StringBuffer.
   */
  public StringBuffer delete(int start, int end) {
    if ( start == 0 && end >= length ) {
      length = 0;
      return this;
    }

    for ( int to = start, from = end; from < length; to++, from++ ) {
      s[to] = s[from];
    }
    length = length-(end-start);

    return this;
  }

  /**
   * Append a String to the StringBuffer.
   *
   * @param str the String to append.
   * @param the StringBuffer.
   */
  public StringBuffer append(String str) {
    if ( str == null )
      str = "null";

    int toAdd = str.length();
    expand(toAdd);
    for ( int i = length; i < length+toAdd; i++ )
      s[i] = str.value[i-length];

    length += toAdd;

    return this;
  }

  /**
   * Append a character to the StringBuffer.
   *
   * @param c the character to append.
   * @param the StringBuffer.
   */
  public StringBuffer append(char c) {
    expand(1);

    s[length++] = c;

    return this;
  }

  static String temp = new String("      ");

  /**
   * Append the string value of an integer to the StringBuffer. The conversion
   * to a String uses an internal buffer and so this method will not cause
   * a memory leak.
   *
   * @param val the integer val to append.
   * @param the StringBuffer.
   */
  public StringBuffer append(int val) {
    expand(6);
    String.valueOf(val, temp);

    return append(temp);
  }

  /**
   * Append an array of characters to a StringBuffer.
   *
   * @param str the array to append.
   * @param length the number of characters from str to append.
   * @return the StringBuffer.
   */
  public StringBuffer append(char str[], int length) {
    expand(length);
    for ( int i = this.length; i < length; i++ )
      s[i] = str[i-this.length];

    this.length += length;

    return this;
  }

  /**
   * Insert a character into a StringBuffer. All subsequent characters are
   * moved to make space.
   *
   * @param offset the index to insert the character at.
   * @param c the character to insert.
   * @return the StringBuffer.
   */
  public StringBuffer insert(int offset, char c)
    throws IndexOutOfBoundsException {
    if ( offset > length )
      throw IndexOutOfBoundsException.throwIt();

    expand(1);

    for ( int i = length; i >= offset; --i )
      s[i+1] = s[i];
    s[offset] = c;
    length++;

    return this;
  }

  /**
   * Convert the StringBuffer to a string. A single buffer is used to convert
   * to a String. Each time this method is used the previously returned
   * String will be overwritten. If you need to keep the String then use
   * the toNewString method.
   *
   * @return a String representation of the StringBuffer.
   */
  public String toString() {
    if ( returnString == null )
      returnString = new String();
    returnString.value = s;
    returnString.length = length;
    return returnString;
  }

  /**
   * Convert the StringBuffer to a string by allocating a new String object.
   *
   * @return a String representation of the StringBuffer.
   */
  public String toNewString() {
    return new String(this);
  }

  /**
   * Compare this StringBuffer to another to see if they contain the
   * identical sequence of characters. The comparison is case sensitive.
   *
   * @param sb the StringBuffer to compare to.
   * @return true if sb is identical to this StringBuffer.
   */
  public boolean equals(StringBuffer sb) {
    if ( length != sb.length() )
      return false;

    for ( int i = 0; i < length && i < sb.length(); i++ )
      if ( charAt(i) != sb.charAt(i) )
        return false;

    return true;
  }

  /**
   * Compare this StringBuffer to a String to see if they contain the
   * identical sequence of characters. The comparison is case sensitive.
   *
   * @param sb the String to compare to.
   * @return true if sb is identical to this StringBuffer.
   */
  public boolean equals(String sb) {
    if ( length != sb.length() )
      return false;

    for ( int i = 0; i < length && i < sb.length(); i++ )
      if ( charAt(i) != sb.charAt(i) )
        return false;

    return true;
  }

  /**
   * Retrieve a character from StringBuffer.
   *
   * @param index the index into the StringBuffer of the character to retrieve.
   *    Indexes start at 0 and end at length()-1.
   * @return the character at the specified index.
   */
  public char charAt(int index)
    throws IndexOutOfBoundsException {
    if ( index >= length || index > s.length )
      throw IndexOutOfBoundsException.throwIt();

    return s[index];
  }

  /**
   * Extract a substring from the StringBuffer into a new StringBuffer.
   *
   * @param start the index of the first character in the substring.
   * @param end the index of the character after the last character in the
   *   substring.
   * @return a newly allocated StringBuffer containing the substring.
   */
  public StringBuffer subString(int start, int end) {
    StringBuffer dest = new StringBuffer(end-start);
    subString(start, end, dest);
    return dest;
  }

  /**
   * Extract a substring from the StringBuffer into a existing StringBuffer.
   *
   * @param start the index of the first character in the substring.
   * @param end the index of the character after the last character in the
   *   substring.
   * @param dest the Stringbuffer to place the substring in. Any existing content
   *   in dest is overwritten.
   */
  public void subString(int start, int end, StringBuffer dest) {
    dest.clear();
    for ( int i = start; i < end; i++ )
      dest.append(charAt(i));
  }

  /**
   * Finds the first occurance of the specified String in this StringBuffer.
   *
   * @param str the String to search for.
   * @return the index of the first character matching the String or -1 if the
   *   String is not found.
   */
  public int indexOf(String str) {
    int start = -1;
    int count = 0;
    for ( int i = 0; i < length; i++ ) {
      if ( str.value[count] == s[i] ) {
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

  /*
  /*
   * Check the length of the internal buffer and expand it if necessary.
   */
  private void expand(int len) {
    if ( s.length - length < len ) {
      char newBuffer[] = new char[s.length*2];
      for ( int i = 0; i < length; i++ )
        newBuffer[i] = s[i];
      s = newBuffer;
    }
  }
}