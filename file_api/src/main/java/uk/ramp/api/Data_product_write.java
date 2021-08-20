package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.APPEND;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.dataregistry.content.*;
import uk.ramp.file.CleanableFileChannel;

/**
 * retrieving and storing the dataRegistry details for a dataproduct; as requested from the API
 * user, and possibly amended by the config.
 */
public class Data_product_write extends Data_product_RW {
  private boolean is_hashed;

  public Data_product_write(String dataProduct_name, FileApi fileApi) {
    super(dataProduct_name, fileApi);
  }

  public Data_product_write(String dataProduct_name, FileApi fileApi, String extension) {
    super(dataProduct_name, fileApi, extension);
  }

  void populate_dataproduct() {
    // called from the constructor
    if (this.getDataProduct() != null) {
      throw (new IllegalArgumentException(
          "Data Product already exists: "
              + this.actualDataProduct_name
              + ", version "
              + this.version
              + ", namespace: "
              + this.namespace_name));
    }
    this.storage_root = this.getStorageRoot();
    this.storage_location = new Storage_location();
    this.storage_location.setStorage_root(this.storage_root.getUrl());
    String filename = this.version + "." + this.extension;
    Path my_stolo_path =
        Paths.get(this.namespace_name).resolve(this.actualDataProduct_name).resolve(filename);
    this.filePath = Paths.get(this.storage_root.getRoot()).resolve(my_stolo_path);
    this.storage_location.setPath(my_stolo_path.toString());
    this.fdpObject = new FDPObject();
    this.fdpObject.setDescription(this.description);
    // o.setAuthors();
    this.data_product =
        new Data_product(); // sorry for the confusion but this is a restclient Object for registry.
    this.data_product.setName(this.actualDataProduct_name);
    this.data_product.setNamespace(this.namespace.getUrl());
    this.data_product.setVersion(this.version);
  }

  List<ImmutableConfigItem> getConfigItems() {
    return fileApi.config.writeItems();
  }

  String getDefaultNamespace_name() {
    return this.fileApi.config.run_metadata().default_output_namespace().orElse("");
  }

  Namespace getNamespace(String namespace_name) {
    Namespace ns = super.getNamespace(namespace_name);
    if (ns == null) {
      ns = (Namespace) fileApi.restClient.post(new Namespace(namespace_name));
      if (ns == null) {
        throw (new IllegalArgumentException(
            "failed to create in registry: namespace '" + namespace_name + "'"));
      }
    }
    return ns;
  }

  public Data_product getDataProduct() {
    // for a write DP we just make sure the DP should not exist yet:
    Data_product dp = super.getDataProduct();
    if (dp != null) {
      throw (new IllegalArgumentException(
          "Trying to write to existing data_product "
              + this.actualDataProduct_name
              + "; NS "
              + this.namespace.get_id().toString()
              + "; version "
              + version));
    }
    return dp;
  }

  private boolean globMatch(String pattern, String dataProduct_name) {
    if(pattern.endsWith("/*")){
      return dataProduct_name.startsWith(pattern.substring(0, pattern.length()-1));
    }
    return pattern.equals(dataProduct_name);
  }

  ImmutableConfigItem getConfigItem(String dataProduct_name) {
    ImmutableConfigItem configItem = super.getConfigItem(dataProduct_name);
    if (configItem == null) {
      // for WRITING dp's; we allow /* globbing if there is no exact match we look for a /* match
      configItem =
          this.getConfigItems().stream()
              .filter(
                  ci -> globMatch(ci.data_product(), dataProduct_name))
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
    this.storage_location.setHash(hash);
    this.is_hashed = true;
  }

  CleanableFileChannel getFilechannel() throws IOException {
    this.been_used = true;
    Runnable onClose = this::executeOnCloseFileHandleDP;
    if (this.filechannel == null) {
      if(!this.filePath.getParent().toFile().exists()){
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

  void stolo_obj_and_dp_to_registry() {
    // Storage_location sl = this.storage_location;
    if (this.storage_location.getUrl() == null) {
      // String storageRoot = this.storage_location.getStorage_root();
      Map<String, String> find_identical =
          Map.of(
              "storage_root",
              FDP_RootObject.get_id(this.storage_location.getStorage_root()).toString(),
              "hash",
              this.storage_location.getHash(),
              "public",
              this.storage_location.isIs_public() ? "true" : "false");
      Storage_location identical_sl =
          (Storage_location)
              this.fileApi.restClient.getFirst(Storage_location.class, find_identical);
      if (identical_sl != null) {
        // we've found an existing stolo with matching hash.. delete this one.
        this.filePath.toFile().delete();
        this.storage_location = identical_sl;
        // my FilePath is now wrong!
      } else {
        // we've not found an existing stolo with matching hash.. store this one.
        Storage_location sl =
            (Storage_location) this.fileApi.restClient.post(this.storage_location);
        if (sl == null) {
          throw (new IllegalArgumentException(
              "Failed to create in registry: new storage location "
                  + this.storage_location.getPath()));
        } else {
          this.storage_location = sl;
        }
      }
    }
    this.fdpObject.setStorage_location(this.storage_location.getUrl());
    final FDPObject o = (FDPObject) this.fileApi.restClient.post(this.fdpObject);
    if (o == null)
      throw (new IllegalArgumentException(
          "Failed to create in registry: Object " + this.fdpObject.getDescription()));
    this.fdpObject = o;
    this.data_product.setObject(o.getUrl());
    Data_product dp = (Data_product) this.fileApi.restClient.post(this.data_product);
    if (dp == null) {
      throw (new IllegalArgumentException(
          "Failed to create in registry: Data_product " + this.data_product.getName()));
    }
    this.data_product = dp;
  }

  void components_to_registry() {
    this.componentMap.entrySet().stream().filter(c -> c.getValue().been_used)
        .forEach(
            component -> {
              component.getValue().register_me_in_registry();
            });
  }

  Storage_root getStorageRoot() {
    String storage_root_path = fileApi.config.run_metadata().write_data_store().orElse("");
    if (storage_root_path == "") {
      throw (new IllegalArgumentException("No write_data_store given in config."));
    }
    Storage_root sr =
        (Storage_root)
            fileApi.restClient.getFirst(
                Storage_root.class, Collections.singletonMap("root", storage_root_path));
    if (sr == null) {
      sr = (Storage_root) fileApi.restClient.post(new Storage_root(storage_root_path));
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
