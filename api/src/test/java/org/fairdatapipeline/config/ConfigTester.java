package org.fairdatapipeline.config;

import java.net.URISyntaxException;
import java.nio.file.Path;
import org.fairdatapipeline.yaml.YamlFactory;
import org.fairdatapipeline.yaml.YamlReader;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigTester {
  YamlReader yamlReader;

  @BeforeAll
  public void setUp() {
    yamlReader = new YamlFactory().yamlReader();
  }

  @Test
  public void testConfig() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, cfilepath);
        });
  }

  @Test
  public void testConfig1() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config1.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, cfilepath);
        });
  }

  @Test
  public void testConfig2() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config2.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, cfilepath);
        });
  }

  @Disabled // config 3 uses Register; we don't need to be able to parse this.
  @Test
  public void testConfig3() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config3.yaml").toURI());
    new ConfigFactory().config(yamlReader, cfilepath);
  }

  @Test
  public void testConfigStdApi() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config-stdapi.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, cfilepath);
        });
  }
}
