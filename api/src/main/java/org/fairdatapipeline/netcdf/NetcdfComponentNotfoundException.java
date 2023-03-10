package org.fairdatapipeline.netcdf;

/** The consumer is probably trying to do something they aren't supposed to be doing. */
public class NetcdfComponentNotfoundException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public NetcdfComponentNotfoundException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public NetcdfComponentNotfoundException(String message, Exception e) {
    super(message, e);
  }
}
