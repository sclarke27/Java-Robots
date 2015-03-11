package stamp.util.text;

/**
 * This class provides 'C' character library functions.
 * All test functions return boolean instead of int.
 *
 * @version 1.0 April 6, 2004
 * @author Peter Verkaik (peterverkaik@boselectro.nl)
 */

public class cchr {

  /**
   * Test if character is alphanumeric.
   *
   * @param ch Character to be tested
   * @return True if ch is alphanumeric
   */
  public static boolean isalnum(int ch) {
    return isalpha(ch) || isdigit(ch);
  }

  /**
   * Test if character is alphabetic.
   *
   * @param ch Character to be tested
   * @return True if ch is alphabetic
   */
  public static boolean isalpha(int ch) {
    return (ch<='z' && ch>='a') || (ch<='Z' && ch>='A');
  }

  /**
   * Test if character is an ascii character (0-127).
   *
   * @param ch Character to be tested
   * @return True if ch is an ascii character
   */
  public static boolean isascii(int ch) {
    return (ch >= 0) && (ch <= 127);
  }

  /**
   * Test if character is a control character (0-31 or 127).
   *
   * @param ch Character to be tested
   * @return True if ch is a control character
   */
  public static boolean iscntrl(int ch) {
    return ((ch >= 0) && (ch <= 31)) || (ch == 127);
  }

  /**
   * Test if character is a digit.
   *
   * @param ch Character to be tested
   * @return True if ch is a digit
   */
  public static boolean isdigit(int ch) {
    return (ch <= '9') && (ch >= '0');
  }

  /**
   * Test if character is a printable character other than a space (33-126).
   *
   * @param ch Character to be tested
   * @return True if ch is a printable character other than a space
   */
  public static boolean isgraph(int ch) {
    return (ch >= 33) && (ch <= 126);
  }

  /**
   * Test if character is a lowercase alphabetic.
   *
   * @param ch Character to be tested
   * @return True if ch is a lowercase alphabetic
   */
  public static boolean islower(int ch) {
    return (ch <= 'z') && (ch >= 'a');
  }

  /**
   * Test if character is a printable character (32-126).
   *
   * @param ch Character to be tested
   * @return True if ch is a printable character
   */
  public static boolean isprint(int ch) {
    return (ch >= 32) && (ch <= 126);
  }

  /**
   * Test if character is a punctuation character.
   * (all but control and alphanumeric)
   *
   * @param ch Character to be tested
   * @return True if ch is a punctuation character
   */
  public static boolean ispunct(int ch) {
    return !isalnum(ch) && !iscntrl(ch);
  }

  /**
   * Test if character is a space, tab or newline
   *
   * @param ch Character to be tested
   * @return True if ch is a space, tab or newline
   */
  public static boolean isspace(int ch) {
    return (ch == ' ') || (ch == '\t') || (ch == '\n');
  }

  /**
   * Test if character is an uppercase alphabetic.
   *
   * @param ch Character to be tested
   * @return True if ch is an uppercase alphabetic
   */
  public static boolean isupper(int ch) {
    return (ch <= 'Z') && (ch >= 'A');
  }

  /**
   * Test if character is a hexadecimal digit.
   *
   * @param ch Character to be tested
   * @return True if ch is a hexadecimal digit
   */
  public static boolean isxdigit(int ch) {
    return (ch<='f' && ch>='a') || (ch<='F' && ch>='A') || isdigit(ch);
  }

  /**
   * Get the lowercase equivalent of a letter.
   *
   * @param ch Character to be converted
   * @return Lowercase equivalent in case ch is a letter,
   *         otherwise ch is returned unchanged
   */
  public static int tolower(int ch) {
    return isupper(ch) ? ch+0x20 : ch;
  }

  /**
   * Get the uppercase equivalent of a letter.
   *
   * @param ch Character to be converted
   * @return Uppercase equivalent in case ch is a letter,
   *         otherwise ch is returned unchanged
   */
  public static int toupper(int ch) {
    return islower(ch) ? ch-0x20 : ch;
  }

}