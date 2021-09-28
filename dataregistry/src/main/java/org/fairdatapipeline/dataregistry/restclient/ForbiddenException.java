package org.fairdatapipeline.dataregistry.restclient;

/** Exception for HTTP 403 - the token might be wrong? */
public class ForbiddenException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public ForbiddenException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public ForbiddenException(String message, Exception e) {
    super(message, e);
  }
}
