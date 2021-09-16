package org.fairdatapipeline.api;

public class RegistryObjectNotfoundException extends RuntimeException {
  public RegistryObjectNotfoundException(String message) {
    super(message);
  }

  public RegistryObjectNotfoundException(String message, Exception e) {
    super(message, e);
  }
}
