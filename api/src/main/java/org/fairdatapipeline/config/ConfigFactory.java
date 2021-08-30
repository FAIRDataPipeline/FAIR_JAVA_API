package org.fairdatapipeline.config;

import java.nio.file.Path;
import java.time.Instant;
import org.fairdatapipeline.hash.Hasher;
import org.fairdatapipeline.yaml.YamlReader;

public class ConfigFactory {
  public Config config(
      YamlReader yamlReader, Hasher hasher, Instant openTimestamp, Path configFilePath) {
    var config = new ConfigReader(yamlReader, configFilePath).read();
    var freshHash = hasher.fileHash(configFilePath.toString(), openTimestamp);
    // var runId = config.runId().orElse(freshHash);
    return config;
    // .withParentPath(configFilePath.getParent().toString());
  }
}
