package com.factual.honey;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.factual.Factual;
import com.factual.Query;
import com.factual.ReadResponse;
import com.google.common.collect.Lists;
import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

public class Honey {
  private final Factual factual;


  public Honey(Factual factual) {
    this.factual = factual;
  }

  //BUG: _distance supported in order by, but not anywhere else. E.g., no way to SELECT $distance
  public static void main(String[] args) {
    Factual factual = new Factual(read("key.txt"), read("secret.txt"));
    Honey honey = new Honey(factual);

    //	  execute("EXPLAIN select * from places where " +
    //	  		"(" +
    //	  		"  (name = one and (name = 'twoA' or name = 'twoB'))" +
    //	  		"  or" +
    //	  		"  (name = 'three' and name = 'four')" +
    //	  		")");
    //execute("EXPLAIN select * from places where (name = 'one' or name = 'two') and name = 'three'");
    honey.execute("select name, tel from places where (name like 'star%' or name = 'coffee')");
    //execute("EXPLAIN select name, tel from places near('1801 avenue of the stars, century city, ca') LIMIT 5");
    //execute("select * from places near('1801 ave of the stars, century city, ca') order by _distance DESC LIMIT 10");
    //execute("select name from places search('kcrw') near('1801 ave of the stars, century city, ca') order by _distance DESC LIMIT 10");
    //execute("EXPLAIN SELECT name, address, website FROM PLACES NEAR('1801 ave of the stars, century city, ca') WHERE name LIKE 'starbucks' LIMIT 10");
  }

  /**
   * Reads value from named file in src/test/resources
   */
  public static String read(String name) {
    try {
      return FileUtils.readFileToString(new File("src/test/resources/" + name)).trim();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void execute(String sql) {
    Query query = new Query();

    HoneyStatement honey = preprocess(sql, query);

    //String sql = "SELECT * FROM PLACES WHERE (name = 'Starbucks' or name= 'Icbm') AND (locality = 'Joplin' OR locality = 'Malone')  LIMIT 10";
    //String sql = "SELECT * FROM PLACES WHERE name = 'Starbucks' or name= 'Icbm' AND locality = 'Joplin' OR locality = 'Malone' LIMIT 10";
    //String sql = "SELECT * FROM PLACES WHERE (name = 'Starbucks' or name = 'Icbm') LIMIT 10";
    //String sql = "SELECT name, tel FROM PLACES WHERE name = 'Starbucks' LIMIT 10";

    String tableName = SqlParser.parse(honey.sql, query);
    //    System.out.println("Table: " + tableName);

    if(honey.explain) {
      System.out.println(query);
    } else {
      ReadResponse resp = factual.fetch(tableName, query);
      format(query, resp);
    }

  }

  //TODO: support SELECT COUNT __ that gets a full_row_count (unless LIMIT'd)
  private static HoneyStatement preprocess(String sql, Query query) {
    HoneyStatement honey = new HoneyStatement();

    // EXPLAIN...
    // TODO: pretty print: http://stackoverflow.com/questions/4105795/pretty-print-json-in-java
    if(sql.startsWith("EXPLAIN ")) {
      honey.explain();
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

    return honey.sql(sql);
  }

  private static void format(Query query, ReadResponse resp) {
    if(!resp.isEmpty()) {
      List<String> columnNames = Lists.newArrayList();
      if(query.getSelectFields() != null) {
        for(String col : query.getSelectFields()) {
          columnNames.add(col);
        }
      } else {
        for(String col : resp.first().keySet()) {
          columnNames.add(col);
        }
      }

      TableFormatter tf = new SimpleTableFormatter(true);
      tf.nextRow();

      for(Object columnName : columnNames) {
        tf.nextCell().addLine(columnName.toString());
      }

      for(Map record : resp.getData()) {
        tf.nextRow();
        for(Object columnName : columnNames) {
          Object valObj = record.get(columnName);
          String val = (valObj == null ? "null" : valObj.toString());
          tf.nextCell()
          .addLine(val);
        }

      }
      for(String line : tf.getFormattedTable())
      {
        System.out.println(line);
      }
    } else {
      System.out.println("[No Results]");
    }
  }

}
