package luckycharms.rest;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class RequestCounter {
   private static final org.slf4j.Logger sLog = org.slf4j.LoggerFactory.getLogger(RequestCounter.class);

   private final Semaphore lock;
   private final long timelimit;
   private final long max;
   private final LinkedList<Long> times = new LinkedList<>();

   private long mRequestCount;

   public RequestCounter(Duration pTime, long pMax) {
      timelimit = pTime.toMillis();
      max = pMax;
      lock = new Semaphore(1, true);
   }

   public long startRequest() throws InterruptedException {
      lock.acquire();

      waitForClearance();

      mRequestCount += 1;
      return mRequestCount;
   }

   public void completeRequest() {
      long now = System.currentTimeMillis();
      times.add(now);
      lock.release();
   }

   public void purge() {
      long now = System.currentTimeMillis();
      long fence = now - timelimit;
      Iterator<Long> iter = times.iterator();
      while (iter.hasNext()) {
         long time = iter.next();
         if (time < fence) {
            iter.remove();
         } else {
            return;
         }
      }
   }

   public void waitForClearance() throws InterruptedException {
      purge();

      if (times.size() < max) {
         return;
      }

      long earliest = times.get(0);
      long falloffTime = earliest + timelimit + 1;
      long wait = falloffTime - System.currentTimeMillis();
      if (wait > 1) {
         sLog.info("To many requests recently. Waiting {} before continuing.", Duration.ofMillis(wait));
         Thread.sleep(wait);
      }

      purge();
   }
}
