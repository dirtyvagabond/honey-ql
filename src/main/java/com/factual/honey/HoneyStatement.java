package com.factual.honey;

import com.factual.driver.Factual;
import com.factual.driver.Query;
import com.factual.driver.Tabular;
import com.factual.honey.parse.SqlParser;
import com.factual.honey.preprocess.Preprocessor;

public class HoneyStatement {
  private final String sql;
  private final Query query;
  private final String tableName;
  private boolean explain;
  private boolean hasCountFn;
  private String describe;

  public HoneyStatement(String honeyql) {
    query = new Query();
    sql = preprocess(honeyql, query);

    if(isDescribe()) {
      tableName = describe;
    } else {
      tableName = parseInto(sql, query);
    }
  }

  /**
   * Preprocesses Honey specific syntax out of <tt>sql</tt>.
   * <p>
   * Side Effect: modifies {@link #query} as appropriate.
   * Side Effect: modifies this statement as appropriate.
   * 
   * @return the SQL statement, after taking out Honey specific syntax.
   */
  private String preprocess(String sql, Query query) {
    Preprocessor prep = new Preprocessor();
    String prepped = prep.preprocess(sql);

    if(prep.hasDescribe()) {
      describe = prep.getDescribe();
      return "";
    } else {
      prep.applyTo(this, query);
      return prepped;
    }
  }

  private String parseInto(String sql, Query query) {
    SqlParser parser = new SqlParser(query);
    parser.parse(sql);

    hasCountFn = parser.hasCountFn();

    return parser.getTableName();
  }

  public void setExplain() {
    this.explain = true;
  }

  public boolean isExplain() {
    return explain;
  }

  public boolean isDescribe() {
    return describe != null;
  }

  public boolean hasCountFn() {
    return hasCountFn;
  }

  public String getTableName() {
    return tableName;
  }

  public boolean hasSelectFields() {
    return query.getSelectFields() != null;
  }

  public String[] getSelectFields() {
    return query.getSelectFields();
  }

  public Tabular execute(Factual factual) {
    if(isDescribe()) {
      return factual.schema(tableName);
    } else {
      return factual.fetch(tableName, query);
    }
  }

  public String getExplanation() {
    return "Read table: " + tableName + "\n" + query.toString();
  }

}
