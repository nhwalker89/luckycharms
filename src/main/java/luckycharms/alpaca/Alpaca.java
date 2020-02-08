package luckycharms.alpaca;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import luckycharms.alpaca.json.bars.BarsEndpointResponse;
import luckycharms.alpaca.json.calendar.CalendarEndpointResponse;
import luckycharms.config.SecretConfig;
import luckycharms.config.StandardConfig;
import luckycharms.datasets.calendar.MarketDayData;
import luckycharms.datasets.prices.PriceBar;
import luckycharms.json.JsonFactory;
import luckycharms.logging.Markers;
import luckycharms.protos.datasets.PriceBarProto;
import luckycharms.rest.RequestCounter;
import luckycharms.rest.RestException;
import luckycharms.rest.RestRequest;
import luckycharms.rest.RestResponse;
import luckycharms.rest.UriBuilder;
import luckycharms.storage.KeyValuePair;
import luckycharms.time.units.DaysKey;
import luckycharms.util.progress.CountingProgressManager;
import luckycharms.util.progress.ProgressGui;

public class Alpaca {
   public static Alpaca instance() {
      return InstanceHolder.inst;
   }

   private static class InstanceHolder {
      private static final Alpaca inst = new Alpaca();
   }

   private Alpaca() {}

   private static final String API_NAME = "Alpaca";

   public static final org.slf4j.Logger sRequestLog = org.slf4j.LoggerFactory
         .getLogger("RestRequests." + API_NAME);
   public static final org.slf4j.Logger stdLog = org.slf4j.LoggerFactory.getLogger(Alpaca.class);

   private static final int REQUEST_COUNT_NUMBER_LIMIT = 200 - 1/* 1 = safety buffer */;
   private static final Duration REQUEST_COUNT_TIME_LIMIT_MILLIS = Duration.ofMinutes(1)
         .minusMillis(10/* safety buffer */);

   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

   private static final String API_V2_URL_ROOT = "https://paper-api.alpaca.markets/v2/";
   private static final String API_V1_URL_ROOT = "https://data.alpaca.markets/v1/";

   private static final String CALENDAR_ENDPOINT = API_V2_URL_ROOT + "calendar";
   private static final String BARS_ENDPOINT_ROOT = API_V1_URL_ROOT + "bars/";
   private static final String BARS_1D_ENDPOINT = BARS_ENDPOINT_ROOT + "1D";
//   private static final String CLOCK_ENDPOINT = API_V2_URL_ROOT + "clock";
//   private static final String ORDERS_ENDPOINT = API_V2_URL_ROOT + "orders";
//   private static final String POSITIONS_ENDPOINT = API_V2_URL_ROOT + "positions";

   private final String keyID = SecretConfig.alpacaKeyID();
   private final String secretKey = SecretConfig.alpacaSecretKey();

   private final RequestCounter sCounter = new RequestCounter(REQUEST_COUNT_TIME_LIMIT_MILLIS,
         REQUEST_COUNT_NUMBER_LIMIT);

   private final HttpClient httpClient = HttpClient.newBuilder()//
         .connectTimeout(DEFAULT_TIMEOUT)//
         .followRedirects(Redirect.NORMAL)//
         .build();

   public RestResponse<List<MarketDayData>> calendar(LocalDate start, LocalDate end)
         throws RestException {
      // Build Request URL
      UriBuilder urlBldr = UriBuilder.create(CALENDAR_ENDPOINT);
      if (start != null) {
         urlBldr.queryString("start", DaysKey
               .of(start.getYear(), start.getMonthValue(), start.getDayOfMonth()).toIsoFormat());
      }
      if (end != null) {
         urlBldr.queryString("end",
               DaysKey.of(end.getYear(), end.getMonthValue(), end.getDayOfMonth()).toIsoFormat());
      }
      URI url = urlBldr.build();

      // Make Request
      RestResponse<String> stringResponse = makeGetRequest(url);

      // Map Reply to valid response type and return
      RestResponse<List<MarketDayData>> response = stringResponse.map(str -> {
         CalendarEndpointResponse json = JsonFactory.WIRE.fromJson(str,
               CalendarEndpointResponse.class);
         List<MarketDayData> list = new ArrayList<>(json.size());
         for (CalendarEndpointResponse.CalendarPoint point : json) {
            if (point.date == null) {
               sRequestLog.error("Calendar Point was missing date");
               stdLog.error("Calendar Point was missing date");
            } else {
               DaysKey date = DaysKey.of(LocalDate.parse(point.date).toEpochDay());
               if (date.marketDate().compareTo(start) >= 0
                     && date.marketDate().compareTo(end) <= 0) {
                  LocalTime open = point.open == null ? null : LocalTime.parse(point.open);
                  LocalTime close = point.close == null ? null : LocalTime.parse(point.close);
                  MarketDayData data = new MarketDayData(date, open, close);
                  list.add(data);
               } else {
                  stdLog.debug("Skipping calendar day {}. Not in range of {} to {}", date, start,
                        end);
               }
            }
         }
         return list;
      });
      return response;
   }

