package org.fairdatapipeline.api;

import java.nio.file.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_root;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.fairdatapipeline.hash.Hasher;
import org.fairdatapipeline.yaml.YamlFactory;
import org.fairdatapipeline.config.Config;
import org.fairdatapipeline.config.ConfigFactory;
import org.fairdatapipeline.yaml.YamlReader;

/**
 * Java implementation of Data Pipeline File API.
 *
 * <p>Users should initialise this library using a try-with-resources block or ensure that .close()
 * is explicitly closed when the required file handles have been accessed.
 *
 * <p>
 *     <b>Usage example</b>
 *     <blockquote><pre>
 *    try (var fileApi = new FileApi(configPath, scriptPath)) {
 *       ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
 *       String dataProduct = "animal/dodo";
 *       String component1 = "example-samples-dodo1";
 *       Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
 *       Object_component_write oc1 = dp.getComponent(component1);
 *       oc1.raise_issue("something is terribly wrong with this component", 10);
 *       oc1.writeSamples(samples);
 *     }
 *     </pre></blockquote>
 *
 *
 */
public class FileApi implements AutoCloseable {
  final Config config;
  static final boolean DP_READ = true;
  static final boolean DP_WRITE = false;
  final RestClient restClient;
  private Map<String, Data_product>
      dp_info_map; // TODO: if we can have one and the same DP open for read & write we need 2 maps
  Code_run_session code_run_session;
  Hasher hasher = new Hasher();
  private Path scriptPath;
  private Path configFilePath;
  private RegistryStorage_root registryStorage_root;
  StandardApi stdApi;
  RandomGenerator rng;
  List<Issue> issues;

  /**
   *  Constructor using only configFilePath - scriptPath is read from the config.
   * @param configFilePath the Path to the config file, which must be located in the local data
   *                       store CodeRun folder with timestamp name.
   */
  public FileApi(Path configFilePath) {
    this(configFilePath, null);
  }

  /**
   *  Constructor using both configFilePath and ScriptPath
   * @param configFilePath the Path to the {@link Config config.yaml} file
   * @param scriptPath the Path to the script file
   *
   * both startup files must be located in the local data store CodeRun folder with timestamp name.
   */
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

  /**
   * Obtain a data product for reading.
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product.
   */
  public Data_product_read get_dp_for_read(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if(dp_info_map.get(dataProduct_name).getClass() != Data_product_read.class) {
        throw(new IllegalArgumentException("You are trying to open the same data product twice in the same coderun, first for write and then for read. Please don't."));
      }
      return (Data_product_read) dp_info_map.get(dataProduct_name);
    }
    Data_product_read dp = new Data_product_read(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing.
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @param extension the file extension representing the file type we will write, e.g. csv or toml
   * @return the data product
   */
  public Data_product_write get_dp_for_write(String dataProduct_name, String extension) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_write.class) {
        throw (new IllegalArgumentException("You are trying to open the same data product twice in the same coderun, first for read and then for write. Please don't."));
      }
      if(dp_info_map.get(dataProduct_name).extension != extension) {
        throw(new IllegalArgumentException("You are trying to open the same data product using two different file types. Please don't."));
      }
      return (Data_product_write) dp_info_map.get(dataProduct_name);
    }
    Data_product_write dp = new Data_product_write(dataProduct_name, this, extension);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing. (gets the extension from config)
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product
   */
  public Data_product_write get_dp_for_write(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_write.class) {
        throw (new IllegalArgumentException("You are trying to open the same data product twice in the same coderun, first for read and then for write. Please don't."));
      }
      return (Data_product_write) dp_info_map.get(dataProduct_name);
    }
    Data_product_write dp = new Data_product_write(dataProduct_name, this);
    // if dp.extension != dp_info_map.get(dataProduct_name).extension) -> FAIL
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * create an Issue that can be linked to a number of object components
   * @param description text description of the issue
   * @param severity integer representing the severity of the issue, larger integer means more severe
   * @return the Issue
   */
  public Issue raise_issue(String description, Integer severity) {
    Issue i = new Issue(description, severity);
    this.issues.add(i);
    return i;
  }

  private void register_issues() {
    this.issues.stream()
        .filter(issue -> !issue.components.isEmpty())
        .forEach(issue -> restClient.post(issue.getRegistryIssue()));
  }

  @Override
  public void close() {
    System.out.println("fileApi.close()");
    dp_info_map.entrySet().stream()
        .forEach(
            li -> {
              li.getValue().close();
            });
    code_run_session.finish();
    this.register_issues();
  }
}
