package luckycharms.portfolio;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import com.google.common.base.Converter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.DoubleValue;

import luckycharms.protos.portfolio.PortfolioStateProto;
import luckycharms.protos.portfolio.PortfolioWorthProto;
import luckycharms.protos.portfolio.SharePriceProto;
import luckycharms.runner.EPriceHint;
import luckycharms.runner.IRunnerContext;
import luckycharms.util.ProtobufSerializer;
import luckycharms.util.Tabler;
import luckycharms.util.sizeable.ISizeable;
import luckycharms.util.sizeable.LazySizeable;
import luckycharms.util.sizeable.SizeableValue.SizeableString;
import luckycharms.util.sizeable.Sizes;

public class PortfolioWorth implements ISizeable {

   public static final Converter<PortfolioWorth, byte[]> BYTE_FORMAT = Converter
         .from(PortfolioWorth::toProto, PortfolioWorth::new)
         .andThen(new ProtobufSerializer<>(PortfolioWorthProto::parseFrom));

   private final PortfolioState portfolioState;
   private final ImmutableMap<String, Double> pricesPerShare;
   private final LazySizeable byteSize = new LazySizeable(this::computeByteSize);

   public PortfolioWorth(PortfolioState state, IRunnerContext ctx) {
      this.portfolioState = state;
      this.pricesPerShare = ImmutableMap.copyOf(
            ctx.currentPrices(state.getPositions().keySet().iterator(), EPriceHint.DEFAULT));
   }

   public PortfolioWorth(PortfolioWorthProto proto) {
      PortfolioStateProto stateProto = proto.getPortfolioState();
      portfolioState = new PortfolioState(stateProto);

      List<SharePriceProto> sharePricesProto = proto.getPricesList();
      ImmutableMap.Builder<String, Double> sharePricesMap = ImmutableMap.builder();
      for (SharePriceProto priceProto : sharePricesProto) {
         if (priceProto.hasPricePerShare()) {
            sharePricesMap.put(priceProto.getSymbol(), priceProto.getPricePerShare().getValue());
         }
      }
      pricesPerShare = sharePricesMap.build();

   }

   public Map<String, Double> getPricesPerShare() { return pricesPerShare; }

   public OptionalDouble getPricePerShare(String symbol) {
      Double v = pricesPerShare.get(symbol);
      return v == null ? OptionalDouble.empty() : OptionalDouble.of(v.doubleValue());
   }

   public OptionalDouble getPriceOfPosition(Position pos) {
      OptionalDouble price = getPricePerShare(pos.getSymbol());
      if (price == null || price.isEmpty()) {
         return OptionalDouble.empty();
      }
      return OptionalDouble.of(price.getAsDouble() * pos.getSharesCount());
   }

   public PortfolioState getPortfolioState() { return portfolioState; }

   public double getTotalWorth() {
      return portfolioState.getPositions().values().parallelStream()//
            .map(this::getPriceOfPosition)//
            .mapToDouble(e -> e.orElse(0d)).sum() + portfolioState.getCash();
   }

   public boolean hasUnresolvedPositions() {
      return portfolioState.getPositions().values().parallelStream()//
            .anyMatch(pos -> getPricePerShare(pos.getSymbol()).isEmpty());
   }

   public List<Position> unresolvedPositions() {
      return ImmutableList.copyOf(portfolioState.getPositions().values().parallelStream()//
            .filter(pos -> getPricePerShare(pos.getSymbol()).isEmpty())//
            .toArray(Position[]::new));
   }

   public PortfolioWorthProto toProto() {
      PortfolioWorthProto.Builder bldr = PortfolioWorthProto.newBuilder();
      bldr.setPortfolioState(portfolioState.toProto());
      pricesPerShare.forEach((symbol, price) -> {
         bldr.addPricesBuilder()//
               .setSymbol(symbol).setPricePerShare(DoubleValue.of(price));
      });
      return bldr.build();
   }

   @Override
   public double byteSize() {
      return byteSize.byteSize();
   }

   protected double computeByteSize() {
      return (double) (ISizeable.sum(pricesPerShare.keySet().stream().map(SizeableString::new))
            + (pricesPerShare.size() * Sizes.DOUBLE));
   }

   @Override
   public String toString() {
      double totalWorth = getTotalWorth();
      double cash = getPortfolioState().getCash();
      Tabler positionTable = new Tabler();
      positionTable.name("Portfolio Worth");
      positionTable.setDefaultPrecision(2);
      positionTable.headers("Symbol", "Qty", "$ Per Share", "$ Total", "Dates");
      for (Position pos : getPortfolioState().getPositions().values()) {
         LinkedHashMap<LocalDate, Integer> datesMap = new LinkedHashMap<>();
         for (PositionShareData data : pos.getShares()) {
            datesMap.merge(data.getPurchaseDate().marketDate(), 1,
                  (a, b) -> a.intValue() + b.intValue());
         }

         String symbol = pos.getSymbol();
         int qty = pos.getSharesCount();
         double perShare = getPricePerShare(pos.getSymbol()).orElse(Double.NaN);
         double total = getPriceOfPosition(pos).orElse(Double.NaN);
         String dates = datesMap.entrySet().stream().map(e -> e.getKey() + "[" + e.getValue() + "]")
               .limit(5).collect(Collectors.joining(", "));
         if (datesMap.entrySet().size() > 5) {
            dates = dates + "...";
         }
         positionTable.row(symbol, qty, perShare, total, dates);
      }

      positionTable.row("CASH", "", "", cash, "");
      positionTable.row("WORTH", "", "", totalWorth, "");
      return positionTable.toString();
   }
}
