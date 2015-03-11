/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

public class OutOfMemoryError extends Error {

  private static OutOfMemoryError e;

  public static OutOfMemoryError throwIt() {
    if ( e != null )
      return e;
    else
      return (e = new OutOfMemoryError());
  }
}