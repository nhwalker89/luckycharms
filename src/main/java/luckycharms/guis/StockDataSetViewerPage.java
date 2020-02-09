package luckycharms.guis;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import luckycharms.config.StockUniverse;
import luckycharms.datasets.prices.DailyPriceDataSet;
import luckycharms.datasets.prices.FifteenMinPriceDataSet;
import luckycharms.storage.IPagedDataSet;

public class StockDataSetViewerPage extends BorderPane {

   public static StockDataSetViewerPage createDaily() {
      return createDaily(StockUniverse.SP500);
   }

   public static StockDataSetViewerPage createDaily(List<String> symbols) {
      return new StockDataSetViewerPage(DailyPriceDataSet::instance, symbols);
   }

   public static StockDataSetViewerPage createFifteenMin() {
      return createFifteenMin(StockUniverse.SP500);
   }

   public static StockDataSetViewerPage createFifteenMin(List<String> symbols) {
      return new StockDataSetViewerPage(FifteenMinPriceDataSet::instance, symbols);
   }

   private final Function<String, IPagedDataSet<?, ?, ?>> dataSetFetcher;
   private final List<String> symbols;
   private final ListView<String> symbolsListView;

   public StockDataSetViewerPage(Function<String, IPagedDataSet<?, ?, ?>> dataSetFetcher) {
      this(dataSetFetcher, StockUniverse.SP500);
   }

   public StockDataSetViewerPage(Function<String, IPagedDataSet<?, ?, ?>> dataSetFetcher,
         List<String> symbols) {
      this.symbols = ImmutableList.sortedCopyOf(symbols);
      this.dataSetFetcher = Objects.requireNonNull(dataSetFetcher);
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
         IPagedDataSet<?, ?, ?> dataset = dataSetFetcher.apply(symbol);
         DataSetViewerPage<?, ?> page = new DataSetViewerPage<>(dataset.toString(),
               dataset.pagedDataSet());
         setCenter(page);
      }
   }

}
