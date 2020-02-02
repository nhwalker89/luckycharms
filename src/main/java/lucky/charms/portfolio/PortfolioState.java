package lucky.charms.portfolio;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Converter;
import com.google.common.collect.ImmutableMap;

import lucky.charms.runner.IRunnerContext;
import luckycharms.protos.portfolio.PortfolioStateProto;
import luckycharms.time.units.DaysKey;
import luckycharms.util.ProtobufSerializer;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.LazySizeable;
import luckycharms.util.sizeable.SizeableValue.SizeableString;
import luckycharms.util.sizeable.Sizes;

public class PortfolioState implements ISizeable {

   public static final Converter<PortfolioState, byte[]> FORMAT = Converter
         .from(PortfolioState::toProto, PortfolioState::new)
         .andThen(new ProtobufSerializer<>(PortfolioStateProto::parseFrom));

   private final double cash;

   private final Map<String, Position> positions;

   private final LazySizeable byteSize = new LazySizeable(this::computeByteSize);

   public PortfolioState() {
      this(0.0, ImmutableMap.of());
   }

   public PortfolioState(double cash, Collection<Position> positions) {
      this.cash = cash;
      this.positions = positions.stream().collect(ImmutableMap.toImmutableMap(//
            Position::getSymbol, Function.identity(), //
            (a, b) -> a.merge(b)));
   }

   public PortfolioState(double cash, Map<String, Position> positions) {
      this.cash = cash;
      this.positions = ImmutableMap.copyOf(positions);
   }

   public PortfolioState(PortfolioStateProto proto) {
      this(proto.getCash(), proto.getPositionList().stream()//
            .map(Position::new).collect(Collectors.toList()));
   }

   public PortfolioState removePositions(Map<String, Integer> toRemove) {
      Map<String, Position> values = new HashMap<>(this.positions);
      for (Entry<String, Integer> entry : toRemove.entrySet()) {
         if (entry.getValue().intValue() < 0) {
            throw new IllegalArgumentException("Cannot remove negative positions");
         } else if (entry.getValue().intValue() == 0) {
            // do nothing
         } else {
            Position oldPos = values.get(entry.getKey());
            int oldShares = oldPos == null ? 0 : oldPos.getSharesCount();

            int count = oldShares;
            count -= entry.getValue().intValue();
            if (count < 0) {
               throw new IllegalArgumentException("Cannot remove " //
                     + entry.getValue().intValue() + " from "//
                     + oldShares + " shares of symbol " + entry.getValue());
            }
            if (count > 0) {
               values.put(entry.getKey(), oldPos.updateQty(count, null));
            }

         }

      }
      return new PortfolioState(getCash(), values);
   }

   public PortfolioState addPositions(DaysKey today, Map<String, Integer> toAdd) {
      Map<String, Position> values = new HashMap<>(this.positions);
      for (Entry<String, Integer> entry : toAdd.entrySet()) {
         if (entry.getValue().intValue() < 0) {
            throw new IllegalArgumentException("Cannot add negative positions");
         } else if (entry.getValue().intValue() == 0) {
            // do nothing
         } else {
            Position oldPos = values.get(entry.getKey());
            int oldShares = oldPos == null ? 0 : oldPos.getSharesCount();

            int count = oldShares;
            count += entry.getValue().intValue();
            if (count > 0) {
               values.put(entry.getKey(), oldPos == null //
                     ? new Position(entry.getKey(), today, count)//
                     : oldPos.updateQty(count, today));
            }

         }

      }
      return new PortfolioState(getCash(), values);
   }

   public PortfolioState updateCash(double newCash) {
      return new PortfolioState(newCash, positions);
   }

   public Map<String, Position> getPositions() { return positions; }

   public double getCash() { return cash; }

//   public PortfolioWorth currentWorth(RunnerContext context) {
//      return new PortfolioWorth(this, context);
//   }

   public PortfolioStateProto toProto() {
      return PortfolioStateProto.newBuilder()//
            .setCash(cash)//
            .addAllPosition(positions.values()//
                  .stream().map(Position::toProto).collect(Collectors.toList()))//
            .build();
   }

   @Override
   public double byteSize() {
      return byteSize.byteSize();
   }

   public Position getPosition(String symbol) {
      return positions.get(symbol);
   }

   public PortfolioWorth computeWorth(IRunnerContext ctx) {
      PortfolioWorth worth = new PortfolioWorth(this, ctx);
      return worth;
   }

   protected double computeByteSize() {
      return ISizeable.sum(positions.values().stream())//
            + ISizeable.sum(positions.keySet().stream().map(SizeableString::new))//
            + Sizes.DOUBLE;
   }
}
