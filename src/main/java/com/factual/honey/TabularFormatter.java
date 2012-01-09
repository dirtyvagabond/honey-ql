package com.factual.honey;

import java.util.List;
import java.util.Map;

import com.factual.driver.Tabular;
import com.google.common.collect.Lists;
import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

public class TabularFormatter {
  private List<String> columns;
  private String firstCol;


  /**
   * Preset the columns to format (by name)
   */
  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  public void setFirstColumn(String col) {
    firstCol = col;
  }

  public String formatCount(Tabular table) {
    TableFormatter tf = new SimpleTableFormatter(true);
    tf.nextRow();
    tf.nextCell().addLine("count");
    tf.nextRow();
    tf.nextCell().addLine(table.getData().size() + "");
    return asTextTable(tf);
  }

  public String format(Tabular table) {
    if(!table.getData().isEmpty()) {
      TableFormatter tf = new SimpleTableFormatter(true).nextRow();
      setColHeadings(tf, prepCols(table));
      formatRows(table, tf);
      return asTextTable(tf);
    } else {
      return "[No Results]\n";
    }
  }

  private void formatRows(Tabular table, TableFormatter tf) {
    for(Map<?, ?> record : table.getData()) {
      tf.nextRow();
      for(String col : columns) {
        Object valObj = record.get(col);
        String val = (valObj == null ? "null" : valObj.toString());
        tf.nextCell()
        .addLine(val);
      }
    }
  }

  private void setColHeadings(TableFormatter tf, List<String> columns) {
    for(String col : columns) {
      tf.nextCell().addLine(col.toString());
    }
  }

  /**
   * Make sure this formatter knows the column names,
   * and enforces any requested order.
   */
  private List<String> prepCols(Tabular table) {
    ensureNonNullCols(table);
    obeyFirst();
    return columns;
  }

  /**
   * Make sure there's a list of column names
   */
  private void ensureNonNullCols(Tabular table) {
    if(columns == null) {
      columns = Lists.newArrayList();
      for(String col : table.getData().iterator().next().keySet()) {
        columns.add(col);
      }
    }
  }

  /**
   * If a firstCol name is set, make sure it's the first column
   */
  private void obeyFirst() {
    if(firstCol != null) {
      columns.remove(firstCol);
      columns.add(0, firstCol);
    }
  }

  private String asTextTable(TableFormatter tf) {
    StringBuilder sbul = new StringBuilder();
    for(String line : tf.getFormattedTable()) {
      sbul.append(line).append("\n");
    }
    return sbul.toString();
  }

}
