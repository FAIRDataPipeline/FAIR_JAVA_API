package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;

import com.vdurmont.semver4j.Semver;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import uk.ramp.config.Config;
import uk.ramp.config.ConfigFactory;
import uk.ramp.config.ImmutableConfig;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.*;
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
  private final Config config;
  private static final boolean DP_READ = true;
  private static final boolean DP_WRITE = false;
  private final boolean shouldVerifyHash;
  private final RestClient restClient;
  private Code_run code_run;
  private Map<String, dp_info> dp_info_map;
  private final String DEFAULT_VERSION_INCREMENT = "${{CLI.PATCH}}";

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
    dp_info_map = new HashMap<String, dp_info>();
  }

  private class Code_run_objects_to_create {
    Code_run code_run;
    FDPObject[] fdpObjects;
    Data_product[] data_products;
    RestClient restClient;
    ImmutableConfig config;

    Code_run_objects_to_create(RestClient restClient, ImmutableConfig config){
      this.config = config;
      this.restClient = restClient;
      this.code_run = new Code_run();
      System.out.print("FileApi.prepare_code_run(); config: ");
      System.out.println(this.config);
      code_run.setSubmission_script(this.config.run_metadata().script().orElse(""));
      code_run.setRun_date(LocalDateTime.now()); // or should this be config.openTimestamp??
      code_run.setDescription(this.config.run_metadata().description().orElse(""));
    }

  }

  private class dp_component {
    dp_info dp;
    String component_name;
    Object_component object_component;

    dp_component(dp_info dp, String component_name) {
      this.dp = dp;
      this.component_name = component_name;
      Map<String, String> objcompmap =
          new HashMap<>() {
            {
              put("object", dp.getFdpObject().get_id().toString());
              put("name", component_name);
            }
          };
      this.object_component =
          (Object_component) restClient.getFirst(Object_component.class, objcompmap);
      if (this.object_component == null) {
        Object_component oc = new Object_component();
        oc.setName(component_name);
        oc.setObject(dp.getFdpObject().get_id().toString());
        restClient.post(oc);
        this.object_component =
            (Object_component) restClient.getFirst(Object_component.class, objcompmap);
      }
    }

    Object_component getObject_component() {
      return this.object_component;
    }
  }

  /**
   * retrieving and storing the dataRegistry details for a dataproduct; as requested from the API
   * user, and possibly amended by the config.
   */
  private class dp_info {
    private Namespace namespace;
    private Data_product data_product;
    private FDPObject fdpObject;
    private Storage_location storage_location;
    private Storage_root storage_root;
    private Path filePath;
    private CleanableFileChannel filechannel;
    private StandardOpenOption read_or_write;

    dp_info(String dataProduct_name, boolean readOrWrite) {
      String version = null, namespace_name, actual_dataProduct_name = dataProduct_name;
      ImmutableConfigItem configItem;

      if (readOrWrite == DP_READ) {
        this.read_or_write = READ;
        namespace_name = config.run_metadata().default_input_namespace().orElse("");
        configItem =
            config.readItems().stream()
                .filter(ci -> ci.data_product().orElse("").equals(dataProduct_name))
                .findFirst()
                .orElse(null);
      } else {
        this.read_or_write = WRITE;
        namespace_name = config.run_metadata().default_output_namespace().orElse("");
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
        if (configItem.use().get().data_product().isPresent()) {
          actual_dataProduct_name = configItem.use().get().data_product().get();
        }
        if (configItem.use().get().version().isPresent()) {
          version = configItem.use().get().version().get();
        }
      }
      System.out.println("namespace: " + namespace_name);
      this.namespace =
          (Namespace)
              restClient.getFirst(
                  Namespace.class, Collections.singletonMap("name", namespace_name));
      Map<String, String> dp_map =
          Map.of("name", actual_dataProduct_name, "namespace", this.namespace.get_id().toString());
      if (readOrWrite == DP_READ) {
        // we either read a specific VERSION or the latest.
        if (version == null) {
          this.data_product = (Data_product) restClient.getLatestDataProduct(dp_map);
          // TODO and we (for now) assume that this exists and succeeds..
        } else {
          Map<String, String> dp_map_with_version = new HashMap<>(dp_map);
          dp_map_with_version.put("version", version);
          this.data_product =
              (Data_product) restClient.getFirst(Data_product.class, dp_map_with_version);
          // TODO and we (for now) assume that this exists and succeeds..
        }
      } else {
        // we either write a specific VERSION or the latest + increment
        Semver create_version, found_version;
        if(version == null) version = DEFAULT_VERSION_INCREMENT;
        if (version.startsWith("${{CLI.")) {
          Data_product lpd = (Data_product) restClient.getLatestDataProduct(dp_map);
          if (lpd == null) {
            found_version = new Semver("0.0.0");
          } else {
            found_version = new Semver(lpd.getVersion());
          }
          switch(version) {
            case "${{CLI.MAJOR}}":
              create_version = found_version.nextMajor();
              break;
            case "${{CLI.MINOR}}":
              create_version = found_version.nextMinor();
              break;
            case "${{CLI.PATCH}}":
              create_version = found_version.nextPatch();
              break;
            default:
              throw(new IllegalArgumentException("CLI.xxx version should be MAJOR, MINOR, or PATCH; not: " + version));
          }

          // CREATION OF STOLO/OBJ/DP happens after the code_run as we store the hash of the file with the stolo


          //Map<String, String> dp_map_with_version = new HashMap<>(dp_map);
          //dp_map_with_version.put("version", version);
          /*Storage_location stolo_to_post = new Storage_location();
          stolo_to_post.set
          FDPObject object_to_post = new FDPObject();
          object_to_post.setStorage_location();
          Data_product dp_to_post = new Data_product();
          dp_to_post.setVersion(create_version.toString());
          dp_to_post.setNamespace(this.namespace.getUrl());
          dp_to_post.setName(actual_dataProduct_name);
          dp_to_post.setObject();
          restClient.post()
          this.data_product =
                  (Data_product) restClient.getFirst(Data_product.class, dp_map_with_version);*/
        }
      }
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
      this.filePath =
          Path.of(this.storage_root.getRoot()).resolve(Path.of(this.storage_location.getPath()));
    }

    public CleanableFileChannel getFilechannel() throws IOException {
      if (this.filechannel == null) {
        Runnable onClose = () -> executeOnCloseFileHandleDP(this);
        // try {
        this.filechannel =
            new CleanableFileChannel(
                FileChannel.open(this.getFilePath(), CREATE, this.read_or_write), onClose);
        // } catch (IOException e) {
        //  throw (new IllegalArgumentException("Failed to create file " + this.getFilePath()));
        // }
      }
      return this.filechannel;
    }

    public FDPObject getFdpObject() {
      return this.fdpObject;
    }

    public Data_product getData_product() {
      return this.data_product;
    }

    public Path getFilePath() {
      return this.filePath;
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

  public CleanableFileChannel openForWrite(String dataproduct, String component_name)
      throws IOException {
    dp_info dp = new dp_info(dataproduct, DP_WRITE);
    dp_component dc = new dp_component(dp, component_name);

    String componentUrl = dc.getObject_component().getUrl();
    String DP_Url = dp.getData_product().getUrl();
    System.out.println("openForWrite() componentUrl: " + componentUrl);
    if (code_run.getOutputs().contains(componentUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addOutput(componentUrl);
    if (!this.dp_info_map.containsKey(dataproduct)) {
      // only open the file if we haven't already opened (and stored in dp_file_map) previously
      System.out.println("openForWrite() dp.getFilePath: " + dp.getFilePath());
      File dir = new File(String.valueOf(dp.getFilePath().getParent()));
      if (!dir.exists()) {
        dir.mkdirs();
      }
      this.dp_info_map.put(dataproduct, dp);
    }
    return this.dp_info_map.get(dataproduct).getFilechannel();
  }

  public CleanableFileChannel openForRead(String dataproduct, String component) throws IOException {
    /*dp_info dp = new dp_info(dataproduct, component, DP_READ);
    String outputUrl = dp.getComponent().getUrl();
    if (code_run.getOutputs().contains(outputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addOutput(outputUrl);
    return new CleanableFileChannel(FileChannel.open(dp.getFilePath(), READ), () -> {});*/
    return null;
  }

  private void executeOnCloseFileHandleDP(dp_info dp) {
    // hash the dp.getFileName() and put this hash into updatedObjects
  }

  private void executeOnCloseFileHandleEO(eo_info eo) {
    // hash the eo.getFileName() and put this hash into updatedObjects
  }

  public Path getFilepathForWrite(String dataproduct, String component) {
    dp_info dp = new dp_info(dataproduct, DP_WRITE);
    /*String outputUrl = dp.getComponent().getUrl();
    if (code_run.getOutputs().contains(outputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addOutput(outputUrl);
    return dp.getFilePath();*/
    return null;
  }

  public Path getFilepathForRead(String dataproduct, String component) {
    dp_info dp = new dp_info(dataproduct, DP_READ);
    /*String inputUrl = dp.getComponent().getUrl();
    if (code_run.getInputs().contains(inputUrl)) {
      // ERROR: we've already written to this component
      return null;
    }
    code_run.addInput(inputUrl);
    return dp.getFilePath();*/
    return null;
  }

  public void closeFile(String filename) {
    // hash this file; put the hash into updatedObjects
    //
  }

  /**
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
