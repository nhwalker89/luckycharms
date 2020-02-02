package lucky.charms.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import luckycharms.protos.portfolio.PositionProto;
//import lucky.charms.protos.PositionProto;
import luckycharms.time.units.DaysKey;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.LazySizeable;
import luckycharms.util.sizeable.Sizes;

public final class Position implements Comparable<Position>, ISizeable {
   private final ImmutableList<PositionShareData> shares;
   private final String symbol;
   private final LazySizeable byteSize = new LazySizeable(this::computeByteSize);

   public Position(String symbol) {
      this.symbol = symbol;
      this.shares = ImmutableList.of();
   }

   public Position(String symbol, List<PositionShareData> data) {
      this.symbol = symbol;
      this.shares = ImmutableList.sortedCopyOf(data);
   }

   public Position(String symbol, DaysKey today, int qty) {
      this.symbol = symbol;
      PositionShareData data = new PositionShareData(today);
      ImmutableList.Builder<PositionShareData> list = ImmutableList.builderWithExpectedSize(qty);
      for (int i = 0; i < qty; i++) {
         list.add(data);
      }
      this.shares = list.build();
   }

   public Position(PositionProto proto) {
      this.symbol = proto.getSymbol();
      this.shares = ImmutableList.copyOf(proto.getSharesList().stream().map(PositionShareData::new)
            .sorted().collect(Collectors.toList()));
   }

   private Position(String symbol, List<PositionShareData> data, boolean ALREADY_SORTED) {
      this.symbol = symbol;
      this.shares = ImmutableList.copyOf(data);
   }

   @Override
   public double byteSize() {
      return byteSize.byteSize();
   }

   protected double computeByteSize() {
      return (double) (ISizeable.sum(shares.stream()) + Sizes.sizes(symbol));
   }

   public Position updateQty(int newQty, DaysKey today) {
      if (newQty < shares.size()) {
         // shares were sold
         return new Position(symbol, shares.subList(shares.size() - newQty, shares.size()), true);
      } else if (newQty > shares.size()) {
         // shares were bought
         int delta = newQty - shares.size();
         ArrayList<PositionShareData> data = new ArrayList<>(newQty);
         data.addAll(shares);
         PositionShareData newData = new PositionShareData(today);
         for (int i = 0; i < delta; i++) {
            data.add(newData);
         }
         return new Position(symbol, data);
      } else {
         return this;
      }
   }

   public String getSymbol() { return symbol; }

   public ImmutableList<PositionShareData> getShares() { return shares; }

   public ImmutableList<PositionShareData> getShares(Range<DaysKey> range) {
      int start = 0;
      int fence = shares.size();

      // ignore to early
      for (; start < fence; start++) {
         if (range.contains(shares.get(start).getPurchaseDate())) {
            // HUZZAH!
            break;
         }
      }

      // ignore to late
      for (; fence >= start; fence--) {
         if (range.contains(shares.get(fence - 1).getPurchaseDate())) {
            // HUZZAH!
            break;
         }
      }
      if (fence <= start) {
         return ImmutableList.of();
      } else {
         return shares.subList(start, fence);
      }
   }

   public int getSharesCount() { return shares.size(); }

   @Override
   public int compareTo(Position o) {
      return this.symbol.compareTo(o.symbol);
   }

   @Override
   public int hashCode() {
      return Objects.hash(shares, symbol);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      Position other = (Position) obj;
      return Objects.equals(shares, other.shares) && Objects.equals(symbol, other.symbol);
   }

   @Override
   public String toString() {
      return symbol + "[qty:" + getSharesCount() + "]";
   }

   public PositionProto toProto() {
      return PositionProto.newBuilder().setSymbol(symbol)
            .addAllShares(
                  shares.stream().map(PositionShareData::toProto).collect(Collectors.toList()))
            .build();
   }

   public Position merge(Position other) {
      if (!getSymbol().equals(other.getSymbol())) {
         throw new IllegalArgumentException("Cannot merge positions of different symbols");
      }
      String symbol = other.getSymbol();
      return new Position(symbol, Ordering.natural().//
            immutableSortedCopy(Iterables.concat(shares, other.shares)));

   }
}
