package luckycharms.rest;

import java.net.http.HttpResponse;
import java.util.Objects;

import com.google.common.base.Preconditions;

import luckycharms.rest.exceptions.FailedResponse;
import luckycharms.util.ExceptionalFunction;

public class RestResponse<T> {

   public static RestResponse<String> of(RestRequest request, HttpResponse<String> response) {
      return new RestResponse<>(request, response.statusCode(), response.body(), response.body(),
            null);
   }

   public static <T> RestResponse<T> success(RestRequest request, int statusCode, String body,
         T payload) {
      return new RestResponse<T>(request, statusCode, body, payload, null);
   }

   public static <T> RestResponse<T> failure(RestRequest request, Throwable cause) {
      return new RestResponse<T>(request, -1, null, null, cause);
   }

   public static <T> RestResponse<T> failure(RestRequest request, int statusCode, Throwable cause) {
      return new RestResponse<T>(request, statusCode, null, null, cause);
   }

   public static <T> RestResponse<T> failure(RestRequest request, int statusCode, String body,
         Throwable cause) {
      return new RestResponse<T>(request, statusCode, body, null, Objects.requireNonNull(cause));
   }

   private final RestRequest request;
   private final int statusCode;
   private final String body;
   private final T payload;
   private final Throwable error;

   private RestResponse(RestRequest request, int statusCode, String body, T payload,
         Throwable error) {
      this.request = Objects.requireNonNull(request);
      this.statusCode = statusCode;
      this.body = body == null ? "" : body;
      if (error == null && !ResponseCodes.isSuccess(this.statusCode)) {
         this.error = new FailedResponse("Error Code=" + this.statusCode);
      } else {
         this.error = error;
      }
      if (this.error != null) {
         this.payload = null;
      } else {
         this.payload = payload;
      }
   }

   public boolean isSuccess() { return error == null; }

   public <X> RestResponse<X> map(ExceptionalFunction<? super T, ? extends X> fn) {
      if (!isSuccess()) {
         @SuppressWarnings({ "rawtypes", "unchecked" })
         RestResponse<X> coerce = (RestResponse) this;
         return coerce;
      }
      try {
         return success(request, statusCode, body, fn.apply(payload));
      } catch (Exception e) {
         return failure(request, statusCode, body, e);
      }
   }

   public RestRequest getRequest() { return request; }

   public String getBody() { return body; }

   public int getStatusCode() { return statusCode; }

   public Throwable getError() {
      Preconditions.checkState(!isSuccess());
      return error;
   }

   public T getPayload() {
      Preconditions.checkState(isSuccess());
      return payload;
   }

   @Override
   public String toString() {
      return shortToString();
   }

   public String shortToString() {
      StringBuilder builder = new StringBuilder();
      builder.append("RestResponse [request=");
      builder.append(request);
      builder.append(", statusCode=");
      builder.append(statusCode);
      builder.append(", body=");
      builder.append(body);
      if (isSuccess()) {
         builder.append(", payload=");
         builder.append(payload == null ? "null" : "set");
      } else {
         builder.append(", error=");
         builder.append(error);
      }
      builder.append("]");
      return builder.toString();
   }

   @Override
   public int hashCode() {
      return Objects.hash(body, error, payload, request, statusCode);
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
      RestResponse<?> other = (RestResponse<?>) obj;
      return Objects.equals(body, other.body) && Objects.equals(error, other.error)
            && Objects.equals(payload, other.payload) && Objects.equals(request, other.request)
            && statusCode == other.statusCode;
   }

}
