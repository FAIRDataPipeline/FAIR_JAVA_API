package org.fairdatapipeline.config;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.fairdatapipeline.hash.Hasher;
import org.fairdatapipeline.yaml.YamlFactory;
import org.fairdatapipeline.yaml.YamlReader;

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
    System.out.println("path: " + cfilepath.toString());
    System.out.println("yamlReader: " + yamlReader);
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }

  @Test
  public void testConfig1() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config1.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }

  @Test
  public void testConfig2() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config2.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }

  @Disabled // config 3 uses Register; we don't need to be able to parse this.
  @Test
  public void testConfig3() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config3.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }

  @Test
  public void testConfigStdApi() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config-stdapi.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }
}