   public void getBulkDailyBar(//
         DaysKey start, DaysKey end, List<String> symbols,
         Consumer<RestResponse<Map<String, List<KeyValuePair<DaysKey, PriceBar>>>>> process)
         throws RestException {
      boolean isRestProgressReportsEnabled = StandardConfig.isRestProgressReportsEnabled();

      final int maxSymbols = 195;
      final long maxDays = 975;
      int symbolIdx = 0;
      int symbolFence;
      List<List<String>> subsymbols = new ArrayList<>();
      while (symbolIdx < symbols.size()) {
         symbolFence = symbolIdx + maxSymbols;
         List<String> subsym = symbols//
               .subList(symbolIdx, Math.min(symbols.size(), symbolFence));
         subsymbols.add(subsym);
         symbolIdx = symbolFence;
      }

      class IndividualRequests {
         DaysKey start;
         DaysKey end;
         List<String> symbols;
         String description;
      }
      List<IndividualRequests> requests = new ArrayList<>();

      long dayIndex = start.index();
      long endIndex = end.index() + 1;
      long fence;
      while (dayIndex < endIndex) {
         fence = dayIndex + maxDays;
         for (int i = 0; i < subsymbols.size(); i++) {
            List<String> s = subsymbols.get(i);
            IndividualRequests request = new IndividualRequests();
            request.start = DaysKey.of(dayIndex);
            request.end = DaysKey.of(Math.min(fence, endIndex) - 1);
            request.symbols = s;
            if (isRestProgressReportsEnabled) {
               request.description = request.start + " - " + request.end + //
                     " [" + "Symbols " + s.get(0) + " to " + s.get(s.size() - 1) + " " + (i + 1)
                     + "/" + subsymbols.size() + "]";
            }
            requests.add(request);
         }
         dayIndex = fence;
      }
      CountingProgressManager progressReport = isRestProgressReportsEnabled
            ? new CountingProgressManager(requests.size())
            : null;
      if (isRestProgressReportsEnabled) {
         ProgressGui.openPopup("Bulk Daily Fetch", progressReport, true);
      }
      for (int i = 0; i < requests.size(); i++) {
         IndividualRequests request = requests.get(i);
         RestResponse<Map<String, List<KeyValuePair<DaysKey, PriceBar>>>> response = dailyPriceData(
               request.start, request.end, request.symbols);
         process.accept(response);
         if (isRestProgressReportsEnabled) {
            progressReport.incrementCompleted(request.description);
         }
      }
   }

