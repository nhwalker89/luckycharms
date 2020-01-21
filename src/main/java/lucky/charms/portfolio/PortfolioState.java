package lucky.charms.portfolio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import luckycharms.time.units.DaysKey;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.LazySizeable;
import luckycharms.util.sizeable.Sizes;

public class PortfolioState implements ISizeable {

//   public static final Converter<PortfolioState, byte[]> FORMAT = Converter
//         .from(PortfolioState::toProto, PortfolioState::new)
//         .andThen(new ProtobufSerializer<>(PortfolioStateProto::parseFrom));

   private double cash;

   private List<Position> positions;

   private final LazySizeable byteSize = new LazySizeable(this::computeByteSize);

   public PortfolioState(double cash, List<Position> positions) {
      this.cash = cash;
      this.positions = ImmutableList.copyOf(positions.stream().sorted().toArray(Position[]::new));
   }

//   public PortfolioState(PortfolioStateProto proto) {
//      cash = proto.getCash();
//      Position[] pos = proto.getPositionList().stream().map(Position::new).sorted()
//            .toArray(Position[]::new);
//      this.positions = ImmutableList.copyOf(pos);
//   }

   public PortfolioState updatePositions(Map<String, Integer> newPositions, DaysKey today) {
      Map<String, Position> curMap = positions.stream()
            .collect(Collectors.toMap(Position::getSymbol, Function.identity()));
      ArrayList<Position> values = new ArrayList<>(positions.size());
      for (Entry<String, Integer> newPos : newPositions.entrySet()) {
         Position oldPos = curMap.get(newPos.getKey());
         if (oldPos == null) {
            values.add(new Position(newPos.getKey(), today, newPos.getValue()));
         } else {
            values.add(oldPos.updateQty(newPos.getValue(), today));
         }
      }
      return new PortfolioState(cash, values);
   }

   public PortfolioState updateCash(double newCash) {
      return new PortfolioState(newCash, positions);
   }

   public List<Position> getPositions() { return positions; }

   public double getCash() { return cash; }

//   public PortfolioWorth currentWorth(RunnerContext context) {
//      return new PortfolioWorth(this, context);
//   }

//   public PortfolioStateProto toProto() {
//      return PortfolioStateProto.newBuilder()//
//            .setCash(cash)//
//            .addAllPosition(positions.stream().map(Position::toProto).collect(Collectors.toList()))//
//            .build();
//   }

   @Override
   public double byteSize() {
      return byteSize.byteSize();
   }

   public Position getPosition(String symbol) {
      Position pos = new Position(symbol);
      int idx = Collections.binarySearch(positions, pos);
      if (idx > 0) {
         return positions.get(idx);
      }
      return null;
   }

   protected double computeByteSize() {
      return (double) (ISizeable.sum(positions.stream()) + Sizes.DOUBLE);
   }
}
