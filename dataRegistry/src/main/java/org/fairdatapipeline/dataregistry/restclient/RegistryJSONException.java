package org.fairdatapipeline.dataregistry.restclient;

public class RegistryJSONException extends RuntimeException {
  public RegistryJSONException(String message) {
    super(message);
  }

  public RegistryJSONException(String message, Exception e) {
    super(message, e);
  }
}
