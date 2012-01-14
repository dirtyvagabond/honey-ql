package com.factual.honey;

import java.util.List;
import java.util.Map;

import com.factual.driver.Factual;
import com.factual.driver.Query;
import com.factual.driver.ReadResponse;
import com.factual.driver.Tabular;
import com.factual.honey.parse.SqlParser;
import com.factual.honey.preprocess.Preprocessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HoneyStatement {
  private final String sql;
  private final Query query;
  private final String tableName;
  private boolean explain;
  private boolean isCount;
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

    isCount = parser.hasCountFn();

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

  public boolean isCount() {
    return isCount;
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

  /**
   * Executes this Statement against Factual and returns the results as Tabular
   * data.
   */
  public Tabular execute(Factual factual) {
    if(isDescribe()) {
      return factual.schema(tableName);
    } else if(isCount()){
      return countResult(factual);
    } else {
      return factual.fetch(tableName, query);
    }
  }

  private Tabular countResult(final Factual factual) {
    return new Tabular(){
      @Override
      public List<Map<String, Object>> getData() {
        return makeCountData(factual);
      }};
  }

  private List<Map<String, Object>> makeCountData(Factual factual) {
    // query should already have "count all rows" set
    ReadResponse resp = factual.fetch(tableName, query);
    final List<Map<String, Object>> countData = Lists.newArrayList();
    Map<String, Object> cell = Maps.newHashMap();
    cell.put("count", resp.getTotalRowCount());
    countData.add(cell);
    return countData;
  }

  public String getExplanation() {
    return "Read table: " + tableName + "\n" + query.toString();
  }

}
