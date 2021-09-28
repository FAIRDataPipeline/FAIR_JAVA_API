package org.fairdatapipeline.api;

/** Failure to retrieve an expected object from the registry */
public class RegistryObjectNotfoundException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public RegistryObjectNotfoundException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public RegistryObjectNotfoundException(String message, Exception e) {
    super(message, e);
  }
}
