package com.factual.honey.parse;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.factual.Query;


public class SqlParserTest {

  @Test(expected= ParseException.class)
  public void testParse_unknownKeyword() {
    String bad = "select * from places where badness";
    new SqlParser(new Query()).parse(bad);
  }

  @Test
  public void testParse_count() {
    String sql = "select count(*) from places";
    SqlParser parser = new SqlParser(new Query());
    parser.parse(sql);

    assertTrue(parser.hasCountFn());
  }

}
