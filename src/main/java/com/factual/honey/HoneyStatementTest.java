package com.factual.honey;

import junit.framework.TestCase;

import org.junit.Test;

public class HoneyStatementTest extends TestCase {

  @Test
  public void testExplain_simple() {
    HoneyStatement stmt = new HoneyStatement("EXPLAIN select name, tel from places where name is not null");

    assertTrue(stmt.isExplain());
    assertTrue(stmt.getExplanation().contains("places"));
    assertTrue(stmt.getExplanation().contains("select"));
    assertTrue(stmt.getExplanation().contains("name"));
    assertTrue(stmt.getExplanation().contains("tel"));
    assertTrue(stmt.getExplanation().contains("filters"));
    assertTrue(stmt.getExplanation().contains("blank"));
    assertTrue(stmt.getExplanation().contains("false"));

    assertCols(stmt, "name", "tel");
  }

  private void assertCols(HoneyStatement stmt, String... cols) {
    top: for(String col : cols) {
      for(String f : stmt.getSelectFields()) {
        if(f.equals(col)) continue top;
      }
      fail(col + " not in select fields");
    }
  }

}
