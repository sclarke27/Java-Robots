package stamp.util.text;

/**
 * This class provides 'C' string library functions.
 * All test functions return boolean instead of int.
 * Functions that were defined to return a pointer as a position,
 * return an integer index instead, or -1 in case of error.
 *
 * @version 1.0 April 6, 2004
 * @author Peter Verkaik (peterverkaik@boselectro.nl)
 */

public class cstr {

  /**
   * The strcat() function concatenates a copy of str2 to str1
   * and terminates str1 with a null.
   * str1 must be large enough to hold both str1 and str2.
   *
   * @param str1 Asciiz character array
   * @param str2 Asciiz character array
   * @return str1
   */
  public static char[] strcat(char[] str1, char[] str2) {
    return _copy(strlen(str1),str1,str2);
  }

  /**
   * The strcpy() function is used to copy the contents of str2 into str1.
   *
   * @param str1 Result string
   * @param str2 Asciiz string to copy
   * @return str1
   */
  public static char[] strcpy(char[] str1, char[] str2) {
    return _copy(0,str1,str2);
  }

  /**
   * The _copy() copies the contents of str2 into str1, starting at offset h.
   *
   * @param h Offset in str1 to start copy
   * @param str1 Result string
   * @param str2 Asciiz string to copy
   * @return str1
   */
  private static char[] _copy(int h, char[] str1, char[] str2) {
    int k=0, l=str2.length;
    char c;
    while (k < l) {
      c = str2[k++];
      str1[h++] = c;
      if (c == 0) break;
    }
    str1[h] = 0;
    return str1;
  }

  /**
   * Compare two strings lexicographically
   *
   * @param str1 String 1
   * @param str2 String 2
   * @return <0 if str1 < str2
   *         0 if str1 == str2
   *        >0 if str1 > str2
   */
  public static int strcmp(char[] str1, char[] str2) {
    int h=0, k=0, l=str1.length, m=str2.length;
    while ((h<l) && (k<m)) {
      if (str1[h] == str2[k]) {
        if (str1[h] == 0) return 0;
        h++;
        k++;
      }
      else return (str1[h] - str2[k]);
    }
    if ((h==l) && (k==m)) return 0;
    return (h==l) ? -1 : 1;
  }

  /**
   * Get length of a string
   *
   * @param str String
   * @return Length of str
   */
  public static int strlen(char[] str) {
    int h=0, l=str.length;
    while (h<l) {
      if (str[h] == 0) return h;
      h++;
    }
    return h;
  }

}