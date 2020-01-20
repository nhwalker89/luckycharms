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
import java.util.List;

import luckycharms.alpaca.json.calendar.CalendarEndpointResponse;
import luckycharms.config.SecretConfig;
import luckycharms.datasets.calendar.MarketDayData;
import luckycharms.json.JsonFactory;
import luckycharms.rest.RequestCounter;
import luckycharms.rest.RestException;
import luckycharms.rest.RestRequest;
import luckycharms.rest.RestResponse;
import luckycharms.rest.UriBuilder;
import luckycharms.time.units.DaysKey;

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

   private static final int REQUEST_COUNT_NUMBER_LIMIT = 200 - 1/* 1 = safety buffer */;
   private static final Duration REQUEST_COUNT_TIME_LIMIT_MILLIS = Duration.ofMinutes(1)
         .minusMillis(10/* safety buffer */);

   private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

   private static final String API_V2_URL_ROOT = "https://paper-api.alpaca.markets/v2/";
//   private static final String API_V1_URL_ROOT = "https://data.alpaca.markets/v1/";

   private static final String CALENDAR_ENDPOINT = API_V2_URL_ROOT + "calendar";
//   private static final String BARS_ENDPOINT_ROOT = API_V1_URL_ROOT + "bars/";
//   private static final String BARS_1D_ENDPOINT = BARS_ENDPOINT_ROOT + "1D";
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
            DaysKey date = DaysKey.of(LocalDate.parse(point.date).toEpochDay());
            LocalTime open = point.open == null ? null : LocalTime.parse(point.open);
            LocalTime close = point.close == null ? null : LocalTime.parse(point.close);
            MarketDayData data = new MarketDayData(date, open, close);
            list.add(data);
         }
         return list;
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
      sRequestLog.info("Request Complete: {}", stringResponse);
      return stringResponse;
   }
}