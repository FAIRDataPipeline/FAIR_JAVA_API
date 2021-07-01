package uk.ramp.config;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ramp.hash.Hasher;
import uk.ramp.yaml.YamlFactory;
import uk.ramp.yaml.YamlReader;

public class ConfigTester {
  Hasher hasher;
  YamlReader yamlReader;
  Instant openTimestamp;

  @Before
  public void setUp() {
    hasher = new Hasher();
    yamlReader = new YamlFactory().yamlReader();
    openTimestamp = Clock.systemUTC().instant();
  }

  @Test
  public void testConfig() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config.yaml").toURI());
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

  @Ignore // config 3 uses Register; we don't need to be able to parse this.
  @Test
  public void testConfig3() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config3.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }

  @Test
  public void testConfig4() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config4.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }

  @Test
  public void testConfig5() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config5.yaml").toURI());
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    System.out.println(config);
  }
}
