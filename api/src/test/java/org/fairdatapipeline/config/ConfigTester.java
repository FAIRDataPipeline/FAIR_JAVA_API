package org.fairdatapipeline.config;

import java.net.URISyntaxException;
import java.nio.file.Path;
import org.fairdatapipeline.yaml.YamlFactory;
import org.fairdatapipeline.yaml.YamlReader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigTester {
  YamlReader yamlReader;

  @BeforeAll
  public void setUp() {
    yamlReader = new YamlFactory().yamlReader();
  }

  @ParameterizedTest
  @ValueSource(strings = {"/config.yaml", "/config1.yaml", "/config2.yaml", "/config-stdapi.yaml"})
  void testConfig(String resourceName) throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource(resourceName).toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, cfilepath);
        });
  }
}
