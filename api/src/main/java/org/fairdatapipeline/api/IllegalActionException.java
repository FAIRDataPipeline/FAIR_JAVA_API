package org.fairdatapipeline.api;

public class IllegalActionException extends RuntimeException {
  public IllegalActionException(String message) {
    super(message);
  }

  public IllegalActionException(String message, Exception e) {
    super(message, e);
  }
}
