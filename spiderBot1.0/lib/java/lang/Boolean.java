/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

/**
 * The <code>Boolean</code> class represents values of type <code>boolean</code>.
 *
 * @author Chris Waters
 * @version 1.0
 */

public class Boolean {

  // The internal boolean value of this object.
  private boolean value;

  /**
   * A <code>Boolean</code> object corresponding to the value <code>true</code>.
   */
  public static final Boolean TRUE = new Boolean(true);

  /**
   * A <code>Boolean</code> object corresponding to the value <code>false</code>.
   */
  public static final Boolean FALSE = new Boolean(false);

  /**
   * Create a new <code>Boolean</code> object with the given value.
   *
   * @param value the <code>boolean</code> value the object should be given.
   */

  public Boolean(boolean value) {
    this.value = value;
  }

  /**
   * Create a <code>Boolean</code> object from a string. The value of the
   * object is true if, and only if, <code>s</code> is the string
   * <code>"true"</code>, ignoring case.
   *
   * @param s the string to base the object value on.
   */

  public Boolean(String s) {
    value = s.equalsIgnoreCase("true");
  }

  /**
   * Convert the <code>Boolean</code> to a string. The value of the returned
   * string is either <code>"true"</code> or <code>"false"</code>.
   *
   * @return the value of this object as a string.
   */

  public String toString() {
    if ( value )
      return "true";
    else
      return "false";
  }

  /**
   * Return <code>true</code> if <code>obj</code> refers to the same value
   * as this object.
   *
   * @param obj the object to compare to.
   * @return whether this object and <code>obj</code> refer to the same value.
   */

  public boolean equals(Object obj) {
    if ( obj != null && (obj instanceof Boolean))
      return (value == ((Boolean)obj).value);
    else
      return false;
  }

  /**
   * Return the hashcode for the <code>Boolean</code> object. If the value of
   * this object is <code>true</code> then 1231 is returned. If the value is
   * false then 1237 is returned.
   *
   * @return hash code of the object.
   */

  public int hashcode() {
    return value ? 1231 : 1237;
  }

  /**
   * Return the value of this object.
   *
   * @return the value of this <code>Boolean</code> object.
   */

  public boolean booleanValue() {
    return value;
  }

  /**
   * Convert a string to a boolean value. The value of a string is <code>true</code>
   * if and only if the string is not null, and is equal to <code>"true"</code>,
   * ignoring case.
   *
   * @param s <code>String</code> object to covert to a <code>boolean</code>.
   * @return the value of the <code>String</code> as a <code>boolean</code>.
   */

  public static boolean valueOf(String s) {
    return (s != null && s.equalsIgnoreCase("true"));
  }
}