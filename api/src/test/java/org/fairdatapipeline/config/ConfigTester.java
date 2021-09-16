package org.fairdatapipeline.config;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import org.fairdatapipeline.hash.Hasher;
import org.fairdatapipeline.yaml.YamlFactory;
import org.fairdatapipeline.yaml.YamlReader;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigTester {
  Hasher hasher;
  YamlReader yamlReader;
  Instant openTimestamp;

  @BeforeAll
  public void setUp() {
    hasher = new Hasher();
    yamlReader = new YamlFactory().yamlReader();
    openTimestamp = Clock.systemUTC().instant();
  }

  @Test
  public void testConfig() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
        });
  }

  @Test
  public void testConfig1() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config1.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
        });
  }

  @Test
  public void testConfig2() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config2.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
        });
  }

  @Disabled // config 3 uses Register; we don't need to be able to parse this.
  @Test
  public void testConfig3() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config3.yaml").toURI());
    new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
  }

  @Test
  public void testConfigStdApi() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config-stdapi.yaml").toURI());
    Assertions.assertDoesNotThrow(
        () -> {
          new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
        });
  }
}
