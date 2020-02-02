package luckycharms.util.progress;

import luckycharms.util.events.AChanger;

public class ProgressManager extends AChanger {
   protected final Object mutex = this;
   protected volatile float percentComplete = 0f;
   protected volatile String message = "";

   public String getMessage() { return message; }

   public float getPercentComplete() { return percentComplete; }

   public Progress getProgress() {
      synchronized (mutex) {
         return new Progress(message, percentComplete);
      }
   }

   public void setProgress(float percentComplete) {
      setProgress(null, percentComplete);
   }

   public void setProgress(String message, float percentComplete) {
      synchronized (mutex) {
         updateMessage(message);
         updatePercent(percentComplete);
      }
      onChange();
   }

   protected void updatePercent(float percent) {
      float p = percent;
      if (Float.isFinite(p)) {
         if (p < 0f || p == -0f) {
            p = 0f;
         } else if (p > 1f) {
            p = 1f;
         }
      } else if (Float.POSITIVE_INFINITY == percent) {
         p = 1f;
      } else if (Float.NEGATIVE_INFINITY == percent) {
         p = 0f;
      } else {
         p = Float.NaN;
      }
      this.percentComplete = p;
   }

   protected void updateMessage(String newMsg) {
      if (newMsg == null) {
         return;
      }
      message = newMsg;
   }

}
