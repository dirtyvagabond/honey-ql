package com.factual.honey.preprocess;

import org.apache.commons.lang.StringUtils;

import com.factual.Query;
import com.factual.honey.HoneyStatement;


public class Preprocessor {
  protected String near;
  protected String search;
  protected boolean explain;


  public String preprocess(String sql) {
    sql = prepExplain(sql);
    sql = prepNear(sql);
    sql = prepSearch(sql);
    sql = prepTableName(sql);
    return sql;
  }

  //Parser does not take - in table names, but Factual does.
  private String prepTableName(String sql) {
    Snip snip = Snipper.in("from ", " ").snip(sql);
    if(snip != null) {
      sql = snip.splice("from " + snip.middle().replace('-', '_') + " ");
    } else {
      snip = Snipper.after("from ").snip(sql);
      if(snip != null) {
        sql = snip.splice(snip.middle().replace('-', '_'));
      }
    }
    return sql;
  }

  private String prepExplain(String sql) {
    if(StringUtils.startsWithIgnoreCase(sql, "explain ")) {
      explain = true;
      sql = sql.substring(8);
    }
    return sql;
  }

  private String prepSearch(String sql) {
    Snip snip = Snipper.in("search", ")").snip(sql);
    if(snip != null) {
      sql = snip.splice();
      search = snip.middle();
      search = Snipper.in("'").snip(search).middle();
    }
    return sql;
  }

  private String prepNear(String sql) {
    Snip snip = Snipper.in("near", ")").snip(sql);
    if(snip != null) {
      sql = snip.splice();
      near = snip.middle();
      near = Snipper.in("'").snip(near).middle();
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
