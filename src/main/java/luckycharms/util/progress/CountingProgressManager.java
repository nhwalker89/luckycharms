package luckycharms.util.progress;

public class CountingProgressManager extends ProgressManager {
   private final int total;
   private volatile int completed = 0;

   public CountingProgressManager(int total) {
      this.total = total < 0 ? 0 : total;
   }

   public int getTotal() { return total; }

   public int getCompleted() { return completed; }

   public void incrementCompleted() {
      incrementCompleted(null, 1);
   }

   public void incrementCompleted(String msg) {
      incrementCompleted(msg, 1);
   }

   public void incrementCompleted(int newlyCompleted) {
      incrementCompleted(null, newlyCompleted);
   }

   public void incrementCompleted(String msg, int newlyCompleted) {
      synchronized (mutex) {
         updateMessage(msg);
         updateComplete(this.completed + newlyCompleted);
         updatePercent();
      }
      onChange();
   }

   public void setCompleted(int completed) {
      setCompleted(null, completed);
   }

   public void setCompleted(String msg, int completed) {
      synchronized (mutex) {
         updateMessage(msg);
         updateComplete(completed);
         updatePercent();
      }
      onChange();
   }

   @Override
   public CountedProgress getProgress() {
      synchronized (mutex) {
         return new CountedProgress(message, percentComplete, completed, total);
      }
   }

   protected void updateComplete(int completed) {
      this.completed = completed < 0 ? 0 : completed;
   }

   protected void updatePercent() {
      if (total == 0) {
         if (completed == 0) {
            updatePercent(0f);
         } else {
            updatePercent(1f);
         }
      } else {
         float p = ((float) completed) / ((float) total);
         updatePercent(p);
      }
   }
}
