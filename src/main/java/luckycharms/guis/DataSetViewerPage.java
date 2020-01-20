package luckycharms.guis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import luckycharms.storage.IDataSet;

public class DataSetViewerPage<K, V> extends BorderPane {
   private static final String DIVISOR = System.lineSeparator() + Strings.repeat("=", 50)
         + System.lineSeparator();

   @SuppressWarnings("rawtypes")
   private static final Comparator reverseOrder = Comparator.naturalOrder().reversed();

   private final IDataSet<K, V> dataset;
   private final ListView<Object> pageKeyListView;
   private final TextArea textArea;

   public DataSetViewerPage(IDataSet<K, V> ds) {
      dataset = ds;
      pageKeyListView = new ListView<>();
      pageKeyListView.setMinWidth(300);
      pageKeyListView.setEditable(false);
      pageKeyListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

      textArea = new TextArea();
      textArea.setEditable(false);
      pageKeyListView.getSelectionModel().getSelectedItems()
            .addListener(new InvalidationListener() {

               @Override
               public void invalidated(Observable observable) {
                  textArea.setText(computeText());
               }
            });

      Label top = new Label(ds.toString());
      ListView<?> left = pageKeyListView;
      setTop(top);
      setLeft(left);
      setCenter(textArea);

      onRefresh();
   }

   private void onRefresh() {
      updateKeys();
      textArea.setText(computeText());
   }

   private void updateKeys() {
      pageKeyListView.getSelectionModel().clearSelection();
      pageKeyListView.getItems().clear();

      Object[] keys = dataset.keys().toArray();
      Collections.reverse(Arrays.asList(keys));

      pageKeyListView.getItems().addAll(keys);
   }

   @SuppressWarnings("unchecked")
   private String computeText() {
      List<Object> selectedItems = new ArrayList<>(
            pageKeyListView.getSelectionModel().getSelectedItems());
      selectedItems.sort(reverseOrder);
      return selectedItems.stream().map(k -> grabValueString(dataset, k))
            .collect(Collectors.joining(DIVISOR));

   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private String grabValueString(IDataSet dataSet, Object key) {
      return String.valueOf(dataSet.get(key));
   }
}
