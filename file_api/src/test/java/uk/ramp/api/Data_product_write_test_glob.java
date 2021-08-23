package uk.ramp.api;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.ramp.config.ConfigFactory;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.hash.Hasher;
import uk.ramp.yaml.YamlFactory;
import uk.ramp.yaml.YamlReader;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Data_product_write_test_glob {
  Hasher hasher;
  YamlReader yamlReader;
  Instant openTimestamp;

  @BeforeAll
  public void setUp() {
    hasher = new Hasher();
    yamlReader = new YamlFactory().yamlReader();
    openTimestamp = Clock.systemUTC().instant();
  }

  public boolean globDPmatches(String pattern, String dataProduct_name) {
    if (pattern.endsWith("/*")) {
      return dataProduct_name.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return pattern.equals(dataProduct_name);
  }

  @Test
  public void testGlob() throws URISyntaxException {
    Path cfilepath = Path.of(getClass().getResource("/config-stdapi.yaml").toURI());
    System.out.println("path: " + cfilepath.toString());
    System.out.println("yamlReader: " + yamlReader);
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, cfilepath);
    String dataProduct_name = "animal/dog";
    // Path dataProduct_path = Paths.get(dataProduct_name);
    ImmutableConfigItem configItem =
        config.writeItems().stream()
            .filter(ci -> globDPmatches(ci.data_product(), dataProduct_name))
            .findFirst()
            .orElse(null);
    if (configItem != null) System.out.println(configItem.data_product());
    else System.out.println("got null");
  }
}
