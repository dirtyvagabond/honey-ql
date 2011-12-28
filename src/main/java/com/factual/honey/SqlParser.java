package com.factual.honey;

import java.io.StringReader;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

import com.factual.Query;

public class SqlParser implements SelectVisitor, StatementVisitor, FromItemVisitor {
  private final Query query;
  private String tableName;

  public SqlParser(Query query) {
    this.query = query;
  }

  /**
   * Builds out query from sql and returns table name.
   */
  public static String parse(String sql, Query query) {
    CCJSqlParserManager pm = new CCJSqlParserManager();
    net.sf.jsqlparser.statement.Statement statement = null;
    try {
      statement = pm.parse(new StringReader(sql));
    } catch (JSQLParserException e) {
      throw new RuntimeException(e);
    }
    if (statement instanceof Select) {
      Select selectStatement = (Select) statement;
      SqlParser parser = new SqlParser(query);
      selectStatement.accept(parser);
      return parser.getTableName();
    } else {
      throw new IllegalArgumentException("need a SELECT statement");
    }
  }

  public String getTableName() {
    return tableName;
  }

  @Override
  public void visit(Select sel) {
    sel.getSelectBody().accept(this);
  }

  @Override
  public void visit(Delete arg0) {
    System.out.println("VISIT2:" + arg0);
  }

  @Override
  public void visit(Update arg0) {
    System.out.println("VISIT3:" + arg0);
  }

  @Override
  public void visit(Insert arg0) {
    System.out.println("VISIT4:" + arg0);
  }

  @Override
  public void visit(Replace arg0) {
    System.out.println("VISIT5:" + arg0);
  }

  @Override
  public void visit(Drop arg0) {
    System.out.println("VISIT6:" + arg0);
  }

  @Override
  public void visit(Truncate arg0) {
    System.out.println("VISIT7:" + arg0);
  }

  @Override
  public void visit(CreateTable arg0) {
    System.out.println("VISIT8:" + arg0);
  }

  @Override
  public void visit(PlainSelect select) {
    List items = select.getSelectItems();
    if(!"*".equals(items.get(0).toString())) {
      String[] itemsArr = new String[items.size()];
      for(int i = 0; i<items.size(); i++) {
        itemsArr[i] = items.get(i).toString();
      }
      query.only(itemsArr);
    }

    FromItem from = select.getFromItem();
    from.accept(this);

    if(select.getWhere() !=null) {
      WhereVisitor wv = new WhereVisitor();
      select.getWhere().accept(wv);
      wv.addFilters(query);
    }

    Limit limit = select.getLimit();
    if(limit != null) {
      query.limit(limit.getRowCount());
      if(limit.getOffset() > 0) {
        query.offset(limit.getOffset());
      }
    }

    // Parses ORDER BY
    // NOTE: $ is not legal leading char, so we use _
    //       e.g., _distance
    List orders = select.getOrderByElements();
    if(orders != null) {
      for(Object o : orders) {
        OrderByElement sort = (OrderByElement)o;
        String fieldName = sort.getExpression().toString();
        if(fieldName.startsWith("_")) {
          fieldName = fieldName.replaceFirst("_", "\\$");
        }
        if(sort.isAsc()) {
          query.sortAsc(fieldName);
        } else {
          query.sortDesc(fieldName);
        }
      }
    }
  }

  @Override
  public void visit(Union arg0) {
    System.out.println("VISIT10:" + arg0);
  }

  @Override
  public void visit(Table table) {
    tableName = table.getName().toLowerCase();
  }

  @Override
  public void visit(SubSelect arg0) {
    System.out.println("VISIT12:" + arg0);
  }

  @Override
  public void visit(SubJoin arg0) {
    System.out.println("VISIT13:" + arg0);
  }

}