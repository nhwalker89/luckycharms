package luckycharms.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import com.google.common.base.Strings;

public class Tabler {

   private static final int PRECISION = 2;
   private String name;
   private int defaultPrecision = PRECISION;
   private final List<Object> headers = new ArrayList<>();
   private final List<List<Object>> rows = new ArrayList<>();
   private final List<Integer> precision = new ArrayList<>();

   public Tabler() {}

   public Tabler name(String name) {
      this.name = name;
      return this;
   }

   public Tabler headers(Object... headers) {
      this.headers.addAll(Arrays.asList(headers));
      return this;
   }

   public Tabler row(Object... row) {
      this.rows.add(Arrays.asList(row));
      return this;
   }

   public String toString() {
      return toString(1);
   }

   public String toString(int pillars) {
      int columns = headers.size();
      columns = Math.max(columns, rows.stream().mapToInt(List::size).max().orElse(0));

      int totalColumns = columns * pillars;

      int maxRows = rows.size() / pillars;
      String[][] buffer = new String[maxRows + 1][columns * pillars];

      // Headers
      for (int pillar = 0; pillar < pillars; pillar++) {
         createRowText(headers, columns, buffer[0], pillar * columns);
      }

      // Rows
      for (int row = 0; row < maxRows; row++) {
         String[] toFill = buffer[1 + row];
         for (int pillar = 0; pillar < pillars; pillar++) {
            int pick = (maxRows * pillar) + row;
            if (pick < rows.size()) {
               createRowText(rows.get(pick), columns, toFill, pillar * columns);
            } else {
               createRowText(Collections.emptyList(), columns, toFill, pillar * columns);
            }
         }
      }

      // Find widths
      int total = 0;
      int[] widths = new int[totalColumns];
      for (int col = 0; col < totalColumns; col++) {
         int w = 0;
         for (int row = 0; row < buffer.length; row++) {
            w = Math.max(buffer[row][col].length(), w);
         }
         widths[col] = w;
         total += (w + 4);
      }
      total += (pillars + 1) * 1;

      // Create Format
      StringBuilder b = new StringBuilder();
      for (int pillar = 0; pillar < pillars; pillar++) {
         int offset = pillar * columns;
         b.append("|");
         for (int col = 0; col < columns; col++) {
            b.append("  %").append(widths[offset + col]).append("s  ");
         }
      }
      b.append("|").append(System.lineSeparator());
      String rowFormat = b.toString();

      StringBuilder appender = new StringBuilder();
      String sep = Strings.repeat("=", total);
      if (name != null) {
         appender.append(name).append(System.lineSeparator());
      }
      appender.append(sep).append(System.lineSeparator());
      try (Formatter formatter = new Formatter(appender)) {
         for (String[] row : buffer) {
            formatter.format(rowFormat, (Object[]) row);
         }
      }
      appender.append(sep);
      return appender.toString();
   }

   private void createRowText(List<Object> row, int columns, String[] out, int offset) {
      for (int col = 0; col < row.size(); col++) {
         Object obj = row.get(col);
         String txt;
         if (obj == null) {
            txt = "";
         } else if (obj instanceof BigDecimal) {
            txt = ((BigDecimal) obj).setScale(getPrecision(col), RoundingMode.HALF_UP)
                  .toPlainString();
         } else if (obj instanceof Double || obj instanceof Float) {
            double val = ((Number) obj).doubleValue();
            txt = format(val, getPrecision(col));
         } else {
            txt = obj.toString();
         }
         out[col + offset] = txt;
      }
      for (int col = row.size(); col < columns; col++) {
         out[col + offset] = "";
      }
   }

   private String format(double v, int scale) {
      long a;
      switch (scale) {
      case 0:
         return Long.toString(Math.round(v));
      case 1:
         a = Math.round(v * 10);
         break;
      case 2:
         a = Math.round(v * 100);
         break;
      case 3:
         a = Math.round(v * 1000);
         break;
      default:
         a = Math.round(v * Math.pow(10, scale));
         break;
      }
      StringBuilder b = new StringBuilder();
      b.append(a);
      b.insert(b.length() - scale, '.');
      return b.toString();
   }

   private int getPrecision(int col) {
      if (col < precision.size()) {
         Integer x = precision.get(col);
         if (x != null) {
            return x.intValue();
         }
      }
      return defaultPrecision;
   }
}
