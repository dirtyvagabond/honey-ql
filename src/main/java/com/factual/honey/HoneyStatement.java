package com.factual.honey;

import com.factual.Factual;
import com.factual.Query;
import com.factual.ReadResponse;
import com.factual.honey.parse.SqlParser;
import com.factual.honey.preprocess.Preprocessor;

public class HoneyStatement {
  private final String sql;
  private final Query query;
  private final String tableName;
  private boolean explain;

  public HoneyStatement(String honeyql) {
    query = new Query();
    sql = preprocess(honeyql, query);
    tableName = parseInto(sql, query);
  }

  //TODO: support SELECT COUNT __ that gets a full_row_count (unless LIMIT'd)
  /**
   * Preprocesses Honey specific syntax out of <tt>sql</tt>.
   * <p>
   * Side Effect: modifies {@link #query} as appropriate.
   * Side Effect: modifies this statement as appropriate.
   * 
   * @return the SQL statement, after taking out Honey specific syntax.
   */
  private String preprocess(String sql, Query query) {
    return new Preprocessor(this, query).preprocess(sql);
  }

  private String parseInto(String sql, Query query) {
    SqlParser parser = new SqlParser(query);
    parser.parse(sql);
    return parser.getTableName();
  }

  public void setExplain() {
    this.explain = true;
  }

  public boolean isExplain() {
    return explain;
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

  public ReadResponse execute(Factual factual) {
    return factual.fetch(tableName, query);
  }

  public String getExplanation() {
    return "Read table: " + tableName + "\n" + query.toString();
  }

}