   public RestResponse<Map<String, List<KeyValuePair<DaysKey, PriceBar>>>> dailyPriceData(
         DaysKey start, DaysKey end, List<String> symbols) throws RestException {
      // Build Request URL
      UriBuilder urlBldr = UriBuilder.create(BARS_1D_ENDPOINT);

      // Set Parameters
      String startStr = start.toIsoFormat();
      urlBldr.queryString("start", startStr);
      String endStr = end.next().toIsoFormat();
      urlBldr.queryString("until", endStr);
      urlBldr.queryString("symbols", symbols.stream().collect(Collectors.joining(",")));
      urlBldr.queryString("limit", "1000");

      URI url = urlBldr.build();

      // Make Request
      RestResponse<String> stringResponse = makeGetRequest(url);

      // Map Reply to valid response type and return
      RestResponse<Map<String, List<KeyValuePair<DaysKey, PriceBar>>>> response = stringResponse
            .map(str -> {
               BarsEndpointResponse json = JsonFactory.WIRE.fromJson(str,
                     BarsEndpointResponse.class);
               Map<String, List<KeyValuePair<DaysKey, PriceBar>>> results = new HashMap<>();
               for (Entry<String, BarsEndpointResponse.BarForSymbol> pointJson : json.entrySet()) {
                  List<KeyValuePair<DaysKey, PriceBar>> bars = results
                        .computeIfAbsent(pointJson.getKey(), key -> new ArrayList<>());
                  for (BarsEndpointResponse.BarEntry barJson : pointJson.getValue()) {
                     if (barJson.timestamp == null) {
                        sRequestLog.error("Daily Bar Point was missing timestamp");
                        stdLog.error("Daily Bar Point was missing timestamp");
                     } else {
                        DaysKey dayKey = barJson.getTimestamp().asDaysKey();
                        if (dayKey.compareTo(start) >= 0 && dayKey.compareTo(end) <= 0) {
                           PriceBarProto.Builder bld = PriceBarProto.newBuilder();
                           if (barJson.close != null) {
                              if (barJson.getClose() < 0) {
                                 stdLog.error("Negative value {} - {}", pointJson.getKey(),
                                       barJson);
                              } else {
                                 bld.getCloseBuilder().setValue(barJson.getClose());
                              }
                           }
                           if (barJson.open != null) {
                              if (barJson.getOpen().doubleValue() < 0) {
                                 stdLog.error("Negative value {} - {}", pointJson.getKey(),
                                       barJson);
                              } else {
                                 bld.getOpenBuilder().setValue(barJson.getOpen());
                              }
                           }
                           if (barJson.high != null) {
                              if (barJson.getHigh() < 0) {
                                 stdLog.error("Negative value {} - {}", pointJson.getKey(),
                                       barJson);

                              } else {
                                 bld.getHighBuilder().setValue(barJson.getHigh());
                              }
                           }
                           if (barJson.low != null) {
                              if (barJson.getLow() < 0) {
                                 stdLog.error("Negative value {} - {}", pointJson.getKey(),
                                       barJson);
                              } else {
                                 bld.getLowBuilder().setValue(barJson.getLow());
                              }
                           }
                           if (barJson.volume != null) {
                              if (barJson.getVolume() < 0) {
                                 stdLog.error("Negative value {} - {}", pointJson.getKey(),
                                       barJson);
                              } else {
                                 bld.getVolumeBuilder().setValue(barJson.getVolume());
                              }
                           }
                           bars.add(new KeyValuePair<>(dayKey, new PriceBar(bld.build())));
                        } else {
                           stdLog.debug(
                                 "Skipping bar point symbol {} day {}. Not in range of {} to {}",
                                 pointJson.getKey(), dayKey, start, end);
                        }
                     }
                  }
               }
               return results;
            });
      return response;
   }

   static enum HttpRequestType {
      GET
   }

   private RestResponse<String> makeGetRequest(URI url) {
      return makeRequest(url, HttpRequestType.GET);
   }

   private RestResponse<String> makeRequest(URI url, HttpRequestType type) {
      RestResponse<String> stringResponse;
      RestRequest request = null;
      long id = -1;
      try {
         // Get new request ID, wait if nessessary to avoid to many requests
         id = sCounter.startRequest();

         // Build request
         request = new RestRequest(API_NAME, id, url);
         HttpRequest.Builder httpRequestBldr = HttpRequest.newBuilder()//
               .uri(url)//
               .setHeader("APCA-API-KEY-ID", keyID)//
               .setHeader("APCA-API-SECRET-KEY", secretKey)//
               .timeout(DEFAULT_TIMEOUT)//
         ;
         switch (type) {
         case GET:
            httpRequestBldr = httpRequestBldr.GET();
            break;
         }
         HttpRequest httpRequest = httpRequestBldr.build();

         // Make Request
         HttpResponse<String> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofString());
         stringResponse = RestResponse.of(request, httpResponse);

      } catch (Exception t) {
         // Something unexpected went wrong - return failure
         if (request == null) {
            request = new RestRequest(API_NAME, id, url);
         }
         stringResponse = RestResponse.failure(request, t);
      } finally {
         sCounter.completeRequest();
      }
      sRequestLog.info(Markers.ALPACA, "Request Complete: {}", stringResponse);
      return stringResponse;
   }
}
