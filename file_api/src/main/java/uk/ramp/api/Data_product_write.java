package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.APPEND;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.dataregistry.content.*;
import uk.ramp.file.CleanableFileChannel;

/**
 * Class representing a data product for writing.
 */
public class Data_product_write extends Data_product {
  private boolean is_hashed;

  Data_product_write(String dataProduct_name, FileApi fileApi) {
    super(dataProduct_name, fileApi);
  }

  Data_product_write(String dataProduct_name, FileApi fileApi, String extension) {
    super(dataProduct_name, fileApi, extension);
  }

  void populate_dataproduct() {
    // called from the constructor
    if (this.getRegistryData_product() != null) {
      throw (new IllegalArgumentException(
          "Data Product already exists: "
              + this.actualDataProduct_name
              + ", version "
              + this.version
              + ", namespace: "
              + this.namespace_name));
    }
    File_type file_type = new File_type(this.extension, fileApi.restClient);
    this.registryStorage_root = this.getStorageRoot();
    this.registryStorage_location = new RegistryStorage_location();
    this.registryStorage_location.setStorage_root(this.registryStorage_root.getUrl());
    String filename = this.version + "." + this.extension;
    Path my_stolo_path =
        Paths.get(this.namespace_name).resolve(this.actualDataProduct_name).resolve(filename);
    this.filePath = Paths.get(this.registryStorage_root.getRoot()).resolve(my_stolo_path);
    this.registryStorage_location.setPath(my_stolo_path.toString());
    this.fdpObject = new RegistryObject();
    this.fdpObject.setDescription(this.description);
    this.fdpObject.setFile_type(file_type.registryFile_type.getUrl());
    // o.setAuthors();
    this.registryData_product =
        new RegistryData_product(); // sorry for the confusion but this is a restclient Object for
    // registry.
    this.registryData_product.setName(this.actualDataProduct_name);
    this.registryData_product.setNamespace(this.registryNamespace.getUrl());
    this.registryData_product.setVersion(this.version);
  }

  List<ImmutableConfigItem> getConfigItems() {
    return fileApi.config.writeItems();
  }

  String getDefaultNamespace_name() {
    return this.fileApi.config.run_metadata().default_output_namespace().orElse("");
  }

  RegistryNamespace getRegistryNamespace(String namespace_name) {
    RegistryNamespace ns = super.getRegistryNamespace(namespace_name);
    if (ns == null) {
      ns = (RegistryNamespace) fileApi.restClient.post(new RegistryNamespace(namespace_name));
      if (ns == null) {
        throw (new IllegalArgumentException(
            "failed to create in registry: namespace '" + namespace_name + "'"));
      }
    }
    return ns;
  }

  RegistryData_product getRegistryData_product() {
    // for a write DP we just make sure the DP should not exist yet:
    RegistryData_product dp = super.getRegistryData_product();
    if (dp != null) {
      throw (new IllegalArgumentException(
          "Trying to write to existing data_product "
              + this.actualDataProduct_name
              + "; NS "
              + this.registryNamespace.get_id().toString()
              + "; version "
              + version));
    }
    return dp;
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
      throw (new IllegalArgumentException(
          "dataProduct " + dataProduct_name + " not found in config"));
    }
    return configItem;
  }

  private void executeOnCloseFileHandleDP() {
    this.do_hash();
  }

  private void do_hash() {
    if (this.is_hashed) return;
    String hash = fileApi.hasher.fileHash(this.filePath.toString());
    this.registryStorage_location.setHash(hash);
    this.is_hashed = true;
  }

  Path getFilePath() {
    this.been_used = true;
    this.is_hashed = false;
    if (!this.filePath.getParent().toFile().exists()) {
      try {
        Files.createDirectories(this.filePath.getParent());
      }catch (IOException e) {
        System.out.println("failed to create directory " + this.filePath.getParent().toString());
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
   * @param component_name the name of the object component.
   * @return the Object_component_write object.
   */
  public Object_component_write getComponent(String component_name) {
    if (componentMap.containsKey(component_name))
      return (Object_component_write) componentMap.get(component_name);
    Object_component_write dc;
    if (component_name == null) {
      dc = new Object_component_write(this, "whole_object");
    } else {
      dc = new Object_component_write(this, component_name);
    }
    componentMap.put(component_name, dc);
    return dc;
  }

  /**
   * Obtain an Object_component (whole_object) for writing.
   * @return the Object_component class
   *
   */
  public Object_component_write getComponent() {
    if(this.whole_obj_oc == null)
      this.whole_obj_oc = (Object_component) new Object_component_write(this);
    //componentMap.put(component_name, dc);
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
              this.fileApi.restClient.getFirst(RegistryStorage_location.class, find_identical);
      if (identical_sl != null) {
        // we've found an existing stolo with matching hash.. delete this one.
        this.filePath.toFile().delete();
        this.registryStorage_location = identical_sl;
        // my FilePath is now wrong!
      } else {
        // we've not found an existing stolo with matching hash.. store this one.
        RegistryStorage_location sl =
            (RegistryStorage_location) this.fileApi.restClient.post(this.registryStorage_location);
        if (sl == null) {
          throw (new IllegalArgumentException(
              "Failed to create in registry: new storage location "
                  + this.registryStorage_location.getPath()));
        } else {
          this.registryStorage_location = sl;
        }
      }
    }
    this.fdpObject.setStorage_location(this.registryStorage_location.getUrl());
    final RegistryObject o = (RegistryObject) this.fileApi.restClient.post(this.fdpObject);
    if (o == null)
      throw (new IllegalArgumentException(
          "Failed to create in registry: Object " + this.fdpObject.getDescription()));
    this.fdpObject = o;
    this.registryData_product.setObject(o.getUrl());
    RegistryData_product dp =
        (RegistryData_product) this.fileApi.restClient.post(this.registryData_product);
    if (dp == null) {
      throw (new IllegalArgumentException(
          "Failed to create in registry: Data_product " + this.registryData_product.getName()));
    }
    this.registryData_product = dp;
  }

  void components_to_registry() {
    if(this.whole_obj_oc != null) this.whole_obj_oc.register_me_in_registry();
    this.componentMap.entrySet().stream()
        .forEach(
            component -> {
              component.getValue().register_me_in_registry();
            });
  }

  RegistryStorage_root getStorageRoot() {
    String storage_root_path = fileApi.config.run_metadata().write_data_store().orElse("");
    if (storage_root_path == "") {
      throw (new IllegalArgumentException("No write_data_store given in config."));
    }
    RegistryStorage_root sr =
        (RegistryStorage_root)
            fileApi.restClient.getFirst(
                RegistryStorage_root.class, Collections.singletonMap("root", storage_root_path));
    if (sr == null) {
      sr =
          (RegistryStorage_root)
              fileApi.restClient.post(new RegistryStorage_root(storage_root_path));
      if (sr == null) {
        throw (new IllegalArgumentException(
            "failed to create in registry: storage root " + storage_root_path));
      }
    }
    return sr;
  }

  void objects_to_registry() {
    this.do_hash();
    this.stolo_obj_and_dp_to_registry();
    this.components_to_registry();
  }
}
