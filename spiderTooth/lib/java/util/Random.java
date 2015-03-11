/*
 * Copyright © 1999-2002 Celsius Research Ltd. All rights reserved.
 * Do not modify this file.
 */

package java.util;

/**
 * A class to generate random numbers.
 *
 * To avoid getting the same sequence every time the random number generator
 * should be seeded based on some external stimulae, such as the time between
 * button presses.
 *
 * @author Chris Waters
 * @version 1.0
 */

public class Random {

  public final static int MAX_RAND = 0x7FFF;

  protected int seed;

  /**
   * Create a new random number generator. The seed is initialized to a
   * constant value.
   */
  public Random() {
    seed = (short)4653;
  }

  /**
   * Create a new random number generator using the given seed value.
   *
   * @param seed seed value for the psuedo random sequence.
   */
  public Random(int seed) {
    this.seed = seed;
  }

  /**
   * Returns the next integer in the psuedo random sequence. The numbers
   * generated are in the range 0 .. MAX_RAND
   *
   * @return the next integer value in the sequence.
   */
  public int next() {

    int seedH, seedL, tempH, tempL;

    seedL = seed & 0x0FF;
    seedH = seed>>>8;
    tempH = (seedH<<1) | (seedL & 1);
    tempL = (seedL>>>1) | ((seedL ^ seedH ^ (seedL << 6) ^ (seedH << 6)) & 0x80);

    seed = (tempH<<8)|tempL;
    return seed&MAX_RAND;
  }

}