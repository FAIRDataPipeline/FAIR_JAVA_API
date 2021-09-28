package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.annotation.JsonValue;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/** API URL contains the URL references to API resources. */
public class APIURL {
  private URL url;

  APIURL(String spec) throws MalformedURLException {
    this.url = new URL(spec);
  }

  APIURL(URI uri) throws MalformedURLException {
    this.url = uri.toURL();
  }

  APIURL(URL url) {
    this.url = url;
  }

  @Override
  @JsonValue
  public String toString() {
    return url.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof APIURL) return this.getUrl().equals(((APIURL) o).getUrl());
    return false;
  }

  /**
   * The URL for the API resource of this APIURL.
   *
   * @return The URL for the API resource of this APIURL.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * The Path part of the URL for the API resource of this APIURL.
   *
   * @return The Path part of the URL for the API resource of this APIURL.
   */
  public String getPath() {
    return url.getPath();
  }
}
