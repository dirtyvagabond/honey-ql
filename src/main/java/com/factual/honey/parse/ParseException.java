package com.factual.honey.parse;

import net.sf.jsqlparser.JSQLParserException;

import org.apache.commons.lang.StringUtils;

public class ParseException extends RuntimeException {

  public ParseException(String msg, Throwable t) {
    super(msg, t);
  }

  public ParseException(Throwable t) {
    super(t);
  }

  public String getFirstLineOfMessage() {
    if(!StringUtils.isBlank(getMessage())) {
      return getMessage().split("\n")[0];
    } else {
      return "";
    }
  }

  public static ParseException from(JSQLParserException jsqlpe) {
    Throwable cause = jsqlpe.getCause();
    if(cause instanceof net.sf.jsqlparser.parser.ParseException) {
      String msg = cause.getMessage();
      return new ParseException(msg, jsqlpe);
    } else {
      return new ParseException(jsqlpe.getMessage(), jsqlpe);
    }
  }

}
