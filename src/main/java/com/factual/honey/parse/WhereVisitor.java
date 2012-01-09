package com.factual.honey.parse;

import java.util.List;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import com.factual.driver.FieldFilter;
import com.factual.driver.FilterGroup;
import com.factual.driver.Query;
import com.factual.honey.adapt.ExpressionVisitorAdapter;
import com.factual.honey.preprocess.Snipper;
import com.google.common.collect.Lists;

//TODO! how to handle non-String vals for right side of expression?
public class WhereVisitor extends ExpressionVisitorAdapter {
  private FilterGroup filters;
  private FilterGroup f;
  private final List<FilterGroup> filterStack = Lists.newArrayList();

  public void addFilters(Query query) {
    if(filters != null) {
      query.add(filters); //TODO: support addAll, or similar to avoid extra nesting
    }
  }

  @Override
  public void visit(Parenthesis p) {
    if(p.isNot()) throw new UnsupportedOperationException("Aaron has not implemented NOT, yet. :-(");
    p.getExpression().accept(this);
  }

  @Override
  public void visit(AndExpression expr) {
    pushF(new FilterGroup());
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
    popF();
  }

  @Override
  public void visit(OrExpression expr) {
    pushF(new FilterGroup().asOR());
    expr.getLeftExpression().accept(this);
    expr.getRightExpression().accept(this);
    popF();
  }

  @Override
  public void visit(EqualsTo expr) {
    addFilter(expr, "$eq");
  }

  @Override
  public void visit(GreaterThan expr) {
    addFilter(expr, "$gt");
  }

  @Override
  public void visit(GreaterThanEquals expr) {
    addFilter(expr, "$gte");
  }

  @Override
  public void visit(InExpression in) {
    ItemsList list = in.getItemsList();
    final List<Object> inArgs = Lists.newArrayList();

    list.accept(new ItemsListVisitor() {
      @Override
      public void visit(ExpressionList exprs) {
        for(Object e : exprs.getExpressions()) {
          inArgs.add(coerce((Expression)e));
        }
      }
      @Override
      public void visit(SubSelect subsel) {
        //NOOP (unsupported)
      }
    });

    String field = in.getLeftExpression().toString();

    if(in.isNot()) {
      addFilter("$nin", field, inArgs);
    } else {
      addFilter("$in", field, inArgs);
    }
  }

  @Override
  public void visit(IsNullExpression isnull) {
    ensureFilters();
    String field = isnull.getLeftExpression().toString();
    f.add(new FieldFilter("$blank", field, !isnull.isNot()));
  }

  @Override
  public void visit(LikeExpression like) {
    if(like.isNot()) {
      throw new UnsupportedOperationException("Negation of full text search is not supported");
    }

    String fieldName = like.getLeftExpression().toString();
    String termWithWilds = Snipper.in("'").snip(like.getRightExpression().toString()).middle();
    //String termWithWilds = Strs.betweenSingleQuotes(like.getRightExpression().toString());
    String term = termWithWilds.replace("%", "");

    boolean wildAtStart = termWithWilds.startsWith("%");
    boolean wildAtEnd = termWithWilds.endsWith("%");

    ensureFilters();
    if(!wildAtStart && wildAtEnd) {
      f.add(new FieldFilter("$bw", fieldName, term));
    } else if(wildAtStart && !wildAtEnd) {
      throw new UnsupportedOperationException("ends-with is not supported");
    } else {
      f.add(new FieldFilter("$search", fieldName, term));
    }
  }

  @Override
  public void visit(MinorThan expr) {
    addFilter(expr, "$lt");
  }

  @Override
  public void visit(MinorThanEquals expr) {
    addFilter(expr, "$lte");
  }

  @Override
  public void visit(NotEqualsTo expr) {
    addFilter(expr, "$neq");
  }

  private void pushF(FilterGroup filter) {
    if(filters == null) {
      //root filter
      filters = filter;
    } else {
      filters.add(filter);
    }

    f = filter;
    filterStack.add(filter);
  }

  private void popF() {
    int i = filterStack.size() - 1;
    filterStack.remove(i);
    if(filterStack.isEmpty()) {
      f = null;
    } else {
      f = filterStack.get(i - 1);
    }
  }

  private void addFilter(BinaryExpression expr, String op) {
    ensureFilters();
    String field = expr.getLeftExpression().toString();
    Object val = coerce(expr.getRightExpression());
    val = stripDoubleQuotes(val);
    f.add(new FieldFilter(op, field, val));
  }

  private Object stripDoubleQuotes(Object val) {
    if(val instanceof String && ((String) val).startsWith("\"") && ((String) val).endsWith("\"")) {
      val = val.toString().substring(1, val.toString().length()-1);
    }
    return val;
  }

  private void addFilter(String op, String field, List<Object> args) {
    ensureFilters();
    f.add(new FieldFilter(op, field, args));
  }

  private void ensureFilters() {
    if(filters == null) {
      filters = new FilterGroup();
      f = filters;
    }
  }

  private Object coerce(Expression expr) {
    if(expr instanceof LongValue) {
      return ((LongValue)expr).getValue();
    } else if(expr instanceof DoubleValue) {
      return ((DoubleValue)expr).getValue();
    } else if(expr instanceof Column) {
      String str = expr.toString().toLowerCase();
      if("true".equals(str)) {
        return true;
      } else if("false".equals(str)) {
        return false;
      } else {
        return expr.toString();
      }
    } else if (expr instanceof StringValue){
      return (((StringValue)expr).getValue());
    } else {
      return expr.toString();
    }
  }

}
