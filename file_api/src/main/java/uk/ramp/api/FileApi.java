package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import uk.ramp.config.Config;
import uk.ramp.config.ConfigFactory;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.*;
import uk.ramp.file.CleanableFileChannel;
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
  private final Config config;
  private static final boolean DP_READ = true;
  private static final boolean DP_WRITE = false;
  private final boolean shouldVerifyHash;
  private final RestClient restClient;
  private Map<String, dp_info> dp_info_map;
  private final String DEFAULT_VERSION_INCREMENT = "${{CLI.PATCH}}";
  private Code_run_session code_run_session;
  private Hasher hasher = new Hasher();
  private Path scriptPath;
  private Path configFilePath;
  private Storage_root storage_root;

  public FileApi(Path configFilePath) {
    this(configFilePath, null);
  }

  public FileApi(Path configFilePath, Path scriptPath) {
    this(Clock.systemUTC(), configFilePath, scriptPath);
  }

  FileApi(Clock clock, Path configFilePath, Path scriptPath) {
    System.out.println("FileApi; configPath: " + configFilePath);
    System.out.println("Exists? " + configFilePath.toFile().exists());
    Instant openTimestamp = clock.instant();
    YamlReader yamlReader = new YamlFactory().yamlReader();
    this.hasher = new Hasher();
    this.scriptPath = scriptPath;
    this.configFilePath = configFilePath;

    this.config =
        new ConfigFactory().config(yamlReader, this.hasher, openTimestamp, configFilePath);
    System.out.print("FileApi.constructor; config: ");
    System.out.println(this.config);
    restClient =
        new RestClient(
            this.config
                .run_metadata()
                .local_data_registry_url()
                .orElse("http://localhost:8000/api/"));
    // this.cleanable = cleaner.register(this, accessLoggerWrapper);

    String Storage_root_path = config.run_metadata().write_data_store().orElse("");
    // TODO: i don't think write_data_store is optional..
    this.storage_root =
        (Storage_root)
            restClient.getFirst(
                Storage_root.class, Collections.singletonMap("root", Storage_root_path));
    if (this.storage_root == null) {
      System.out.println("creating new storage root: " + Storage_root_path);
      this.storage_root = (Storage_root) restClient.post(new Storage_root(Storage_root_path));
    } else {
      System.out.println("found existing storage root: " + this.storage_root.get_id());
    }

    this.shouldVerifyHash = config.failOnHashMisMatch();
    if (this.scriptPath == null && config.run_metadata().script_path().isPresent()) {
      this.scriptPath = Path.of(config.run_metadata().script_path().get());
    }
    System.out.println("before prepare code run session");
    prepare_code_run_session();
    System.out.println("after prepare code run session");
    dp_info_map = new HashMap<String, dp_info>();
  }

  private void prepare_code_run_session() {
    this.code_run_session =
        new Code_run_session(
            this.restClient, this.config, this.configFilePath, this.scriptPath, this.storage_root);
  }

  private class dp_component {
    dp_info dp;
    String component_name;
    Object_component object_component;

    dp_component(dp_info dp, String component_name) {
      this(dp, component_name, false);
    }

    dp_component(dp_info dp, String component_name, boolean whole_object) {
      this.dp = dp;
      this.component_name = component_name;
      String my_obj_id = dp.getFdpObject().get_id().toString();
      System.out.println("id: " + my_obj_id);
      Map<String, String> objcompmap =
          new HashMap<>() {
            {
              put("object", my_obj_id);
              put("name", component_name);
            }
          };
      // if whole_object is true, we could ignore component_name (but we don't have to?)
      this.object_component =
          (Object_component) restClient.getFirst(Object_component.class, objcompmap);
      if (this.object_component == null) {
        throw (new IllegalArgumentException(
            "Object Component '"
                + component_name
                + "' for FDPObj "
                + dp.getFdpObject().get_id().toString()
                + " not found in registry."));
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
    private String givenDataProduct_name;
    private String actualDataProduct_name;
    private boolean is_hashed = false;

    dp_info(String dataProduct_name, boolean readOrWrite) {
      this(dataProduct_name, readOrWrite, null);
    }

    dp_info(String dataProduct_name, boolean readOrWrite, String extension) {
      System.out.println("dp_info() constructor");
      String version = null, namespace_name;
      ImmutableConfigItem configItem;

      this.givenDataProduct_name = dataProduct_name;
      this.actualDataProduct_name = dataProduct_name;

      if (readOrWrite == DP_READ) {
        this.read_or_write = READ;
        namespace_name = config.run_metadata().default_input_namespace().orElse("");
        configItem =
            config.readItems().stream()
                .filter(ci -> ci.data_product().equals(dataProduct_name))
                .findFirst()
                .orElse(null);
      } else {
        this.read_or_write = WRITE;
        namespace_name = config.run_metadata().default_output_namespace().orElse("");
        configItem =
            config.writeItems().stream()
                .filter(ci -> ci.data_product().equals(dataProduct_name))
                .findFirst()
                .orElse(null);
        if (configItem == null) {
          // for WRITING dp's; we allow * globbing if there is no exact match we look for a * match
          Path dataProduct_path = Paths.get(dataProduct_name);
          configItem =
              config.writeItems().stream()
                  .filter(
                      ci ->
                          FileSystems.getDefault()
                              .getPathMatcher(ci.data_product())
                              .matches(dataProduct_path))
                  .findFirst()
                  .orElse(null);
        }
      }
      if (configItem == null) {
        throw (new IllegalArgumentException(
            "dataProduct " + dataProduct_name + " not found in config"));
      }
      System.out.println("found configItem");
      if (configItem.use().namespace().isPresent()) {
        namespace_name = configItem.use().namespace().get();
      }
      if (configItem.use().data_product().isPresent()) {
        this.actualDataProduct_name = configItem.use().data_product().get();
      }
      version = configItem.use().version();
      this.namespace =
          (Namespace)
              restClient.getFirst(
                  Namespace.class, Collections.singletonMap("name", namespace_name));
      if (this.namespace == null) {
        this.namespace = (Namespace) restClient.post(new Namespace(namespace_name));
        if (namespace == null) {
          throw (new IllegalArgumentException(
              "failed to create namespace '" + namespace_name + "' in the registry."));
        }
        System.out.println("created the namespace");
      }
      System.out.println("found namespace");
      System.out.println(this.namespace.getName());
      System.out.println(this.namespace.getUrl());
      System.out.println(this.namespace.get_id());
      System.out.println(this.namespace.get_id().toString());
      System.out.println(version);
      System.out.println("that's it");
      Map<String, String> dp_map =
          Map.of(
              "name",
              this.actualDataProduct_name,
              "namespace",
              this.namespace.get_id().toString(),
              "version",
              version);
      System.out.println("searching dataProduct");
      this.data_product = (Data_product) restClient.getFirst(Data_product.class, dp_map);
      if (this.data_product == null && this.read_or_write == READ) {
        throw (new IllegalArgumentException(
            "Trying to read from non-existing data_product "
                + this.actualDataProduct_name
                + "; NS "
                + this.namespace.getName()
                + "; version "
                + version));
      } else if (this.read_or_write == WRITE) {
        if (this.data_product != null) {
          throw (new IllegalArgumentException(
              "Trying to write to existing data_product "
                  + this.actualDataProduct_name
                  + "; NS "
                  + this.namespace.getName()
                  + "; version "
                  + version));
        } else {
          String Storage_root_path = config.run_metadata().write_data_store().orElse("");
          this.storage_root =
              (Storage_root)
                  restClient.getFirst(
                      Storage_root.class, Collections.singletonMap("root", Storage_root_path));
          if (this.storage_root == null) {
            this.storage_root = (Storage_root) restClient.post(new Storage_root(Storage_root_path));
          }
          this.storage_location = new Storage_location();
          this.storage_location.setStorage_root(this.storage_root.getUrl());
          if (extension == null) {
            extension = configItem.file_type().orElse("");
          }
          String filename = version + "." + extension;
          Path my_stolo_path =
              Paths.get(namespace_name).resolve(this.actualDataProduct_name).resolve(filename);
          this.filePath = Paths.get(Storage_root_path).resolve(my_stolo_path);
          this.storage_location.setPath(my_stolo_path.toString());
          this.fdpObject = new FDPObject();
          this.fdpObject.setDescription(configItem.description().orElse(""));
          // o.setAuthors();
          this.data_product = new Data_product();
          this.data_product.setName(this.actualDataProduct_name);
          this.data_product.setNamespace(this.namespace.getUrl());
          this.data_product.setVersion(version);
          code_run_session.addstuff(
              this.givenDataProduct_name,
              this.storage_location,
              this.fdpObject,
              this.data_product);
          code_run_session.setFilePath(this.givenDataProduct_name, this.filePath);
        }
      } else {
        // read_or_write is READ and data product exists
        System.out.println("READ and DP exists");
        this.fdpObject = (FDPObject) restClient.get(FDPObject.class, this.data_product.getObject());
        if (this.fdpObject == null) {
          throw (new IllegalArgumentException(
              "couldn't retrieve the fdpObject for this READ dp "
                  + this.givenDataProduct_name
                  + " ("
                  + this.actualDataProduct_name
                  + ")"));
        }
        System.out.println("retrieved the fdpObj for this DP");
        this.storage_location =
            (Storage_location)
                restClient.get(Storage_location.class, this.fdpObject.getStorage_location());
        if (this.storage_location == null) {
          throw (new IllegalArgumentException(
              "couldn't retrieve the StorageLocation for this READ dp "
                  + this.givenDataProduct_name
                  + " ("
                  + this.actualDataProduct_name
                  + ")"));
        }
        this.storage_root =
            (Storage_root)
                restClient.get(Storage_root.class, this.storage_location.getStorage_root());
        if (this.storage_root == null) {
          throw (new IllegalArgumentException(
              "couldn't retrieve the StorageRoot for this READ dp "
                  + this.givenDataProduct_name
                  + " ("
                  + this.actualDataProduct_name
                  + ")"));
        }
        this.filePath =
            Path.of(this.storage_root.getRoot()).resolve(Path.of(this.storage_location.getPath()));
        System.out.println("setting filepath on code_run_session to: " + this.filePath.toString());
        System.out.println("data_product_to_create for " + this.givenDataProduct_name + ": " + code_run_session.data_products_to_create.get(this.givenDataProduct_name));
        code_run_session.setFilePath(this.givenDataProduct_name, this.filePath);// or not?
      }
    }

    private void executeOnCloseFileHandleDP() {
      System.out.println("executeOnCloseFileHandleDP() .. " + this.toString());
      this.do_hash();
    }

    private void do_hash() {
      if (this.is_hashed) {
        System.out.println("it was already hashed!");
        return;
      }
      System.out.println("It wasn't hashed yet");
      if (this.read_or_write == READ) return;
      String hash =hasher.fileHash(this.getFilePath().toString());
      System.out.println("setting hash to: " + hash);
      this.storage_location.setHash(hash);

      System.out.println("hash from stolo: " + this.storage_location.getHash());
      System.out.println("stolo from code_run: " + code_run_session.getStolo(givenDataProduct_name));
      System.out.println("hash from code_run stolo: " + code_run_session.getStolo(this.givenDataProduct_name).getHash());

      /*Map<String, String> find_identical =
          Map.of(
              "storage_root",
              FDP_RootObject.get_id(this.storage_location.getStorage_root()).toString(),
              "hash",
              hash,
              "public",
              this.storage_location.isIs_public() ? "true" : "false");
      Storage_location sl =
          (Storage_location) restClient.getFirst(Storage_location.class, find_identical);
      if (sl != null) {
        // we've found an existing stolo with matching hash.. delete this one.
        this.getFilePath().toFile().delete();
        this.storage_location = sl;
        code_run_session.setStorageLocation(this.givenDataProduct_name, sl);
      } else {
        this.storage_location.setHash(hash);
      }*/
      this.is_hashed = true;
      // hash the dp.getFileName() and put this hash into updatedObjects
    }

    public CleanableFileChannel getFilechannel() throws IOException {
      Runnable onClose = this::executeOnCloseFileHandleDP;
      if (this.filechannel == null) {

        // try {
        this.filechannel =
            new CleanableFileChannel(
                FileChannel.open(this.getFilePath(), CREATE_NEW, this.read_or_write), onClose);
        // } catch (IOException e) {
        //  throw (new IllegalArgumentException("Failed to create file " + this.getFilePath()));
        // }
      }else{
        if(!this.filechannel.isOpen()){
          System.out.println("re-opening the filechannel");
          this.filechannel =
                  new CleanableFileChannel(
                          FileChannel.open(this.getFilePath(), APPEND, this.read_or_write), onClose);
        }
      }
      this.is_hashed = false;
      return this.filechannel;
    }

    void ensureHashed() {
      System.out.println("ensureHashed()");
      this.closeFileChannel();
      System.out.println("closed the fileChannel");
      if (!this.is_hashed) {
        System.out.println("do_hash()");
        this.do_hash();
      }else{
          System.out.println("was already hashed");
      }
    }

    void closeFileChannel() {
      if (this.filechannel != null) {
        System.out.println("closing the filechannel for dp " + this.filePath);
        this.filechannel.close();
        this.filechannel = null;
      }
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

  /*public CleanableFileChannel openForWrite(String dataProduct_name) throws IOException {
    return openForWrite(dataProduct_name, null, null);
  }*/

  /*
   * openForWrite - used in these different ways:
   * writeLink: fileApi.fileApi.openForWrite(dataProduct) - extension will be in config
   * writeEstimate/writeArray/writeDistribution: fileApi.fileApi.openForWrite(dataProduct, component, extension)
   */
  /*
  public CleanableFileChannel openForWrite(
      String dataProduct_name, String component_name, String extension) throws IOException {
    if (prepare_dp_forWrite(dataProduct_name, component_name, extension)) {
      return this.dp_info_map.get(dataProduct_name).getFilechannel();
    } else {
      return null;
    }
  }*/

  public CleanableFileChannel openForWrite(String dataProduct_name)  {
      return openForWrite(dataProduct_name, null, null);
  }

  /*
   * openForWrite - used in these different ways:
   * writeLink: fileApi.fileApi.openForWrite(dataProduct) - extension will be in config
   * writeEstimate/writeArray/writeDistribution: fileApi.fileApi.openForWrite(dataProduct, component, extension)
   */
  public CleanableFileChannel openForWrite(
          String dataProduct_name, String component_name, String extension)  {
    if (prepare_dp_forWrite(dataProduct_name, component_name, extension)) {
      try {
        return this.dp_info_map.get(dataProduct_name).getFilechannel();
      }catch(IOException e) {
        System.out.println("openForWrite() - exception: " + e);
        e.printStackTrace();
        return null;
      }
    } else {
      return null;
    }
  }

  /*
  * readlink: fileApi.fileApi.openForRead(dataProduct)
  * readEstimate/readArray/readDist: fileApi.fileApi.openForRead(dataProduct, component)

  */
  public CleanableFileChannel openForRead(String dataProduct_name) throws IOException {
    return openForRead(dataProduct_name, null);
  }

  public CleanableFileChannel openForRead(String dataProduct_name, String component_name)
      throws IOException {
    System.out.println("openForRead() start");
    if (prepare_dp_forRead(dataProduct_name, component_name)) {
      System.out.println("going to return the filechannel");
      dp_info d = this.dp_info_map.get(dataProduct_name);
      System.out.println("got the dp");
      CleanableFileChannel f = d.getFilechannel();
      System.out.println("got the filechannel");
      return f;
      // return this.dp_info_map.get(dataProduct_name).getFilechannel();
    } else {
      System.out.println("openForRead returning NULL");
      return null;
    }
  }

  private boolean prepare_dp_forRead(String dataProduct_name, String component_name) {
    System.out.println("prepare_dp_forRead()");
    dp_info dp = new dp_info(dataProduct_name, DP_READ);
    System.out.println("prepare_dp_forRead() after creating dp");
    dp_component dc;
    if (component_name == null) {
      dc = new dp_component(dp, "whole_object");
    } else {
      dc = new dp_component(dp, component_name);
    }
    System.out.println("prepare_dp_forRead() after creating dc");
    String componentUrl = dc.getObject_component().getUrl();
    System.out.println("prepare_dp_forRead() after getObjComp()./getUrl()");
    if (code_run_session.getInputs().contains(componentUrl)) {
      // ERROR: we've already read from this component
      return false;
    }
    code_run_session.addInput(componentUrl);
    if (!this.dp_info_map.containsKey(dataProduct_name)) {
      // only open the file if we haven't already opened (and stored in dp_file_map) previously
      System.out.println("openForRead() dp.getFilePath: " + dp.getFilePath());
      this.dp_info_map.put(dataProduct_name, dp);
    }
    return true;
  }

  /*private void executeOnCloseFileHandleDP(dp_info dp) {
    System.out.println("executeOnCloseFileHandleDP() .. " + dp.toString());
    if(dp.isHashed()) return;
    String hash = hasher.fileHash(dp.getFilePath().toString());
    Map<String, String> find_identical =
        Map.of(
            "storage_root",
            FDP_RootObject.get_id(dp.storage_location.getStorage_root()).toString(),
            "hash",
            hash,
            "public",
            dp.storage_location.isIs_public() ? "true" : "false");
    Storage_location sl =
        (Storage_location) restClient.getFirst(Storage_location.class, find_identical);
    if (sl != null) {
      // we've found an existing stolo with matching hash.. delete this one.
      dp.getFilePath().toFile().delete();
      dp.storage_location = sl;
      code_run_session.setStorageLocation(dp.givenDataProduct_name, sl);
    } else {
      dp.storage_location.setHash(hash);
    }
    dp.setHashed(true);
    // hash the dp.getFileName() and put this hash into updatedObjects
  }*/

  private boolean prepare_dp_forWrite(
      String dataProduct_name, String component_name, String extension) {
    if (component_name == null) {
      component_name = "whole_object";
    }

    if (code_run_session.contains_output_dp_component(dataProduct_name, component_name)) {
      System.out.println("ERROR: we've already written to this component");
      // ERROR: we've already written to this component
      return false;
    }

    if (!this.dp_info_map.containsKey(dataProduct_name)) {
      System.out.println("dp_info_map did not contain an entry for " + dataProduct_name);
      // only open the file if we haven't already opened (and stored in dp_file_map) previously
      dp_info dp = new dp_info(dataProduct_name, DP_WRITE, extension);
      System.out.println("openForWrite() dp.getFilePath: " + dp.getFilePath());
      File dir = new File(String.valueOf(dp.getFilePath().getParent()));
      if (!dir.exists()) {
        dir.mkdirs();
      }
      this.dp_info_map.put(dataProduct_name, dp);
    }else{
      System.out.println("dp_info_map ALREADY HAD ENTRY FOR " + dataProduct_name);
    }
    code_run_session.addOutput(dataProduct_name, component_name);
    return true;
  }

  public Path getFilePathForWrite(String dataProduct_name) {
    return this.getFilePathForWrite(dataProduct_name, null, null);
  }

  public Path getFilePathForWrite(
      String dataProduct_name, String component_name, String extension) {
    if (prepare_dp_forWrite(dataProduct_name, component_name, extension)) {
      return this.dp_info_map.get(dataProduct_name).getFilePath();
    } else {
      return null;
    }
  }

  public Path getFilePathForRead(String dataProduct_name) {
    return this.getFilePathForRead(dataProduct_name, null);
  }

  public Path getFilePathForRead(String dataProduct_name, String component_name) {
    if (prepare_dp_forRead(dataProduct_name, component_name)) {
      return this.dp_info_map.get(dataProduct_name).getFilePath();
    } else {
      return null;
    }
  }

  public Config getConfig() {
    return this.config;
  }

  @Override
  public void close() {
    System.out.println("\n\nfileapi.close()\n\n");
    dp_info_map.entrySet().stream()
        .forEach(
            li -> {
              li.getValue().ensureHashed();
            });
    System.out.println("ensured all files are hashed");
    code_run_session.finish();
    System.out.println("after code_run_session.finish()");
    // cleanable.clean();
  }
}
