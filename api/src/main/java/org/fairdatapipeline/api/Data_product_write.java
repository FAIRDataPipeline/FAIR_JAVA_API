package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.fairdatapipeline.config.ConfigException;
import org.fairdatapipeline.config.ImmutableConfigItem;
import org.fairdatapipeline.dataregistry.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data_product_write is created by Coderun: {@link Coderun#get_dp_for_write_link(String, String)}
 *
 * <p>Upon {@link Coderun#close()} it will register itself and its components in the registry, and
 * then register its components in the coderun.
 */
abstract class Data_product_write extends Data_product {
  private static final Logger logger = LoggerFactory.getLogger(Data_product_write.class);
  boolean is_hashed;

  Data_product_write(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  Data_product_write(String dataProduct_name, Coderun coderun, String extension) {
    super(dataProduct_name, coderun, extension);
  }

  void populate_data_product() {
    // called from the constructor
    if (this.getRegistryData_product() != null) {
      throw (new IllegalActionException(
          "You can't write to a Data Product that already exists: "
              + this.actualDataProduct_name
              + ", version "
              + this.version
              + ", namespace: "
              + this.namespace_name));
    }
    File_type file_type = new File_type(this.extension, coderun.restClient);

    this.registryStorage_root = this.coderun.getWriteStorage_root();
    this.registryStorage_location = new RegistryStorage_location();
    this.registryStorage_location.setStorage_root(this.registryStorage_root.getUrl());
    String filename = this.version + "." + this.extension;
    Path my_stolo_path =
        Paths.get(this.namespace_name).resolve(this.actualDataProduct_name).resolve(filename);
    this.registryStorage_location.setPath(my_stolo_path.toString());
    this.registryObject = new RegistryObject();
    this.registryObject.setDescription(this.description);
    this.registryObject.setAuthors(this.coderun.authors);
    this.registryObject.setFile_type(file_type.getUrl());
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

  /**
   * @param pattern is the data_product name from the config, that may end in /* to allow matching
   *     for glob matching.
   * @return if pattern ends in /*, we return true if this.givenDataProduct_name starts with pattern
   *     without the * (matching up to and including the /)
   */
  private boolean globMatch(String pattern) {
    if (pattern.endsWith("/*")) {
      return this.givenDataProduct_name.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return pattern.equals(this.givenDataProduct_name);
  }

  /**
   * getConfigItem searches the ConfigItems for this.givenDataProduct_name. if there is no exact
   * match, try globMatch for configItems ending in /*
   *
   * @return the matching configItem
   * @throws ConfigException if none matches.
   */
  @Override
  ImmutableConfigItem getConfigItem() {
    ImmutableConfigItem configItem = super.getConfigItem();
    if (configItem == null) {
      // for WRITING dp's; we allow /* globbing if there is no exact match we look for a /* match
      configItem =
          this.getConfigItems().stream()
              .filter(ci -> globMatch(ci.data_product()))
              .findFirst()
              .orElse(null);
    }
    if (configItem == null) {
      throw (new ConfigException(
          "DataProduct " + this.givenDataProduct_name + " not found in config"));
    }
    return configItem;
  }

  private void executeOnCloseFileHandleDP() {
    this.do_hash();
  }

  void do_hash() {
    if (this.is_hashed) return;
    String hash = coderun.hasher.fileHash(this.getFilePath().toString());
    this.registryStorage_location.setHash(hash);
    this.is_hashed = true;
  }

  void stolo_obj_and_dp_to_registry() {
    if (this.registryStorage_location.getUrl() == null) {
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
        try {
          Files.delete(this.getFilePath());
        } catch (IOException e) {
          logger.warn(
              "Failed to delete current data file which is identical to a file already in the local registry.",
              e);
        }
        this.registryStorage_location = identical_sl;
        // my FilePath is now wrong!
      } else {
        // we've not found an existing stolo with matching hash. store this one.
        RegistryStorage_location sl =
            (RegistryStorage_location) this.coderun.restClient.post(this.registryStorage_location);
        if (sl == null) {
          throw (new RegistryException(
              "Failed to create in registry: new storage location "
                  + this.registryStorage_location.getPath()));
        } else {
          this.registryStorage_location = sl;
        }
      }
    }
    this.registryObject.setStorage_location(this.registryStorage_location.getUrl());
    final RegistryObject o = (RegistryObject) this.coderun.restClient.post(this.registryObject);
    if (o == null) {
      throw (new RegistryException(
          "Failed to create in registry: Object " + this.registryObject.getDescription()));
    }
    this.registryObject = o;
    this.registryData_product.setObject(o.getUrl());
    RegistryData_product dp =
        (RegistryData_product) this.coderun.restClient.post(this.registryData_product);
    if (dp == null) {
      throw (new RegistryException(
          "Failed to create in registry: Data_product " + this.registryData_product.getName()));
    }
    this.registryData_product = dp;
  }
}
