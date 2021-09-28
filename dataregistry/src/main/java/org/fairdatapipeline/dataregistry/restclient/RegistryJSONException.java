package org.fairdatapipeline.dataregistry.restclient;

/** Exception for JSON problems -- might indicate incompatible version of the Registry. */
public class RegistryJSONException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public RegistryJSONException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public RegistryJSONException(String message, Exception e) {
    super(message, e);
  }
}
