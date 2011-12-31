package com.factual.honey;

import com.factual.Factual;
import com.factual.Query;
import com.factual.ReadResponse;

public class HoneyQuery {
  private final String sql;
  private final Query apiQuery;
  private final String tableName;
  private boolean explain;

  public HoneyQuery(String honeyql) {
    sql = preprocess(honeyql);
    apiQuery = new Query();
    tableName = parseInto(apiQuery);
  }

  //TODO: support SELECT COUNT __ that gets a full_row_count (unless LIMIT'd)
  /**
   * Preprocesses Honey specific syntax out of <tt>sql</tt>.
   * <p>
   * Modifies {@link #apiQuery} as appropriate.
   * 
   * @return the SQL statement, after taking out Honey specific syntax.
   */
  private String preprocess(String sql) {
    // EXPLAIN...
    // TODO: pretty print: http://stackoverflow.com/questions/4105795/pretty-print-json-in-java
    if(sql.startsWith("EXPLAIN ")) {
      setExplain();
      sql = sql.substring(8);
    }

    // NEAR('[term]')
    // e.g.: NEAR('1801 avenue of the stars, century city, ca')
    int near_start = sql.indexOf("NEAR(");
    if(near_start == -1) {
      near_start = sql.indexOf("near(");
    }
    if(near_start != -1) {
      int near_end = sql.indexOf("')", near_start) + 2;
      String part1 = sql.substring(0, near_start);
      String part2 = sql.substring(near_end, sql.length());
      String near = sql.substring(near_start, near_end);
      String term = Strs.betweenSingleQuotes(near);
      apiQuery.near(term, 4800);
      sql = part1 + part2;
    }

    // SEARCH('[term]')
    // e.g.: NEAR('1801 avenue of the stars, century city, ca')
    int search_start = sql.indexOf("SEARCH(");
    if(search_start == -1) {
      search_start = sql.indexOf("search(");
    }
    if(search_start != -1) {
      int search_end = sql.indexOf("')", search_start) + 2;
      String part1 = sql.substring(0, search_start);
      String part2 = sql.substring(search_end, sql.length());
      String near = sql.substring(search_start, search_end);
      String term = Strs.betweenSingleQuotes(near);
      apiQuery.search(term);
      sql = part1 + part2;
    }

    return sql;
  }

  private String parseInto(Query apiQuery) {
    return SqlParser.parse(sql, apiQuery);
  }

  public boolean isExplain() {
    return explain;
  }

  private void setExplain() {
    this.explain = true;
  }

  public String getTableName() {
    return tableName;
  }

  public Query getApiQuery() {
    return apiQuery;
  }

  public boolean hasSelectFields() {
    return apiQuery.getSelectFields() != null;
  }

  public String[] getSelectFields() {
    return apiQuery.getSelectFields();
  }

  public ReadResponse run(Factual factual) {
    return factual.fetch(tableName, apiQuery);
  }

}
