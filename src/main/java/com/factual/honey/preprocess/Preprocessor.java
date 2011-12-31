package com.factual.honey.preprocess;

import com.factual.Query;
import com.factual.honey.HoneyStatement;

public class Preprocessor {
  private final HoneyStatement stmt;
  private final Query query;


  public Preprocessor(HoneyStatement stmt, Query query) {
    this.stmt = stmt;
    this.query = query;
  }

  public String preprocess(String sql) {
    // EXPLAIN...
    // TODO: pretty print: http://stackoverflow.com/questions/4105795/pretty-print-json-in-java
    if(sql.startsWith("EXPLAIN ")) {
      stmt.setExplain();
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
      query.near(term, 4800);
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
      query.search(term);
      sql = part1 + part2;
    }

    return sql;
  }

}
