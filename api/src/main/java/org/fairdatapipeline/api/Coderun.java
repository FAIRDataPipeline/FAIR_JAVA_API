package org.fairdatapipeline.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.fairdatapipeline.config.Config;
import org.fairdatapipeline.config.ConfigException;
import org.fairdatapipeline.config.ConfigFactory;
import org.fairdatapipeline.dataregistry.content.RegistryCode_run;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_root;
import org.fairdatapipeline.dataregistry.restclient.APIURL;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.fairdatapipeline.file.FileReader;
import org.fairdatapipeline.hash.Hasher;
import org.fairdatapipeline.parameters.ParameterDataReader;
import org.fairdatapipeline.parameters.ParameterDataReaderImpl;
import org.fairdatapipeline.parameters.ParameterDataWriter;
import org.fairdatapipeline.parameters.ParameterDataWriterImpl;
import org.fairdatapipeline.toml.TOMLMapper;
import org.fairdatapipeline.toml.TomlReader;
import org.fairdatapipeline.toml.TomlWriter;
import org.fairdatapipeline.yaml.YamlFactory;
import org.fairdatapipeline.yaml.YamlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java implementation of the FAIR Data Pipeline API
 *
 * <p>Users should initialise this library using a try-with-resources block or ensure that .close()
 * is explicitly closed when the required file handles have been accessed.
 *
 * <p><b>Usage example</b>
 *
 * <blockquote>
 *
 * <pre>
 *    try (var coderun = new Coderun(configPath, scriptPath, regToken)) {
 *       ImmutableSamples samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
 *       String dataProduct = "animal/dodo";
 *       String component1 = "example-samples-dodo1";
 *       Data_product_write dp = coderun.get_dp_for_write(dataProduct, "toml");
 *       Object_component_write oc1 = dp.getComponent(component1);
 *       oc1.raise_issue("something is terribly wrong with this component", 10);
 *       oc1.writeSamples(samples);
 *     }
 *     </pre>
 *
 * </blockquote>
 *
 * Or, to write a CSV file, with issues on the DP and on the Code_repo:
 *
 * <blockquote>
 *
 * <pre>
 *    try (var coderun = new Coderun(configPath, scriptPath, regToken)) {
 *       String dataProduct = "human/health";
 *       Data_product_write dp = coderun.get_dp_for_write(dataProduct, "toml");
 *       Object_component_write oc1 = dp.getComponent();
 *       CleanableFileChannel f = oc1.writeFileChannel();
 *       my_analysis_csv_writer(f);
 *       Issue i = coderun.raise_issue("something is terribly wrong with this component", 10);
 *       i.add_components(oc1);
 *       i.add_fileObjects(coderun.getCode_repo());
 *     }
 *     </pre>
 *
 * </blockquote>
 */
