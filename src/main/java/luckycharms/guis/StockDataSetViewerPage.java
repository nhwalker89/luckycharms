package luckycharms.guis;

import java.util.List;

import com.google.common.collect.ImmutableList;

import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import luckycharms.config.StockUniverse;
import luckycharms.datasets.prices.DailyPriceDataSet;

public class StockDataSetViewerPage extends BorderPane {

   private final List<String> symbols;
   private final ListView<String> symbolsListView;

   public StockDataSetViewerPage() {
      this(StockUniverse.SP500);
   }

   public StockDataSetViewerPage(List<String> symbols) {
      this.symbols = ImmutableList.sortedCopyOf(symbols);
      symbolsListView = new ListView<>();
      symbolsListView.setMinWidth(150);
      symbolsListView.setEditable(false);
      symbolsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
      symbolsListView.getItems().addAll(this.symbols);

      symbolsListView.getSelectionModel().selectedItemProperty()
            .addListener((src, old, cur) -> updateView());

      ListView<?> left = symbolsListView;
      setLeft(left);

      updateView();
   }

   private void updateView() {
      String symbol = symbolsListView.getSelectionModel().getSelectedItem();
      if (symbol == null) {
         setCenter(new Pane());
      } else {
         DailyPriceDataSet dataset = DailyPriceDataSet.instance(symbol);
         DataSetViewerPage<?, ?> page = new DataSetViewerPage<>(dataset.toString(),
               dataset.pagedDataSet());
         setCenter(page);
      }
   }
}
