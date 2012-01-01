package com.factual.honey.preprocess;

import org.apache.commons.lang.StringUtils;

public class Snipper {
  private final String startTag;
  private final String endTag;


  public static Snipper in(String startTag, String endTag) {
    return new Snipper(startTag, endTag);
  }

  public static Snipper in(String tag) {
    return new Snipper(tag, tag);
  }

  private Snipper(String startTag, String endTag) {
    this.startTag = startTag;
    this.endTag = endTag;
  }

  public Snip snip(String str) {
    int start = StringUtils.indexOfIgnoreCase(str, startTag);

    if (start > -1) {
      int end = str.indexOf(endTag, start + startTag.length());
      if(end > -1) {
        String first = str.substring(0, start);
        String middle = str.substring(start + startTag.length(), end);
        end += endTag.length();
        String last = str.substring(end);
        return new Snip(first, middle, last);
      }
    }

    return null;
  }

}
