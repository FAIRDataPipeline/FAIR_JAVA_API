package org.fairdatapipeline.api;

/** Failure to create an Object in the registry. */
public class RegistryException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public RegistryException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public RegistryException(String message, Exception e) {
    super(message, e);
  }
}
