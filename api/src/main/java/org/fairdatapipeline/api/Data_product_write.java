package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.APPEND;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.fairdatapipeline.config.ConfigException;
import org.fairdatapipeline.config.ImmutableConfigItem;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data_product_write is created by Coderun: {@link Coderun#get_dp_for_write(String, String)}
 *
 * <p>Upon {@link Coderun#close()} it will register itself and its components in the registry, and
 * then register its components in the coderun.
 */
public class Data_product_write extends Data_product {
  private static final Logger logger = LoggerFactory.getLogger(Data_product_write.class);
  private boolean is_hashed;

  Data_product_write(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  Data_product_write(String dataProduct_name, Coderun coderun, String extension) {
    super(dataProduct_name, coderun, extension);
  }

  void populate_data_product() {
    // called from the constructor
    if (this.getRegistryData_product() != null) {
      String msg =
          "You can't write to a Data Product that already exists: "
              + this.actualDataProduct_name
              + ", version "
              + this.version
              + ", namespace: "
              + this.namespace_name;
      logger.error(msg);
      throw (new IllegalActionException(msg));
    }
    File_type file_type = new File_type(this.extension, coderun.restClient);

    this.registryStorage_root = this.coderun.getWriteStorage_root();
    this.registryStorage_location = new RegistryStorage_location();
    this.registryStorage_location.setStorage_root(this.registryStorage_root.getUrl());
    String filename = this.version + "." + this.extension;
    Path my_stolo_path =
        Paths.get(this.namespace_name).resolve(this.actualDataProduct_name).resolve(filename);
    this.filePath = this.registryStorage_root.getPath().resolve(my_stolo_path);
    this.registryStorage_location.setPath(my_stolo_path.toString());
    this.registryObject = new RegistryObject();
    this.registryObject.setDescription(this.description);
    this.registryObject.setFile_type(file_type.getUrl());
    // o.setAuthors();
    this.registryData_product = new RegistryData_product();
    this.registryData_product.setName(this.actualDataProduct_name);
    this.registryData_product.setNamespace(this.registryNamespace.getUrl());
    this.registryData_product.setVersion(this.version);
  }

  List<ImmutableConfigItem> getConfigItems() {
    return coderun.config.writeItems();
  }

  String getDefaultNamespace_name() {
    return this.coderun.config.run_metadata().default_output_namespace().orElse("");
  }

  RegistryNamespace getRegistryNamespace(String namespace_name) {
    RegistryNamespace ns = super.getRegistryNamespace(namespace_name);
    if (ns == null) {
      ns = (RegistryNamespace) coderun.restClient.post(new RegistryNamespace(namespace_name));
      if (ns == null) {
        String msg = "Failed to create in registry: namespace '" + namespace_name + "'";
        logger.error(msg);
        throw (new RegistryException(msg));
      }
    }
    return ns;
  }

  private boolean globMatch(String pattern, String dataProduct_name) {
    if (pattern.endsWith("/*")) {
      return dataProduct_name.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return pattern.equals(dataProduct_name);
  }

  ImmutableConfigItem getConfigItem(String dataProduct_name) {
    ImmutableConfigItem configItem = super.getConfigItem(dataProduct_name);
    if (configItem == null) {
      // for WRITING dp's; we allow /* globbing if there is no exact match we look for a /* match
      configItem =
          this.getConfigItems().stream()
              .filter(ci -> globMatch(ci.data_product(), dataProduct_name))
              .findFirst()
              .orElse(null);
    }
    if (configItem == null) {
      String msg = "DataProduct " + dataProduct_name + " not found in config";
      logger.error(msg);
      throw (new ConfigException(msg));
    }
    return configItem;
  }

  private void executeOnCloseFileHandleDP() {
    this.do_hash();
  }

  private void do_hash() {
    if (this.is_hashed) return;
    String hash = coderun.hasher.fileHash(this.filePath.toString());
    this.registryStorage_location.setHash(hash);
    this.is_hashed = true;
  }

  Path getFilePath() {
    this.been_used = true;
    this.is_hashed = false;
    if (!this.filePath.getParent().toFile().exists()) {
      try {
        Files.createDirectories(this.filePath.getParent());
      } catch (IOException e) {
        logger.error("failed to create directory " + this.filePath.getParent().toString());
        // throw, or continue?
        return null;
      }
    }
    return this.filePath;
  }

  CleanableFileChannel getFilechannel() throws IOException {
    this.been_used = true;
    Runnable onClose = this::executeOnCloseFileHandleDP;
    if (this.filechannel == null) {
      if (!this.filePath.getParent().toFile().exists()) {
        Files.createDirectories(this.filePath.getParent());
      }
      this.filechannel =
          new CleanableFileChannel(FileChannel.open(this.filePath, CREATE_NEW, WRITE), onClose);
    } else {
      if (!this.filechannel.isOpen()) {
        this.filechannel =
            new CleanableFileChannel(FileChannel.open(this.filePath, APPEND, WRITE), onClose);
      }
    }
    this.is_hashed = false;
    return this.filechannel;
  }

  void closeFileChannel() {
    if (this.filechannel != null) {
      this.filechannel.close();
      this.filechannel = null;
    }
  }

  /**
   * Obtain an Object_component for writing.
   *
   * @param component_name the name of the object component.
   * @return the Object_component_write object.
   */
  public Object_component_write getComponent(String component_name) {
    if (componentMap.containsKey(component_name))
      return (Object_component_write) componentMap.get(component_name);
    Object_component_write dc;
    dc =
        new Object_component_write(
            this, Objects.requireNonNullElse(component_name, "whole_object"));
    componentMap.put(component_name, dc);
    return dc;
  }

  /**
   * Obtain an Object_component (whole_object) for writing.
   *
   * @return the Object_component class
   */
  public Object_component_write getComponent() {
    if (this.whole_obj_oc == null) this.whole_obj_oc = new Object_component_write(this);
    // componentMap.put(component_name, dc);
    return (Object_component_write) this.whole_obj_oc;
  }

  void stolo_obj_and_dp_to_registry() {
    // Storage_location sl = this.storage_location;
    if (this.registryStorage_location.getUrl() == null) {
      // String storageRoot = this.storage_location.getStorage_root();
      Map<String, String> find_identical =
          Map.of(
              "storage_root",
              Registry_RootObject.get_id(this.registryStorage_location.getStorage_root())
                  .toString(),
              "hash",
              this.registryStorage_location.getHash(),
              "public",
              this.registryStorage_location.isIs_public() ? "true" : "false");
      RegistryStorage_location identical_sl =
          (RegistryStorage_location)
              this.coderun.restClient.getFirst(RegistryStorage_location.class, find_identical);
      if (identical_sl != null) {
        // we've found an existing stolo with matching hash. delete this one.
        if (!this.filePath.toFile().delete()) {
          logger.warn(
              "Failed to delete current data file which is identical to a file already in the local registry.");
        }
        this.registryStorage_location = identical_sl;
        // my FilePath is now wrong!
      } else {
        // we've not found an existing stolo with matching hash. store this one.
        RegistryStorage_location sl =
            (RegistryStorage_location) this.coderun.restClient.post(this.registryStorage_location);
        if (sl == null) {
          String msg =
              "Failed to create in registry: new storage location "
                  + this.registryStorage_location.getPath();
          logger.error(msg);
          throw (new RegistryException(msg));
        } else {
          this.registryStorage_location = sl;
        }
      }
    }
    this.registryObject.setStorage_location(this.registryStorage_location.getUrl());
    final RegistryObject o = (RegistryObject) this.coderun.restClient.post(this.registryObject);
    if (o == null) {
      String msg = "Failed to create in registry: Object " + this.registryObject.getDescription();
      logger.error(msg);
      throw (new RegistryException(msg));
    }
    this.registryObject = o;
    this.registryData_product.setObject(o.getUrl());
    RegistryData_product dp =
        (RegistryData_product) this.coderun.restClient.post(this.registryData_product);
    if (dp == null) {
      String msg =
          "Failed to create in registry: Data_product " + this.registryData_product.getName();
      logger.error(msg);
      throw (new RegistryException(msg));
    }
    this.registryData_product = dp;
  }

  void components_to_registry() {
    if (this.whole_obj_oc != null) this.whole_obj_oc.register_me_in_registry();
    this.componentMap.forEach((key, value) -> value.register_me_in_registry());
  }

  void objects_to_registry() {
    this.do_hash();
    this.stolo_obj_and_dp_to_registry();
    this.components_to_registry();
  }
}
