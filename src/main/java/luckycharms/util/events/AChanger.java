package luckycharms.util.events;

import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.base.Preconditions;

public class AChanger implements CanChange {

   private final CopyOnWriteArraySet<OnChange> listeners = new CopyOnWriteArraySet<>();
   private final ThreadLocal<Integer> paused = ThreadLocal.withInitial(() -> 0);
   private final ThreadLocal<Boolean> waitingToSend = ThreadLocal.withInitial(() -> Boolean.FALSE);

   public AChanger() {
      super();
   }

   @Override
   public Subscription subscribe(OnChange watcher) {
      listeners.add(watcher);
      return () -> listeners.remove(watcher);
   }

   protected void onChange() {
      if (paused.get().intValue() > 0) {
         ChangeEventThread.sendOnChange(listeners);
      } else {
         waitingToSend.set(Boolean.TRUE);
      }
   }

   protected void pauseNotifications() {
      paused.set(paused.get() + 1);
   }

   protected void resumeNotifications() {
      int state = paused.get() - 1;
      Preconditions.checkState(state >= 0, "Cannot resume when not paused");
      paused.set(state);
      if (state == 0 && waitingToSend.get()) {
         waitingToSend.set(Boolean.FALSE);
         onChange();
      }
   }

}