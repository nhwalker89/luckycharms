package lucky.charms.portfolio;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.LazySizeable;
import luckycharms.util.sizeable.Sizes;

public class PortfolioWorth implements ISizeable {

//   public static final Converter<PortfolioWorth, byte[]> FORMAT = Converter
//         .from(PortfolioWorth::toProto, PortfolioWorth::new)
//         .andThen(new ProtobufSerializer<>(PortfolioWorthProto::parseFrom));

   private final ZonedDateTime timestamp;
   private final Map<Position, Double> pricesPerPosition;
   private final double totalWorth;
   private final double cash;
   private final boolean hasUnresolvedPositions;

   private final LazySizeable byteSize = new LazySizeable(this::computeByteSize);

//   public PortfolioWorth(PortfolioWorthProto proto) {
//      Instant inst = Instant.ofEpochSecond(proto.getTimestamp().getSeconds(),
//            proto.getTimestamp().getNanos());
//      this.timestamp = MarketTimeUtils.inMarketTime(inst);
//      this.cash = proto.getCash();
//      TreeMap<Position, Double> positions = new TreeMap<>();
//      boolean hasUnresolvedPositions = false;
//
//      double sum = this.cash;
//      for (PositionWithWorthProto pos : proto.getPositionList()) {
//         Position unbox = new Position(pos.getPosition());
//         if (pos.hasWorth()) {
//            positions.put(unbox, pos.getWorth().getValue());
//            sum += pos.getWorth().getValue();
//         } else {
//            hasUnresolvedPositions = true;
//            positions.put(unbox, Double.NaN);
//         }
//      }
//      this.pricesPerPosition = Collections.unmodifiableMap(positions);
//      this.hasUnresolvedPositions = hasUnresolvedPositions;
//      this.totalWorth = sum;
//   }

   public PortfolioWorth(PortfolioState state
//         , RunnerContext context
   ) {

      Iterator<String> symbols = state.getPositions().stream().map(Position::getSymbol).distinct()
            .iterator();
      Map<String, Double> pricesPerShare = null;// TODO context.currentPrices(symbols);
      this.timestamp = null; // TODO context.now();

      boolean hasUnresolvedPosition = false;
      double sum = state.getCash();
      TreeMap<Position, Double> result = new TreeMap<>();
      for (Position pos : state.getPositions()) {
         Double price = pricesPerShare.get(pos.getSymbol());
         if (price == null || !Double.isFinite(price.doubleValue())) {
            hasUnresolvedPosition = true;
            result.put(pos, Double.NaN);
         } else {
            double value = price * pos.getSharesCount();
            sum += value;
            result.put(pos, value);
         }
      }

      this.pricesPerPosition = Collections.unmodifiableMap(result);
      this.totalWorth = sum;
      this.cash = state.getCash();
      this.hasUnresolvedPositions = hasUnresolvedPosition;
   }

   public ZonedDateTime getTimestamp() { return timestamp; }

   public Map<Position, Double> getPricesPerPosition() { return pricesPerPosition; }

   public double getTotalWorth() { return totalWorth; }

   public double getCash() { return cash; }

   public boolean hasUnresolvedPositions() {
      return hasUnresolvedPositions;
   }

   public List<Position> unresolvedPositions() {
      Position[] unresolved = pricesPerPosition//
            .entrySet().stream()//
            .filter(e -> e.getValue().isNaN())//
            .map(Entry::getKey)//
            .toArray(Position[]::new);
      return Arrays.asList(unresolved);
   }

//   public PortfolioWorthProto toProto() {
//      Instant inst = timestamp.toInstant();
//      PortfolioWorthProto.Builder b = PortfolioWorthProto.newBuilder();
//      b.getTimestampBuilder().setSeconds(inst.getEpochSecond()).setNanos(inst.getNano());
//      for (Entry<Position, Double> e : pricesPerPosition.entrySet()) {
//         PositionWithWorthProto.Builder pos = b.addPositionBuilder();
//         pos.setPosition(e.getKey().toProto());
//         if (!e.getValue().isNaN()) {
//            pos.getWorthBuilder().setValue(e.getValue());
//         }
//      }
//      b.setCash(cash);
//      return b.build();
//   }

   @Override
   public double byteSize() {
      return byteSize.byteSize();
   }

   protected double computeByteSize() {
      return (double) (ISizeable.sum(pricesPerPosition.keySet().stream())
            + (pricesPerPosition.size() * Sizes.DOUBLE) + Sizes.ZONED_DATE_TIME + Sizes.DOUBLE
            + Sizes.DOUBLE + Sizes.BOOLEAN);
   }
}
