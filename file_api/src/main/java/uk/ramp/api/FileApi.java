package uk.ramp.api;

import java.nio.file.Path;
import java.time.Clock;
import uk.ramp.config.ConfigFactory;
import uk.ramp.dataRegistry.dataRegistry;
import uk.ramp.hash.Hasher;
import uk.ramp.yaml.YamlFactory;

/**
 * Java implementation of Data Pipeline File API.
 *
 * <p>Users should initialise this library using a try-with-resources block or ensure that .close()
 * is explicitly closed when the required file handles have been accessed.
 *
 * <p>As a safety net, .close() is called by a cleaner when the instance of FileApi is being
 * collected by the GC.
 */
public class FileApi implements AutoCloseable {
  // private static final Cleaner cleaner = Cleaner.create(); // safety net for closing
  // private final Cleanable cleanable;
  // private final OverridesApplier overridesApplier;
  private final boolean shouldVerifyHash;

  public FileApi(Path configFilePath) {
    this(Clock.systemUTC(), configFilePath);
  }

  FileApi(Clock clock, Path configFilePath) {
    var openTimestamp = clock.instant();
    var hasher = new Hasher();
    var yamlReader = new YamlFactory().yamlReader();
    var config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, configFilePath);
    var dataregistry = new dataRegistry(config);
    // this.cleanable = cleaner.register(this, accessLoggerWrapper);
    this.shouldVerifyHash = config.failOnHashMisMatch();
  }

  /** Close the session and write the access log. */
  @Override
  public void close() {
    // cleanable.clean();
  }
}
