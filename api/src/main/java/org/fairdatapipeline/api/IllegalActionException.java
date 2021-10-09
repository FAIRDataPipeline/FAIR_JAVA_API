package org.fairdatapipeline.api;

/** The consumer is probably trying to do something they aren't supposed to be doing. */
public class IllegalActionException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public IllegalActionException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public IllegalActionException(String message, Exception e) {
    super(message, e);
  }
}