public class Coderun implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(Coderun.class);
  final Config config;
  final RestClient restClient;
  private final Map<String, Data_product>
      dp_info_map; // TODO: if we can have one and the same DP open for read & write we need 2 maps
  RegistryCode_run registryCode_run;
  final Hasher hasher = new Hasher();
  Storage_location script_storage_location;
  Storage_location config_storage_location;
  private final Storage_root write_data_store_root;
  final ParameterDataReader parameterDataReader;
  final ParameterDataWriter parameterDataWriter;
  RandomGenerator rng;
  List<Issue> issues;
  Path coderuns_txt;
  FileObject script_object;
  FileObject config_object;
  CodeRepo codeRepo;
  List<APIURL> authors;

  /**
   * Constructor using only configFilePath - scriptPath is read from the config.
   *
   * @param configFilePath the Path to the config file, which must be located in the local data
   *     store CodeRun folder with timestamp name.
   */
  public Coderun(Path configFilePath) {
    this(configFilePath, null);
  }

  /**
   * Constructor using both configFilePath and ScriptPath
   *
   * @param configFilePath the Path to the {@link Config config.yaml} file
   * @param scriptPath the Path to the script file - this may be null if the script path is given in
   *     the config file.
   *     <p>both startup files are usually provided by <a
   *     href="https://fairdatapipeline.github.io/docs/interface/fdp/#fair-run">fair run</a> in the
   *     local data registry jobs/{timestamp} folder.
   */
  public Coderun(Path configFilePath, @Nullable Path scriptPath) {
    this(configFilePath, scriptPath, null);
  }

  /**
   * Constructor specifying configPath, scriptPath, and registryToken.
   *
   * @param configFilePath the Path to the {@link Config config.yaml} file
   * @param scriptPath the Path to the script file - this may be null if the script path is given in
   *     the config file.
   *     <p>both startup files are usually provided by <a
   *     href="https://fairdatapipeline.github.io/docs/interface/fdp/#fair-run">fair run</a> in the
   *     local data registry jobs/{timestamp} folder.
   * @param registryToken the authentication token of the local registry (or null if the token is to
   *     be read from the config or from ~/.fair/registry/token)
   */
  public Coderun(Path configFilePath, @Nullable Path scriptPath, @Nullable String registryToken) {
    YamlReader yamlReader = new YamlFactory().yamlReader();
    if (!new File(configFilePath.toString()).isFile()) {
      throw (new IllegalArgumentException(
          "Coderun -- configFilePath argument must be a Path to a config file."));
    }
    if (scriptPath != null && !new File(scriptPath.toString()).isFile()) {
      throw (new IllegalArgumentException(
          "Coderun -- scriptPath argument must be a Path to a script file."));
    }

    this.coderuns_txt = configFilePath.getParent().resolve("coderuns.txt");
    this.rng = new RandomDataGenerator().getRandomGenerator();
    this.parameterDataReader = new ParameterDataReaderImpl(new TomlReader(new TOMLMapper(rng)));
    this.parameterDataWriter = new ParameterDataWriterImpl(new TomlWriter(new TOMLMapper(rng)));

    this.config = new ConfigFactory().config(yamlReader, configFilePath);
    if (registryToken == null) {
      if (config.run_metadata().token().isPresent())
        registryToken = config.run_metadata().token().get();
      else {
        String filename = "~/.fair/registry/token";
        if (Files.isReadable(Path.of(filename))) {
          registryToken = new FileReader().read(filename);
        } else {
          throw (new IllegalActionException(
              "No registry token given. Giving up. (Token can be given in the config or in the Coderun constructor)"));
        }
      }
    }
    restClient =
        new RestClient(
            this.config
                .run_metadata()
                .local_data_registry_url()
                .orElse("http://localhost:8000/api/"),
            registryToken);

    URI storageRootURI;
    if (config.run_metadata().write_data_store().isPresent()) {
      try {
        storageRootURI = URI.create(config.run_metadata().write_data_store().get());
        if (storageRootURI.getScheme() == null) {
          storageRootURI = Path.of(storageRootURI.toString()).toUri();
          logger.trace(
              "added file:/// scheme to write_data_store from config: {}",
              config.run_metadata().write_data_store().get());
        }
      } catch (Exception e) {
        Path wdsPath = Path.of(config.run_metadata().write_data_store().get());
        storageRootURI = wdsPath.toUri();
        logger.trace("tried to create write_data_store URI after Exception: {}", storageRootURI);
      }
    } else {
      storageRootURI = configFilePath.getParent().getParent().getParent().toUri();
      logger.trace(
          "Using configFilePath.parent.parent.parent as storageRootURI: {}", storageRootURI);
    }
    this.write_data_store_root = new Storage_root(storageRootURI, restClient);

    if (scriptPath == null) {
      if (config.run_metadata().script_path().isPresent())
        scriptPath = Path.of(config.run_metadata().script_path().get());
      else {
        throw (new ConfigException(
            "Coderun() -- Script path must be given either in constructor args or in config."));
      }
    }
    this.config_storage_location =
        new Storage_location(configFilePath, write_data_store_root, this, false);
    this.script_storage_location =
        new Storage_location(scriptPath, write_data_store_root, this, false);
    prepare_code_run();
    dp_info_map = new HashMap<>();
    this.issues = new ArrayList<>();
  }

  RegistryStorage_root getWriteStorage_root() {
    return this.write_data_store_root.registryStorage_root;
  }

  private void prepare_code_run() {
    Author a = new Author(this.restClient);
    this.authors = List.of(a.getUrl());
    this.config_object =
        new FileObject(
            new File_type("yaml", restClient),
            this.config_storage_location,
            "Working config.yaml file location in local datastore",
            this.authors,
            this);
    this.registryCode_run = new RegistryCode_run();
    this.registryCode_run.setModel_config(this.config_object.getUrl());

    this.script_object =
        new FileObject(
            new File_type("sh", restClient),
            this.script_storage_location,
            "Submission script location in local datastore",
            this.authors,
            this);
    this.registryCode_run.setSubmission_script(this.script_object.getUrl());
    String latest_commit = this.config.run_metadata().latest_commit().orElse("");
    String remote_repo = this.config.run_metadata().remote_repo().orElse("");
    URL remote_repo_url;
    try {
      remote_repo_url = new URL(remote_repo);
    } catch (MalformedURLException e) {
      throw (new ConfigException(
          "Remote repo must be a valid URL; (" + remote_repo + " isn't)", e));
    }

    this.codeRepo =
        new CodeRepo(
            latest_commit,
            remote_repo_url,
            "Analysis / processing script location",
            this.authors,
            this);

    this.registryCode_run.setCode_repo(this.codeRepo.getUrl());
    this.registryCode_run.setModel_config(this.config_object.getUrl());
    this.registryCode_run.setRun_date(
        LocalDateTime.now()); // or should this be config.openTimestamp??
    this.registryCode_run.setDescription(this.config.run_metadata().description().orElse(""));
  }

  /**
   * Access the Submission_script; in order to raise a Submission_script issue.
   *
   * @return the FileObject representing the Submission_script.
   *     <p>Usage:
   *     <pre>
   *     coderun.getScript.raise_issue("this is a very bad script", 10);
   * </pre>
   *     OR
   *     <pre>
   *     Issue i = coderun.raise_issue("Seriously bad stuff", 10);
   *     i.add_fileObjects(coderun.getScript(), coderun.getConfig());
   * </pre>
   */
  public FileObject getScript() {
    return this.script_object;
  }

  /**
   * Access the configuration file (config.yaml); in order to raise a Config issue.
   *
   * @return the FileObject representing the config file.
   *     <p>Usage:
   *     <pre>
   *     coderun.getConfig.raise_issue("this is a very bad config file", 10);
   * </pre>
   *     OR
   *     <pre>
   *     Issue i = coderun.raise_issue("Seriously bad stuff", 10);
   *     i.add_fileObjects(coderun.getScript(), coderun.getConfig());
   * </pre>
   */
  public FileObject getConfig() {
    return this.config_object;
  }

  /**
   * Access the code repository in order to raise a code repository issue.
   *
   * @return the FileObject representing the code repository (which is given in the config file)
   *     <p>Usage:
   *     <pre>
   *     coderun.getCode_repo.raise_issue("this contains very bad code", 10);
   * </pre>
   *     OR
   *     <pre>
   *     Issue i = coderun.raise_issue("Seriously bad stuff", 10);
   *     i.add_fileObjects(coderun.getCode_repo(), coderun.getConfig());
   * </pre>
   */
  public FileObject getCode_repo() {
    return this.codeRepo.getFileObject();
  }

  /**
   * Obtain a data product for reading.
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product.
   */
  public Data_product_read_link get_dp_for_read_link(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_read.class) {
        throw (new IllegalActionException(
            "You are trying to open the same data product twice in the same coderun, first for write and then for read. Please don't."));
      }
      return (Data_product_read_link) dp_info_map.get(dataProduct_name);
    }
    Data_product_read_link dp = new Data_product_read_link(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing.
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @param extension the file extension representing the file type we will write, e.g. csv or toml
   * @return the data product
   */
  public Data_product_write_link get_dp_for_write_link(String dataProduct_name, String extension) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_write_link.class) {
        throw (new IllegalActionException(
            "You have already opened Data_product with name '"
                + dataProduct_name
                + "' but it was not a Data_product_write_link."));
      }
      if (!dp_info_map.get(dataProduct_name).extension.equals(extension)) {
        throw (new IllegalActionException(
            "You are trying to open the same data product using two different file types. Please don't."));
      }
      return (Data_product_write_link) dp_info_map.get(dataProduct_name);
    }
    Data_product_write_link dp = new Data_product_write_link(dataProduct_name, this, extension);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing. (gets the extension from config)
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product
   */
  public Data_product_write_link get_dp_for_write_link(String dataProduct_name) {
    return this.get_dp_for_write_link(dataProduct_name, null);
  }

  /**
   * Obtain a data product for writing. (gets the extension from config)
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product
   */
  public Data_product_write_toml get_dp_for_write_toml(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_write_toml.class) {
        throw (new IllegalActionException(
            "You have already opened Data_product with name '"
                + dataProduct_name
                + "' but it was not a Data_product_write_json."));
      }
      return (Data_product_write_toml) dp_info_map.get(dataProduct_name);
    }
    Data_product_write_toml dp = new Data_product_write_toml(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing. (gets the extension from config)
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product
   */
  public Data_product_read_nc get_dp_for_read_nc(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_read_nc.class) {
        throw (new IllegalActionException(
            "You have already opened Data_product with name '"
                + dataProduct_name
                + "' but it was not a Data_product_read_nc."));
      }
      return (Data_product_read_nc) dp_info_map.get(dataProduct_name);
    }
    Data_product_read_nc dp = new Data_product_read_nc(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing. (gets the extension from config)
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product
   */
  public Data_product_read_toml get_dp_for_read_toml(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_read_toml.class) {
        throw (new IllegalActionException(
            "You have already opened Data_product with name '"
                + dataProduct_name
                + "' but it was not a Data_product_write_toml."));
      }
      return (Data_product_read_toml) dp_info_map.get(dataProduct_name);
    }
    Data_product_read_toml dp = new Data_product_read_toml(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * Obtain a data product for writing. (gets the extension from config)
   *
   * @param dataProduct_name the name of the dataProduct to obtain.
   * @return the data product
   */
  public Data_product_write_nc get_dp_for_write_nc(String dataProduct_name) {
    if (dp_info_map.containsKey(dataProduct_name)) {
      // I could of course refuse to serve up the same DP twice, but let's be friendly.
      if (dp_info_map.get(dataProduct_name).getClass() != Data_product_write_nc.class) {
        throw (new IllegalActionException(
            "You have already opened Data_product with name '"
                + dataProduct_name
                + "' but it was not a Data_product_write_json."));
      }
      return (Data_product_write_nc) dp_info_map.get(dataProduct_name);
    }
    Data_product_write_nc dp = new Data_product_write_nc(dataProduct_name, this);
    dp_info_map.put(dataProduct_name, dp);
    return dp;
  }

  /**
   * create an Issue that can be linked to a number of {@link Object_component object components}
   * and/or {@link FileObject fileObjects}.
   *
   * @param description text description of the issue
   * @param severity integer representing the severity of the issue, larger integer means more
   *     severe
   * @return the Issue
   */
  public Issue raise_issue(String description, Integer severity) {
    Issue i = new Issue(description, severity);
    this.issues.add(i);
    return i;
  }

  private void register_issues() {
    this.issues.stream()
        .filter(
            issue ->
                !(issue.components.isEmpty()
                    && issue.registryIssue.getComponent_issues().isEmpty()))
        .forEach(issue -> restClient.post(issue.getRegistryIssue()));
  }

  void addInput(APIURL input) {
    this.registryCode_run.addInput(input);
  }

  void addOutput(APIURL output) {
    this.registryCode_run.addOutput(output);
  }

  void append_code_run_uuid(String uuid) {
    try (FileWriter fw = new FileWriter(this.coderuns_txt.toString(), true)) {
      fw.write(uuid + "\n");
    } catch (IOException e) {
      logger.error("IOException: append_code_run_uuid() failed.", e);
    }
  }

  /**
   * Finalize and register the coderun. (this gets called automatically when using Coderun in a
   * try-with-resources block)
   */
  @Override
  public void close() {
    dp_info_map.forEach((key, value) -> value.close());
    RegistryCode_run coderun = (RegistryCode_run) restClient.post(this.registryCode_run);
    if (coderun == null) {
      throw (new RegistryException(
          "Failed to create Code_run in registry: " + this.registryCode_run));
    }
    this.register_issues();
    this.append_code_run_uuid(coderun.getUuid());
  }
}
