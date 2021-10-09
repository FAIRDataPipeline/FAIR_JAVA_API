package org.fairdatapipeline.dataregistry.restclient;

/** General propagated ProcessingException from jakarta. */
public class RestClientException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public RestClientException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public RestClientException(String message, Exception e) {
    super(message, e);
  }
}
