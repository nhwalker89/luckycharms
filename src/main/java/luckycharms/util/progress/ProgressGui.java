package luckycharms.util.progress;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import luckycharms.concurrent.Scheduler;

public class ProgressGui<M extends ProgressManager> extends VBox {

   public static void main(String[] args) throws InterruptedException {
      ProgressGui<ProgressManager> gui = openPopup("Test123", true);

      for (int i = 0; i <= 20; i++) {
         gui.getProgressManager().setProgress("This is a message " + i, i / 20f);
         Thread.sleep(1_000);
      }
      Thread.sleep(50_000);
   }

   public static ProgressGui<ProgressManager> openPopup(String title, boolean closeWhenDone) {
      return openPopup(title, new ProgressManager(), closeWhenDone);
   }

   public static ProgressGui<CountingProgressManager> openPopup(String title, boolean closeWhenDone,
         int total) {
      return openPopup(title, new CountingProgressManager(total), closeWhenDone);
   }

   public static <M extends ProgressManager> ProgressGui<M> openPopup(String title, M mgr,
         boolean closeWhenDone) {
      ProgressGui<M> gui = new ProgressGui<M>(title, mgr);
      gui.showPopup(closeWhenDone);
      return gui;
   }

   private Alert popup = null;
   private boolean closeWhenComplete = false;

   private final String title;
   private final M progressManager;

   public ProgressGui(String title, M progressManager) {
      CheckFxSetup.verify();

      this.title = title;
      this.progressManager = progressManager;

      ProgressBar bar = new ProgressBar();
      Label label = new Label();
      progressManager.subscribe(Platform::runLater, () -> {
         update(bar, label);
      });
      update(bar, label);
      bar.setPrefWidth(400);
      getChildren().addAll(bar, label);
      setFillWidth(true);

   }

   private void update(ProgressBar bar, Label label) {
      Progress prog = progressManager.getProgress();
      if (prog.isIndeterminate()) {
         bar.setProgress(-1d);
      } else {
         bar.setProgress(prog.getPercent());
      }
      label.setText(prog.toString());
      if (prog.isComplete() && closeWhenComplete && popup != null) {
         Scheduler.doLater(2_000, () -> {
            if (popup != null) {
               popup.close();
               popup = null;
            }
         }, Platform::runLater);
      }

   }

   public String getTitle() { return title; }

   public M getProgressManager() { return progressManager; }

   private void showPopup(boolean closeWhenComplete) {
      if (!Platform.isFxApplicationThread()) {
         Platform.runLater(() -> showPopup(closeWhenComplete));
         return;
      }
      if (popup != null) {
         return;
      }
      this.closeWhenComplete = closeWhenComplete;
      popup = new Alert(AlertType.INFORMATION);
      popup.setTitle(title);
      popup.getDialogPane().setHeader(this);
      popup.show();
      Stage stage = (Stage) popup.getDialogPane().getScene().getWindow();
      stage.setAlwaysOnTop(true);
      stage.toFront(); // not sure if necessary

   }

   private static class CheckFxSetup {
      private static void verify() {
         // do nothing
      }

      static {
         try {
            Platform.startup(() -> {
            });
         } catch (IllegalStateException e) {
            // ignore - fx already setup
         }
      }

   }
}
