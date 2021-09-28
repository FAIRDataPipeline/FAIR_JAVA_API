package org.fairdatapipeline.config;

/** Something wrong with the config. */
public class ConfigException extends RuntimeException {
  /**
   * Constructor
   *
   * @param message The error message.
   */
  public ConfigException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message The error message.
   * @param e The Exception that caused it.
   */
  public ConfigException(String message, Exception e) {
    super(message, e);
  }
}
