package luckycharms.util.progress;

public class CountedProgress extends Progress {
   private final int complete;
   private final int total;

   public CountedProgress(String msg, float percent, int complete, int total) {
      super(msg, percent);
      this.complete = complete;
      this.total = total;
   }

   public int getComplete() { return complete; }

   public int getTotal() { return total; }

   @Override
   public String toString() {
      StringBuilder b = new StringBuilder();
      if (hasMessage()) {
         if (!isIndeterminate()) {
            appendPercentage(b);
            b.append(" - ");
         }
         appendCount(b);
         b.append(" ");
         appendMessage(b);
         return b.toString();
      } else if (!isIndeterminate()) {
         appendPercentage(b);
         b.append(" ");
         appendCount(b);
         return b.toString();
      } else {
         appendCount(b);
      }
      return b.toString();
   }

   private void appendCount(StringBuilder b) {
      b.append("[");
      b.append(complete);
      b.append(" / ");
      b.append(total);
      b.append("]");
   }
}
