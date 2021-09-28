package org.fairdatapipeline.config;

import java.nio.file.Path;
import org.fairdatapipeline.yaml.YamlReader;

/** */
public class ConfigFactory {
  /**
   * @param yamlReader the YamlReader that does the work.
   * @param configFilePath the config.yaml configuration file.
   * @return the immutable parsed configuration file.
   */
  public Config config(YamlReader yamlReader, Path configFilePath) {
    var config = new ConfigReader(yamlReader, configFilePath).read();
    return config;
  }
}
