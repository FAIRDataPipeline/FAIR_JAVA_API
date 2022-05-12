package org.fairdatapipeline.api;

/** Failure to retrieve an expected object from the registry. */
public class RegistryObjectNotFoundException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public RegistryObjectNotFoundException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public RegistryObjectNotFoundException(String message, Exception e) {
    super(message, e);
  }
}
