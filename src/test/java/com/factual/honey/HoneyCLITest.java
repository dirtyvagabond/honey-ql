package com.factual.honey;


public class HoneyCLITest {

  /**
   * Convenience main for seeing how cli handles statements
   */
  public static void main(String[] args) {
    HoneyCLI cli = new HoneyCLI();
    cli.loadAuthFromFiles();

    String sql = "describe restaurants-us";
    //String sql = "select * from places";

    cli.evaluateSql(sql);
  }

}
