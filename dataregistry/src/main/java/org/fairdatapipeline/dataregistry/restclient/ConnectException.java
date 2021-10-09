package org.fairdatapipeline.dataregistry.restclient;

/** Failing to connect to the Registry. */
public class ConnectException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public ConnectException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public ConnectException(String message, Exception e) {
    super(message, e);
  }
}
