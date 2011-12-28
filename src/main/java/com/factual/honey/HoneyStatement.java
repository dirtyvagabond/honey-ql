package com.factual.honey;

public class HoneyStatement {
  protected String sql;
  protected boolean explain;


  public HoneyStatement sql(String sql) {
    this.sql = sql;
    return this;
  }

  public HoneyStatement explain() {
    explain = true;
    return this;
  }

}
