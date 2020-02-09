package luckycharms.guis;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lucky.charms.portfolio.PortfolioDataSet;
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
      marketDays.setOnAction(e -> change(new DataSetViewerPage<>(//
            MarketDayDataSet.instance().toString(), //
            MarketDayDataSet.instance().pagedDataSet())));
      viewers.getItems().add(marketDays);

      MenuItem stockDailyPrices = new MenuItem("Stock 1 Day Prices");
      stockDailyPrices.setOnAction(e -> changeToDailyStocks());
      viewers.getItems().add(stockDailyPrices);

      MenuItem stockFifteenMinPrices = new MenuItem("Stock 15 Min Prices");
      stockFifteenMinPrices.setOnAction(e -> changeToFifteenMinStocks());
      viewers.getItems().add(stockFifteenMinPrices);

      Menu portfolios = new Menu("Portfolios");
      for (String portfolioName : PortfolioDataSet.getSavedPortfolios()) {
         MenuItem portMenuItem = new MenuItem(portfolioName);
         portMenuItem.setOnAction(e -> changeToPortfolio(portfolioName));
         portfolios.getItems().add(portMenuItem);
      }
      viewers.getItems().add(portfolios);
//
//      SortedSetMultimap<String, String> groupedSymbols = MultimapBuilder.treeKeys().treeSetValues()
//            .build();
//      for (String symbol : StockUniverse.SP500) {
//         groupedSymbols.put(symbol.substring(0, 1), symbol);
//      }
//      for (String groupKey : groupedSymbols.keySet()) {
//         Menu datasets = new Menu("Daily Prices[" + groupKey + "]");
//
//         for (String symbol : groupedSymbols.get(groupKey)) {
//            DailyPriceDataSet ds = DailyPriceDataSet.instance(symbol);
//            MenuItem item = new MenuItem(symbol);
//            datasets.getItems().add(item);
//            item.setOnAction(e -> change(ds.pagedDataSet()));
//         }
//         viewers.getItems().add(datasets);
//      }
   }

   private void changeToPortfolio(String portfolioName) {
      change(new DataSetViewerPage<>(PortfolioDataSet.instance(portfolioName)));
   }

   private void change(Node node) {
      content.getChildren().setAll(node);
   }

   private void changeToDailyStocks() {
      change(StockDataSetViewerPage.createDaily());
   }

   private void changeToFifteenMinStocks() {
      change(StockDataSetViewerPage.createFifteenMin());
   }

}
