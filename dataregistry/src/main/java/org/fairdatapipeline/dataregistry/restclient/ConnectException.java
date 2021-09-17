package org.fairdatapipeline.dataregistry.restclient;

public class ConnectException extends RuntimeException {
  public ConnectException(String message) {
    super(message);
  }

  public ConnectException(String message, Exception e) {
    super(message, e);
  }
}
