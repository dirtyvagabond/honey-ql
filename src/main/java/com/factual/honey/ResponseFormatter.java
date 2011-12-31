package com.factual.honey;

import java.util.List;
import java.util.Map;

import com.factual.ReadResponse;
import com.google.common.collect.Lists;
import com.inamik.utils.SimpleTableFormatter;
import com.inamik.utils.TableFormatter;

public class ResponseFormatter {
  private String[] columns;



  public String format(ReadResponse resp) {
    if(!resp.isEmpty()) {
      List<String> columnNames = Lists.newArrayList();
      if(columns != null) {
        for(String col : columns) {
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

      for(Map<?, ?> record : resp.getData()) {
        tf.nextRow();
        for(Object columnName : columnNames) {
          Object valObj = record.get(columnName);
          String val = (valObj == null ? "null" : valObj.toString());
          tf.nextCell()
          .addLine(val);
        }

      }

      StringBuilder sbul = new StringBuilder();
      for(String line : tf.getFormattedTable()) {
        sbul.append(line).append("\n");
      }
      return sbul.toString();
    } else {
      return "[No Results]\n";
    }
  }

  public void setColumns(String[] columns) {
    this.columns = columns;
  }

}
