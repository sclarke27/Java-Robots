/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

/**
 * The <code>Object</code> class is the superclass of all other classes.
 *
 * @author Chris Waters
 * @version 1.0
 */

public class Object {

  /**
   * Compare this object to another object. Returns true only if
   * <code>this</code> and <code>obj</code> refer to the same object.
   * This method should be overriden in subclasses that require different
   * comparison semantics.
   *
   * @param obj the object to comapare to.
   * @return <code>true</code> if this object is the same as the paramter <code>obj</code>.
   */

  public boolean equals(Object obj) {
    return (this == obj);
  }

  /**
   * Returns a hash code for this object. This is useful for implementing
   * hash tables and other data-structures requiring a random index. The
   * hash code is unique for each object while a program is running.
   *
   * @return a unique hash code for this object.
   */
  //public int hashCode() {
  //  /* ?? This should be implemented to return the pointer to this. */
  //  return 1;
  //}

  /**
   * Returns a string representation of this class. For the class
   * <class>Object</class> <code>toString</code> returns the hash code
   * of the object as a string. This method should be
   * overriden by sub-classes which can be represented as a string.
   *
   * @return a string representation of this class.
   */
  //public String toString() {
  //  return String.valueOf(hashCode());
  //}

  /**
   * Create a copy of this object.
   *
   * Not implemented yet.
   */
  //public Object clone() { return null; }

}