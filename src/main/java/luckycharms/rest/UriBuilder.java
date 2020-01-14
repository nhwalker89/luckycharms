package luckycharms.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UriBuilder {

   public static UriBuilder create(String url) {
      return new UriBuilder(url);
   }

   private String url;

   private UriBuilder(String url) {
      this.url = url;
   }

   public UriBuilder param(Map<String, Object> params) throws RestException {
      for (Map.Entry<String, Object> e : params.entrySet()) {
         param(e.getKey(), String.valueOf(e.getValue()));
      }
      return this;
   }

   public UriBuilder param(String name, String value) throws RestException {
      Matcher matcher = Pattern.compile("\\{" + name + "\\}").matcher(url);
      int count = 0;
      while (matcher.find()) {
         count++;
      }
      if (count == 0) {
         throw new RestException("Can't find route parameter name \"" + name + "\"");
      }
      this.url = url.replaceAll("\\{" + name + "\\}", RestHelper.encode(value));
      return this;
   }

   public UriBuilder queryString(String name, Collection<?> value) throws RestException {
      for (Object cur : value) {
         queryString(name, cur);
      }
      return this;
   }

   public UriBuilder queryString(String name, Object value) throws RestException {
      StringBuilder queryString = new StringBuilder();
      if (url.contains("?")) {
         queryString.append("&");
      } else {
         queryString.append("?");
      }
      try {
         queryString.append(URLEncoder.encode(name, "UTF-8"));
         if (value != null) {
            queryString.append("=").append(URLEncoder.encode(String.valueOf(value), "UTF-8"));
         }
      } catch (UnsupportedEncodingException e) {
         throw new RestException(e);
      }
      url += queryString.toString();
      return this;
   }

   public UriBuilder queryString(Map<String, Object> parameters) throws RestException {
      if (parameters != null) {
         for (Map.Entry<String, Object> param : parameters.entrySet()) {
            queryString(param.getKey(), param.getValue());
         }
      }
      return this;
   }

   @Override
   public String toString() {
      return url;
   }

   @Override
   public int hashCode() {
      return Objects.hash(url);
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
      UriBuilder other = (UriBuilder) obj;
      return Objects.equals(url, other.url);
   }

   public URI build() throws RestException {
      try {
         return URI.create(url);
      } catch (IllegalArgumentException e) {
         throw new RestException(e);
      }
   }
}
