package org.fairdatapipeline.dataregistry.restclient;

/** HTTP 406 Not Acceptable: registry rejects our Accept: version header. */
public class RegistryVersionException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public RegistryVersionException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public RegistryVersionException(String message, Exception e) {
    super(message, e);
  }
}
