package com.factual.honey.preprocess;

import org.apache.commons.lang.StringUtils;

import com.factual.driver.Query;
import com.factual.honey.HoneyStatement;

/**
 * Knows how to preprocess an SQL query statement. Main purpose is to recognize
 * and pull out Honey specific query syntax.
 * 
 * @author aaron
 */
public class Preprocessor {
  protected String near;
  protected String search;
  protected boolean explain;
  private String describe;


  public String preprocess(String sql) {
    if(StringUtils.startsWithIgnoreCase(sql, "describe")) {
      describe = parseDescribe(sql);
      return "";
    } else {
      sql = prepExplain(sql);
      sql = prepNear(sql);
      sql = prepSearch(sql);
      sql = prepTableName(sql);
      return sql;
    }
  }

  public boolean hasDescribe() {
    return describe != null;
  }

  public String getDescribe() {
    return describe;
  }

  private String parseDescribe(String sql) {
    String table = sql.substring(8);
    return table.trim();
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
