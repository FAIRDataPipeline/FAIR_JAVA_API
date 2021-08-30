package org.fairdatapipeline.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.dataregistry.content.RegistryCode_run;
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
public class Coderun implements AutoCloseable {
  final Config config;
  static final boolean DP_READ = true;
  static final boolean DP_WRITE = false;
  final RestClient restClient;
  private Map<String, Data_product>
      dp_info_map; // TODO: if we can have one and the same DP open for read & write we need 2 maps
  RegistryCode_run registryCode_run;
  Hasher hasher = new Hasher();
  Storage_location script_storage_location;
  Storage_location config_storage_location;
  //private Path scriptPath;
  //private Path configFilePath;
  private Storage_root write_data_store_root;         // i happen to store each local dataregistry stolo
                                                      // with a storage root corresponding to the
                                                      // 'write_data_store' location from config.
  StandardApi stdApi;
  RandomGenerator rng;
  List<Issue> issues;

  /**
   *  Constructor using only configFilePath - scriptPath is read from the config.
   * @param configFilePath the Path to the config file, which must be located in the local data
   *                       store CodeRun folder with timestamp name.
   */
  public Coderun(Path configFilePath) {
    this(configFilePath, null);
  }

  /**
   *  Constructor using both configFilePath and ScriptPath
   * @param configFilePath the Path to the {@link Config config.yaml} file
   * @param scriptPath the Path to the script file
   *
   * both startup files must be located in the local data store CodeRun folder with timestamp name.
   */
  public Coderun(Path configFilePath, Path scriptPath) {
    this(Clock.systemUTC(), configFilePath, scriptPath);
  }

  Coderun(Clock clock, Path configFilePath, Path scriptPath) {
    Instant openTimestamp = clock.instant();
    YamlReader yamlReader = new YamlFactory().yamlReader();
    this.hasher = new Hasher();
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

    // TODO: i don't think write_data_store is optional..
    this.write_data_store_root = new Storage_root(config.run_metadata().write_data_store().orElse(""), restClient);

    if (scriptPath == null) {
      if(config.run_metadata().script_path().isPresent()) {
        scriptPath = Path.of(config.run_metadata().script_path().get());
      }else {
        throw (new IllegalArgumentException("No script path given."));
      }
    }
    this.config_storage_location = new Storage_location(configFilePath, write_data_store_root, this, false);
    this.script_storage_location = new Storage_location(scriptPath, write_data_store_root, this, false);
    prepare_code_run();
    dp_info_map = new HashMap<>();
    this.issues = new ArrayList<>();
  }

  private void prepare_code_run() {
    Author a = new Author(this.restClient);
    List<String> authors = List.of(a.getUrl());
    FileObject config_object = new FileObject(new File_type("yaml", restClient), this.config_storage_location, "Working config.yaml file location in local datastore", authors, this);
    this.registryCode_run = new RegistryCode_run();
    this.registryCode_run.setModel_config(config_object.getUrl());

    FileObject script_object = new FileObject(new File_type("sh", restClient), this.script_storage_location, "Submission script location in local datastore", authors, this);
    this.registryCode_run.setSubmission_script(script_object.getUrl());
    String latest_commit = this.config.run_metadata().latest_commit().orElse("");
    String remote_repo = this.config.run_metadata().remote_repo().orElse("");
    URL remote_repo_url;
    try {
      remote_repo_url = new URL(remote_repo);
    }catch(MalformedURLException e) {
      throw(new IllegalArgumentException("remote repo must be a valid URL; (" + remote_repo + " isn't)"));
    }
    this.registryCode_run.setCode_repo(new Coderepo(latest_commit, remote_repo_url, "Analysis / processing script location", authors, this).getUrl());
    this.registryCode_run.setModel_config(config_object.getUrl());
    this.registryCode_run.setRun_date(LocalDateTime.now()); // or should this be config.openTimestamp??
    this.registryCode_run.setDescription(this.config.run_metadata().description().orElse(""));
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

  void addInput(String input) {
    this.registryCode_run.addInput(input);
  }
  void addOutput(String output) {
    this.registryCode_run.addOutput(output);
  }

  @Override
  public void close() {
    System.out.println("fileApi.close()");
    dp_info_map.entrySet().stream()
        .forEach(
            li -> {
              li.getValue().close();
            });
    if (restClient.post(this.registryCode_run) == null) {
      throw (new IllegalArgumentException(
              "failed to create in registry: " + this.registryCode_run));
    }
    //code_run_session.finish();
    this.register_issues();
  }
}
