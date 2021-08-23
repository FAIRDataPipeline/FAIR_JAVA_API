package uk.ramp.api;

import java.nio.file.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import uk.ramp.config.Config;
import uk.ramp.config.ConfigFactory;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.*;
import uk.ramp.hash.Hasher;
import uk.ramp.yaml.YamlFactory;
import uk.ramp.yaml.YamlReader;

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
  final Config config;
  static final boolean DP_READ = true;
  static final boolean DP_WRITE = false;
  final RestClient restClient;
  private Map<String, Data_product_RW>
      dp_info_map; // TODO: if we can have one and the same DP open for read & write we need 2 maps
  Code_run_session code_run_session;
  Hasher hasher = new Hasher();
  private Path scriptPath;
  private Path configFilePath;
  private RegistryStorage_root registryStorage_root;
  StandardApi stdApi;
  RandomGenerator rng;
  List<Issue> issues;

  public FileApi(Path configFilePath) {
    this(configFilePath, null);
  }

  public FileApi(Path configFilePath, Path scriptPath) {
    this(Clock.systemUTC(), configFilePath, scriptPath);
  }

  FileApi(Clock clock, Path configFilePath, Path scriptPath) {
    Instant openTimestamp = clock.instant();
    YamlReader yamlReader = new YamlFactory().yamlReader();
    this.hasher = new Hasher();
    this.scriptPath = scriptPath;
    this.configFilePath = configFilePath;
    this.rng = new RandomDataGenerator().getRandomGenerator();
    this.stdApi = new StandardApi(this.rng);

    this.config =
        new ConfigFactory().config(yamlReader, this.hasher, openTimestamp, configFilePath);
    restClient =
        new RestClient(
            this.config
                .run_metadata()
                .local_data_registry_url()
                .orElse("http://localhost:8000/api/"));
    // this.cleanable = cleaner.register(this, accessLoggerWrapper);

    String Storage_root_path = config.run_metadata().write_data_store().orElse("");
    // TODO: i don't think write_data_store is optional..
    this.registryStorage_root =
        (RegistryStorage_root)
            restClient.getFirst(
                RegistryStorage_root.class, Collections.singletonMap("root", Storage_root_path));
    if (this.registryStorage_root == null) {
      this.registryStorage_root =
          (RegistryStorage_root) restClient.post(new RegistryStorage_root(Storage_root_path));
    }

    if (this.scriptPath == null && config.run_metadata().script_path().isPresent()) {
      this.scriptPath = Path.of(config.run_metadata().script_path().get());
    }
    prepare_code_run_session();
    dp_info_map = new HashMap<>();
    this.issues = new ArrayList<>();
  }

  private void prepare_code_run_session() {
    this.code_run_session =
        new Code_run_session(
            this.restClient,
            this.config,
            this.configFilePath,
            this.scriptPath,
            this.registryStorage_root);
  }

  public Data_product_read get_dp_for_read(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name))
      return (Data_product_read) dp_info_map.get(dataProduct_name);
    Data_product_read dp = new Data_product_read(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  public Data_product_write get_dp_for_write(String dataProduct_name, String extension) {
    if (dp_info_map.containsKey(dataProduct_name))
      return (Data_product_write) dp_info_map.get(dataProduct_name);
    Data_product_write dp = new Data_product_write(dataProduct_name, this, extension);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  public Issue raise_issue(String description, Integer severity) {
    Issue i = new Issue(description, severity);
    this.issues.add(i);
    return i;
  }

  private void register_issues() {
    this.issues.stream().filter(issue -> !issue.components.isEmpty()).forEach(issue -> restClient.post(issue.getRegistryIssue()));
  }

  @Override
  public void close() {
    dp_info_map.entrySet().stream()
        .forEach(
            li -> {
              li.getValue().close();
            });
    code_run_session.finish();
    this.register_issues();
    // cleanable.clean();
  }
}
