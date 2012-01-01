package com.factual.honey.parse;

import org.junit.Test;

import com.factual.Query;


public class SqlParserTest {

  @Test(expected= ParseException.class)
  public void testParse_unknownKeyword() {
    String bad = "select * from places where badness";
    new SqlParser(new Query()).parse(bad);
  }

}
