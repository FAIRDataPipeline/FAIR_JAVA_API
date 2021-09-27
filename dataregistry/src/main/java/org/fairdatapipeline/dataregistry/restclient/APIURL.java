package org.fairdatapipeline.dataregistry.restclient;

import com.fasterxml.jackson.annotation.JsonValue;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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

  public URL getUrl() {
    return url;
  }

  public String getPath() {
    return url.getPath();
  }
}
