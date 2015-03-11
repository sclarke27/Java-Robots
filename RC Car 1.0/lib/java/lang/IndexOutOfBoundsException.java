/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

public class IndexOutOfBoundsException extends RuntimeException {

  private static IndexOutOfBoundsException e;

  public static RuntimeException throwIt() {
    if ( e != null )
      return e;
    else
      return (e = new IndexOutOfBoundsException());
  }
}