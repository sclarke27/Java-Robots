/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

public class Throwable {

  static final int kIllegalArgumentException = 1;
  static final int kNullPointerException = 2;
  static final int kIndexOutOfBoundsException = 3;
  static final int kOutOfMemoryError = 4;

  public Throwable() {};

  final static void throwVMException(int exceptionNumber) {
    if ( exceptionNumber == kIllegalArgumentException )
       throw IllegalArgumentException.throwIt();
    if ( exceptionNumber == kNullPointerException )
       throw NullPointerException.throwIt();
    if ( exceptionNumber == kIndexOutOfBoundsException )
       throw IndexOutOfBoundsException.throwIt();
    if ( exceptionNumber == kOutOfMemoryError )
       throw OutOfMemoryError.throwIt();
  }

  public String getMessage() {
    return "";
  };
}