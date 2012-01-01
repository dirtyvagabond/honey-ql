package com.factual.honey.preprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class PreprocessorTest {

  @Test
  public void testNear() {
    String str = "select * from places nEaR ('my place') where name like 'star%'";
    Preprocessor prep = new Preprocessor();
    String prepped = prep.preprocess(str);

    assertEquals("select * from places  where name like 'star%'", prepped);
    assertEquals("my place", prep.near);
  }

  @Test
  public void testSearch() {
    String str = "select * from places sEaRcH ('my place') where name like 'star%'";
    Preprocessor prep = new Preprocessor();
    String prepped = prep.preprocess(str);

    assertEquals("select * from places  where name like 'star%'", prepped);
    assertEquals("my place", prep.search);
  }

  @Test
  public void testExplain() {
    String str = "eXpLaIn select * from places";
    Preprocessor prep = new Preprocessor();
    String prepped = prep.preprocess(str);

    assertEquals("select * from places", prepped);
    assertTrue(prep.explain);
  }

}
