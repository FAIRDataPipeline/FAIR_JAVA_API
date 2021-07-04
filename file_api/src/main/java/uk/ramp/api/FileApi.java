package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import uk.ramp.config.Config;
import uk.ramp.config.ConfigFactory;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.RestClient;
import uk.ramp.file.CleanableFileChannel;
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
  private Config config;
  private static boolean DP_READ = true;
  private static boolean DP_WRITE = false;
  private final boolean shouldVerifyHash;
  private RestClient restClient;
  private Code_run code_run;

  public FileApi(Path configFilePath) {
    this(Clock.systemUTC(), configFilePath);
  }

  FileApi(Clock clock, Path configFilePath) {
    System.out.println("FileApi; configPath: " + configFilePath);
    System.out.println("Exists? " + configFilePath.toFile().exists());
    var openTimestamp = clock.instant();
    var hasher = new Hasher();
    var yamlReader = new YamlFactory().yamlReader();
    this.config = new ConfigFactory().config(yamlReader, hasher, openTimestamp, configFilePath);
    System.out.print("FileApi.constructor; config: ");
    System.out.println(this.config);
    restClient =
        new RestClient(
            this.config
                .run_metadata()
                .local_data_registry_url()
                .orElse("http://localhost:8000/api/"));
    // this.cleanable = cleaner.register(this, accessLoggerWrapper);
    this.shouldVerifyHash = config.failOnHashMisMatch();
    prepare_code_run();
  }

  private void prepare_code_run() {
    code_run = new Code_run();
    System.out.print("FileApi.prepare_code_run(); config: ");
    System.out.println(this.config);
    code_run.setSubmission_script(this.config.run_metadata().script().orElse(""));
    code_run.setRun_date(LocalDateTime.now()); // or should this be config.openTimestamp??
    code_run.setDescription(this.config.run_metadata().description().orElse(""));
  }

  /**
   * retrieving and storing the dataRegistry details for a dataproduct/component; as requested from
   * the API user, and possibly amended by the config.
   */
  private class dp_info {
    private Namespace namespace;
    private Data_product data_product;
    private FDPObject fdpObject;
    private Object_component object_component;
    private Storage_location storage_location;
    private Storage_root storage_root;
    private Path filePath;

    dp_info(String dataProduct_name, String component_name, boolean readOrWrite) {
      String namespace_name, actual_dataProduct_name = dataProduct_name;
      ImmutableConfigItem configItem;

      if (readOrWrite == DP_READ) {
        namespace_name = config.run_metadata().default_input_namespace().orElse("");
        configItem =
            config.readItems().stream()
                .filter(ci -> ci.data_product().orElse("").equals(dataProduct_name))
                .findFirst()
                .orElse(null);
      } else {
        namespace_name = config.run_metadata().default_output_namespace().orElse("");
        config.writeItems().stream()
            .forEach(
                s -> {
                  System.out.println(s.data_product());
                  System.out.println(
                      "Equals? " + s.data_product().orElse("").equals(dataProduct_name));
                });
        configItem =
            config.writeItems().stream()
                .filter(ci -> ci.data_product().orElse("").equals(dataProduct_name))
                .findFirst()
                .orElse(null);
      }
      if (configItem == null) {
        throw (new IllegalArgumentException(
            "dataProduct " + dataProduct_name + " not found in config"));
      }
      if (configItem.use().isPresent()) {
        if (configItem.use().get().namespace().isPresent()) {
          namespace_name = configItem.use().get().namespace().get();
        }
        if (configItem.use().get().component().isPresent()) {
          component_name = configItem.use().get().component().get();
        }
        if (configItem.use().get().data_product().isPresent()) {
          actual_dataProduct_name = configItem.use().get().data_product().get();
        }
      }
      System.out.println("namespace: " + namespace_name);
      this.namespace =
          (Namespace)
              restClient.getFirst(
                  Namespace.class, Collections.singletonMap("name", namespace_name));
      this.data_product =
          (Data_product)
              restClient.getFirst(
                  Data_product.class, Collections.singletonMap("name", actual_dataProduct_name));
      this.fdpObject = (FDPObject) restClient.get(FDPObject.class, this.data_product.getObject());
      this.storage_location =
          (Storage_location)
              restClient.get(Storage_location.class, this.fdpObject.getStorage_location());
      this.storage_root =
          (Storage_root)
              restClient.get(Storage_root.class, this.storage_location.getStorage_root());
      String actual_component_name = component_name;
      Map<String, String> objcompmap =
          new HashMap<>() {
            {
              put("object", fdpObject.get_id().toString());
              put("name", actual_component_name);
            }
          };
      this.object_component =
          (Object_component) restClient.getFirst(Object_component.class, objcompmap);
      if (this.object_component == null) {
        Object_component oc = new Object_component();
        oc.setName(actual_component_name);
        oc.setObject(fdpObject.getUrl());
        restClient.post(oc);
        this.object_component =
            (Object_component) restClient.getFirst(Object_component.class, objcompmap);
      }
      this.filePath =
          Path.of(this.storage_root.getRoot()).resolve(Path.of(this.storage_location.getPath()));
    }

    public Path getFilePath() {
      return this.filePath;
    }

    public Object_component getComponent() {
      return this.object_component;
    }
  }

  /**
   * retrieving and storing the dataRegistry details for an external object as requested from the
   * API user, and possibly amended by the config.
   */
  private class eo_info {
    private Namespace namespace;
    private External_object external_object;
    private FDPObject fdpObject;
    private Object_component object_component;
    private Storage_location storage_location;
    private Storage_root storage_root;
    private Path filePath;

    eo_info(String config_identifier, boolean readOrWrite) {
      ImmutableConfigItem configItem;
      String doi_or_unique_name;

      if (readOrWrite == DP_READ) {
        configItem =
            config.readItems().stream()
                .filter(ci -> ci.external_object().orElse("") == config_identifier)
                .findFirst()
                .orElse(null);
      } else {
        configItem =
            config.writeItems().stream()
                .filter(ci -> ci.external_object().orElse("") == config_identifier)
                .findFirst()
                .orElse(null);
      }
      if (configItem == null) {
        throw (new IllegalArgumentException("External object not found in config."));
      }
      doi_or_unique_name = configItem.doi_or_unique_name().orElse("");
      if (doi_or_unique_name == "") {
        throw (new IllegalArgumentException(
            "External object must have a doi_or_unique_name in the config."));
      }
      this.external_object =
          (External_object)
              restClient.getFirst(
                  External_object.class,
                  Collections.singletonMap("doi_or_unique_name", doi_or_unique_name));
      this.fdpObject =
          (FDPObject) restClient.get(FDPObject.class, this.external_object.getData_product());
      if (fdpObject.getComponents().size() != 1) {
        throw (new IllegalArgumentException(
            "external object must contain 1 object_component and only 1."));
      }
      this.storage_location =
          (Storage_location)
              restClient.get(Storage_location.class, this.fdpObject.getStorage_location());
      this.storage_root =
          (Storage_root)
              restClient.get(Storage_root.class, this.storage_location.getStorage_root());
      this.object_component =
          (Object_component)
              restClient.get(Object_component.class, fdpObject.getComponents().get(1));
      this.filePath =
          Path.of(this.storage_root.getRoot()).resolve(Path.of(this.storage_location.getPath()));
    }

    public Path getFilePath() {
      return this.filePath;
    }

    public Object_component getComponent() {
      return this.object_component;
    }
  }

  public CleanableFileChannel openForWrite(String dataproduct, String component)
      throws IOException {
    dp_info dp = new dp_info(dataproduct, component, DP_WRITE);
    String outputUrl = dp.getComponent().getUrl();
    System.out.println("openForWrite() outputUrl: " + outputUrl);
    if (code_run.getOutputs().contains(outputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addOutput(outputUrl);
    Runnable onClose = () -> executeOnCloseFileHandleDP(dp);
    System.out.println("openForWrite() dp.getFilePath: " + dp.getFilePath());
    File dir = new File(String.valueOf(dp.getFilePath().getParent()));
    if(!dir.exists()){
      dir.mkdirs();
    }else{
      File file = new File(String.valueOf(dp.getFilePath()));
      if(file.exists()) {
        // what do we do if the file already exists?

      }
    }
    return new CleanableFileChannel(FileChannel.open(dp.getFilePath(), CREATE, WRITE), onClose);
  }

  public CleanableFileChannel openForRead(String dataproduct, String component) throws IOException {
    dp_info dp = new dp_info(dataproduct, component, DP_READ);
    String outputUrl = dp.getComponent().getUrl();
    if (code_run.getOutputs().contains(outputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addOutput(outputUrl);
    return new CleanableFileChannel(FileChannel.open(dp.getFilePath(), READ), () -> {});
  }

  private void executeOnCloseFileHandleDP(dp_info dp) {
    // hash the dp.getFileName() and put this hash into updatedObjects
  }

  private void executeOnCloseFileHandleEO(eo_info eo) {
    // hash the eo.getFileName() and put this hash into updatedObjects
  }

  public Path getFilepathForWrite(String dataproduct, String component) {
    dp_info dp = new dp_info(dataproduct, component, DP_WRITE);
    String outputUrl = dp.getComponent().getUrl();
    if (code_run.getOutputs().contains(outputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addOutput(outputUrl);
    return dp.getFilePath();
  }

  public Path getFilepathForRead(String dataproduct, String component) {
    dp_info dp = new dp_info(dataproduct, component, DP_READ);
    String inputUrl = dp.getComponent().getUrl();
    if (code_run.getInputs().contains(inputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addInput(inputUrl);
    return dp.getFilePath();
  }

  public void closeFile(String filename) {
    // hash this file; put the hash into updatedObjects
    //
  }

  /**
   *
   * @param config_identifier
   * @return
   * @throws IOException
   */
  public CleanableFileChannel readExternalObject(String config_identifier) throws IOException {
    eo_info eo = new eo_info(config_identifier, DP_READ);
    String inputUrl = eo.getComponent().getUrl();
    if (code_run.getInputs().contains(inputUrl)) {
      // ERROR: we've already read from this component
      return null;
    }
    code_run.addInput(inputUrl);
    return new CleanableFileChannel(FileChannel.open(eo.getFilePath(), READ), () -> {});
  }

  public CleanableFileChannel writeExternalObject(String config_identified) throws IOException {
    eo_info eo = new eo_info(config_identified, DP_WRITE);
    String outputUrl = eo.getComponent().getUrl();
    if (code_run.getOutputs().contains(outputUrl)) {
      // error: we've already written to this component
      return null;
    }
    code_run.addOutput(outputUrl);
    Runnable onClose = () -> executeOnCloseFileHandleEO(eo);
    return new CleanableFileChannel(FileChannel.open(eo.getFilePath(), CREATE, WRITE), onClose);
  }

  public Config getConfig() {
    return this.config;
  }

  /** Close the session and write the access log. */
  @Override
  public void close() {
    // cleanable.clean();
  }
}
