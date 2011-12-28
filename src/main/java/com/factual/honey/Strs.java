package com.factual.honey;

public class Strs {
  public static String splice(String str, String mark1, String mark2) {
    int start = str.indexOf(mark1) + mark1.length();
    int end = str.indexOf(mark2, start) + mark2.length() - 1;
    return str.substring(start, end);
  }

  /**
   * Returns the text between the first pair of single quotes
   */
  public static String betweenSingleQuotes(String str) {
    return splice(str, "'", "'");
  }

}
