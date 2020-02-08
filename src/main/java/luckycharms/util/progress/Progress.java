package luckycharms.util.progress;

import luckycharms.util.DecimalFormatter;

public class Progress {
   private final float percent;
   private final String message;

   public Progress(String msg, float percent) {
      this.percent = percent;
      this.message = msg;
   }

   public boolean hasMessage() {
      return !message.isEmpty();
   }

   public String getMessage() { return message; }

   public float getPercent() { return percent; }

   public boolean isIndeterminate() { return Float.isNaN(percent); }

   public boolean isComplete() { return !isIndeterminate() && percent >= 1f; }

   protected void appendPercentage(StringBuilder b) {
      DecimalFormatter.format(b, percent * 100d, 2);
      b.append('%');
   }

   protected void appendMessage(StringBuilder b) {
      b.append(message);
   }

   @Override
   public String toString() {
      if (hasMessage()) {
         StringBuilder b = new StringBuilder();
         if (!isIndeterminate()) {
            appendPercentage(b);
            b.append(" - ");
         }
         appendMessage(b);
         return b.toString();
      } else if (!isIndeterminate()) {
         StringBuilder b = new StringBuilder();
         appendPercentage(b);
         return b.toString();
      } else {
         return "<unknown>";
      }
   }
}
