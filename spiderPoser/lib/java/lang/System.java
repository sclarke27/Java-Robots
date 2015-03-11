/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.lang;

/**
 * The System class is used to print messages on the terminal. For example:
 * <code>System.out.println("Hello World.")</code>.
 */
public class System {

  public static java.io.PrintStream out;

  static {
    out = new java.io.PrintStream();
  }

}