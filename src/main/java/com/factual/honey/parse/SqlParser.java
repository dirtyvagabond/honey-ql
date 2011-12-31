package com.factual.honey.parse;

import java.io.StringReader;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
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

/**
 * Handles the main parsing of SQL.
 * 
 * @author aaron
 */
public class SqlParser implements SelectVisitor, StatementVisitor, FromItemVisitor {
  private final Query query;
  private String tableName;

  public SqlParser(Query query) {
    this.query = query;
  }

  /**
   * Parses <tt>sql</tt>, modifying {@link #query} as appropriate.
   * Side Effect: {@link #tableName} will be defined, based on <tt>sql</tt>.
   * 
   * @return the name of the table specified by <tt>sql</tt>
   */
  public String parse(String sql) {
    Statement statement = parse(new StringReader(sql));
    if (statement instanceof Select) {
      statement.accept(this);
      return tableName;
    } else {
      throw new IllegalArgumentException("Expected a SELECT statement");
    }
  }

  private Statement parse(StringReader sql) {
    try {
      return new CCJSqlParserManager().parse(sql);
    } catch (JSQLParserException e) {
      throw new RuntimeException(e);
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
  public void visit(Table table) {
    tableName = table.getName().toLowerCase();
  }

  @Override
  public void visit(SubSelect arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(SubJoin arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Delete arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Update arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Insert arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Replace arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Drop arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Truncate arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(CreateTable arg0) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visit(Union arg0) {
    throw new UnsupportedOperationException();
  }

}