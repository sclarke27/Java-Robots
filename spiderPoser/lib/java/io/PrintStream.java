/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.io;

import stamp.core.*;
    
/**
 * Print characters in the Javelin IDE's message window.
 *
 */
public class PrintStream {

  final static String newline = "\r\n";

  // Buffer for formatting non-string arguments.
  String buffer = new String("       ");
  char b[] = new char[1];

  /**
   * Create a new PrintStream. It is not necessary to create new instances
   * of this class. It should be accessed as <code>System.out.println</code>.
   * <p>
   * Methods in this class do not allocate memory when they are called.
   */
  public PrintStream() {}

  /**
   * Print a string on the terminal.
   *
   * @param s the string to print.
   */
  public void print(String s) {
    CPU.message( s.toCharArray(), s.length());
  }

  /**
   * Print an integer on the terminal.
   *
   * @param i the integer to print.
   */
  public void print(int i) {
    print(String.valueOf(i, buffer));
  }

  /**
   * Print a character on the terminal.
   *
   * @param c the character to print.
   */
  public void print(char c) {
    b[0] = c;
    CPU.message( b, 1);
  }

  /**
   * Print a boolean value on the terminal. True values will print "true" and
   * false values will print "false".
   *
   * @param b the boolean value to print.
   */
  public void print(boolean b) {
    if ( b )
      print("true");
    else
      print("false");
  }

  /**
   * Print a string on the terminal and move to a new line.
   *
   * @param s the string to print.
   */
  public void println(String s) {
    print(s);
    print(newline);
  }

  /**
   * Print an integer on the terminal and move to a new line.
   *
   * @param i the integer to print.
   */
  public void println(int i) {
    print(i);
    print(newline);
  }

  /**
   * Print a boolean value on the terminal and move to a new line. True values
   * will print "true" and false values will print "false".
   *
   * @param b the boolean value to print.
   */
  public void println(boolean b) {
    print(b);
    print(newline);
  }

  /**
   * Print a character on the terminal and move to a new line.
   *
   * @param c the character to print.
   */
  public void println(char c) {
    print(c);
    print(newline);
  }

}