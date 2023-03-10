package org.fairdatapipeline.netcdf;

/** The consumer is probably trying to do something they aren't supposed to be doing. */
public class NetcdfComponentWrongTypeException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public NetcdfComponentWrongTypeException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public NetcdfComponentWrongTypeException(String message, Exception e) {
    super(message, e);
  }
}
