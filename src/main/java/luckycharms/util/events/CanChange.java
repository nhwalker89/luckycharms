package luckycharms.util.events;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import luckycharms.concurrent.Scheduler;
import luckycharms.util.UncheckedException;

public interface CanChange {
   Subscription subscribe(OnChange change);

   default Subscription subscribe(Executor pEx, OnChange pAction) {
      return subscribe(() -> offloadedOnChange(pEx, pAction));
   }

   default Subscription subscribeBounced(Duration pBuffer, OnChange pAction) {
      long buffer = pBuffer.toMillis();
      return subscribe(new OnChange() {
         boolean queued = false;

         @Override
         public void onChange() {
            if (!queued) {
               queued = true;
               Scheduler.doLater(buffer, () -> {
                  queued = false;
                  ChangeEventThread.sendOnChange(pAction);
               }, ChangeEventThread::doOnChangThread);
            }
         }
      });
   }

   default Subscription subscribeBounced(Duration pBuffer, Executor pEx, OnChange pAction) {
      long buffer = pBuffer.toMillis();
      return subscribe(new OnChange() {
         boolean queued = false;

         @Override
         public void onChange() {
            if (!queued) {
               queued = true;
               Scheduler.doLater(buffer, () -> {
                  queued = false;

                  offloadedOnChange(pEx, pAction);

               }, ChangeEventThread::doOnChangThread);
            }
         }
      });
   }

   private static void offloadedOnChange(Executor pEx, OnChange pAction) {
      FutureTask<Void> task = new FutureTask<Void>(pAction::onChange, null);
      pEx.execute(task);
      try {
         task.get();
      } catch (InterruptedException | ExecutionException e) {
         throw new UncheckedException(e);
      }
   }
}
