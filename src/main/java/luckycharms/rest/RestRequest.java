package luckycharms.rest;

import java.net.URI;
import java.util.Objects;

public class RestRequest implements Comparable<RestRequest> {

   private final String api;
   private final long id;
   private final URI url;

   public RestRequest(String api, long count, URI url) {
      this.api = Objects.requireNonNull(api);
      this.id = count;
      this.url = Objects.requireNonNull(url);
   }

   public long getId() { return id; }

   public URI getUrl() { return url; }

   public String getApi() { return api; }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("RestRequest [api=");
      builder.append(api);
      builder.append(", id=");
      builder.append(id);
      builder.append(", url=");
      builder.append(url);
      builder.append("]");
      return builder.toString();
   }

   @Override
   public int hashCode() {
      return Objects.hash(api, id, url);
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
      RestRequest other = (RestRequest) obj;
      return Objects.equals(api, other.api) && id == other.id && Objects.equals(url, other.url);
   }

   @Override
   public int compareTo(RestRequest o) {
      int c = Long.compare(id, o.id);
      if (c == 0) {
         c = api.compareTo(o.api);
         if (c == 0) {
            c = url.compareTo(o.url);
         }
      }
      return c;
   }
}
