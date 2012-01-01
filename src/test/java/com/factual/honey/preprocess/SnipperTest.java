package com.factual.honey.preprocess;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class SnipperTest {

  @Test
  public void test() {
    String str = "explain select * from places near ('palm springs') where name = 'star'";
    Snip snip = Snipper.in("near", ")").snip(str);
    assertEquals(" ('palm springs'", snip.middle());
  }

  @Test
  public void testSingleTicks() {
    String str = "this is 'within' single ticks";
    Snip snip = Snipper.in("'").snip(str);
    assertEquals("within", snip.middle());
  }

}
