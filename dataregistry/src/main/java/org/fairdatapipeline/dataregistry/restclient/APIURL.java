package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.annotation.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/** API URL contains the URL references to API resources. */
public class APIURL {
  private URI uri;

  APIURL(String spec) throws URISyntaxException {
    this.uri = new URI(spec);
  }

  APIURL(URI uri) {
    this.uri = uri;
  }

  APIURL(URL url) throws URISyntaxException {
    this.uri = url.toURI();
  }

  @Override
  @JsonValue
  public String toString() {
    return uri.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof APIURL) return this.getUri().equals(((APIURL) o).getUri());
    return false;
  }

  @Override
  public int hashCode() {
    return this.uri.hashCode();
  }

  /**
   * The URL for the API resource of this APIURL.
   *
   * @return The URL for the API resource of this APIURL.
   */
  public URI getUri() {
    return uri;
  }

  /**
   * The Path part of the URL for the API resource of this APIURL.
   *
   * @return The Path part of the URL for the API resource of this APIURL.
   */
  public String getPath() {
    return uri.getPath();
  }
}
