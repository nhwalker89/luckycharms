package luckycharms.guis;

import javafx.geometry.Pos;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import luckycharms.datasets.calendar.MarketDayDataSet;

public class LuckyCharmsRoot extends VBox {
   private final StackPane content;

   public LuckyCharmsRoot() {

      // Build Menu Bar
      MenuBar bar = new MenuBar();
      createMenuBar(bar);

      // Build Content Pane
      content = new StackPane();

      // Add to root
      getChildren().addAll(bar, content);

      // Size Rules
      VBox.setVgrow(bar, Priority.NEVER);
      VBox.setVgrow(content, Priority.ALWAYS);
      setFillWidth(true);
      setAlignment(Pos.TOP_LEFT);
   }

   private void createMenuBar(MenuBar bar) {
      Menu viewers = new Menu("Viewers");
      createViewerContents(viewers);
      bar.getMenus().add(viewers);
   }

   private void createViewerContents(Menu viewers) {
      MenuItem marketDays = new MenuItem("MarketDays");
      marketDays.setOnAction(e -> {
         content.getChildren()
               .setAll(new DataSetViewerPage<>(MarketDayDataSet.instance().pagedDataSet()));
      });
      viewers.getItems().add(marketDays);
   }

}
