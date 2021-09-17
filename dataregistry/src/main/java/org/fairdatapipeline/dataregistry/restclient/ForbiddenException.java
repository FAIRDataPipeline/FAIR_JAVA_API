package org.fairdatapipeline.dataregistry.restclient;

public class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException(String message, Exception e) {
    super(message, e);
  }
}
