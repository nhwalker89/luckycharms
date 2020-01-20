package luckycharms.guis;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LuckyCharmsApp extends Application {

   @Override
   public void start(Stage primaryStage) throws Exception {
      Scene scene = new Scene(new LuckyCharmsRoot());
      primaryStage.setScene(scene);

      primaryStage.setWidth(700);
      primaryStage.setHeight(700);
      primaryStage.setMaximized(true);

      primaryStage.setResizable(true);
      primaryStage.show();
   }
}