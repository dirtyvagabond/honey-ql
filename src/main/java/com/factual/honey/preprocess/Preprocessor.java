package com.factual.honey.preprocess;

import org.apache.commons.lang.StringUtils;

import com.factual.Query;
import com.factual.honey.HoneyStatement;


public class Preprocessor {
  protected String near;
  protected String search;
  protected boolean explain;


  public String preprocess(String sql) {
    if(StringUtils.startsWithIgnoreCase(sql, "explain ")) {
      explain = true;
      sql = sql.substring(8);
      // TODO: pretty print: http://stackoverflow.com/questions/4105795/pretty-print-json-in-java
    }

    // e.g.: NEAR('1801 avenue of the stars, century city, ca')
    Snip snip = Snipper.in("near", ")").snip(sql);
    if(snip != null) {
      sql = snip.splice();
      near = snip.middle();
      near = Snipper.in("'").snip(near).middle();
    }

    snip = Snipper.in("search", ")").snip(sql);
    if(snip != null) {
      sql = snip.splice();
      search = snip.middle();
      search = Snipper.in("'").snip(search).middle();
    }

    return sql;
  }

  public void applyTo(HoneyStatement stmt, Query query) {
    if(explain) {
      stmt.setExplain();
    }
    if(near != null) {
      query.near(near, 4800);
    }
    if(search != null) {
      query.search(search);
    }
  }

}
