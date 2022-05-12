package org.fairdatapipeline.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fairdatapipeline.config.ImmutableConfigItem;
import org.fairdatapipeline.dataregistry.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data product is created: {@link Coderun#get_dp_for_write_link(String, String)} or {@link
 * Coderun#get_dp_for_read_link(String)}
 *
 * <p>Upon {@link Coderun#close()} it will try to register itself and its components in the
 * registry, and then register itself in the coderun.
 */
abstract class Data_product implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(Data_product.class);
  /** coderun is the Coderun that created me */
  final Coderun coderun;
  /**
   * registryNamespace is the registry object for this.namespace_name; namespace must always exist
   * (created by CLI)
   */
  final RegistryNamespace registryNamespace;
  /**
   * namespace_name: from config run_metadata default_input_namespace (for read) or
   * default_output_namespace (for write) then possibly overwritten by configItem.use().namespace()
   */
  String namespace_name;
  /**
   * extension can be null upon creation, in that case, for a write item: it must be found in the
   * configItem.file_type() for a read item: it can be left null.
   */
  String extension;
  /**
   * version MUST be provided by config. used upon creation by this.getRegistryData_product -- for
   * read to check that it DOES exist, for write to check that it DOESN't.
   */
  final String version;
  /**
   * description is an optional config field, only used for 'write'; this will get copied into the
   * Data_product in the registry.
   */
  final String description;
  /**
   * registryData_product created in populate_data_product() - for read this is the
   * RegistryData_product received from registry; for write this is the new RegistryData_product()
   * we will post to the registry upon close()
   */
  RegistryData_product registryData_product;
  /**
   * registryObject created in populate_data_product() - for read this is the RegistryObject
   * received from registry; for write this is the new RegistryObject() we will post to the registry
   * upon close()
   */
  RegistryObject registryObject;
  /**
   * registryStorage_location created in populate_data_product() - for read this is the
   * RegistryStorage_location received from registry; for write this is the new
   * RegistryStorage_location() we will post to the registry upon close()
   */
  RegistryStorage_location registryStorage_location;
  /**
   * registryStorage_root is this object's Storage_root - for read this is the
   * registryStorage_location received from the registry (linked from
   * this.registryStorage_location); for write this is taken from coderun.getWriteStorage_root()
   */
  RegistryStorage_root registryStorage_root;
  /**
   * givenDataProduct_name is the name given by the user (asked for in the FileApi.get_dp_for_xxx()
   * call)
   */
  final String givenDataProduct_name;
  /**
   * actualDataProduct_name is usually the same as the given name, unless the config use section has
   * given an alternative data product name to open.
   */
  String actualDataProduct_name;
  /** whole_obj_oc */
  Object_component whole_obj_oc;
  /** componentMap */
  final Map<String, Object_component> componentMap = new HashMap<>();
  /** been_used to check at close() whether anything actually got read or written */
  boolean been_used = false;

  Data_product(String dataProduct_name, Coderun coderun) {
    this(dataProduct_name, coderun, null);
  }

  Data_product(String dataProduct_name, Coderun coderun, String extension) {
    this.extension = extension;
    this.coderun = coderun;
    this.givenDataProduct_name = dataProduct_name;
    this.actualDataProduct_name = dataProduct_name;
    this.namespace_name = this.getDefaultNamespace_name();
    ImmutableConfigItem configItem = this.getConfigItem();
    if (configItem.use().namespace().isPresent())
      namespace_name = configItem.use().namespace().get();
    if (configItem.use().data_product().isPresent())
      this.actualDataProduct_name = configItem.use().data_product().get();
    if (configItem.file_type().isPresent()) {
      if (extension != null && !configItem.file_type().get().equals(extension))
        logger.warn(
            "file type conflict: code says {}, config says {}",
            extension,
            configItem.file_type().get());
      this.extension = configItem.file_type().get();
    }
    this.description = configItem.description().orElse("");
    this.version = configItem.use().version();
    this.registryNamespace = this.getRegistryNamespace(this.namespace_name);
    this.populate_data_product();
  }

  /*
   * this should populate the main data fields: data_product, storage_location and fdpObject
   * for READ: by reading these from the registry
   * for WRITE: by preparing empty ones that can later be posted to registry
   */
  abstract void populate_data_product();

  /**
   * @return the config writeItems (if this is a write dp) or config readItems (if this is a read
   *     dp)
   */
  abstract List<ImmutableConfigItem> getConfigItems();

  /**
   * @return from config.run_metadata(): return default_input_namespace (for Data_product_read) of
   *     default_output_namespace (for Data_product_write)
   */
  abstract String getDefaultNamespace_name();

  /**
   * getRegistryNamespace retrieves the registryNamespace from the registry. This should always
   * exist as it has been created by the CLI.
   *
   * @param namespace_name
   * @return the RegistryNamespace for the namespace with given name.
   * @throws RegistryObjectNotFoundException if the namespace is not found in the registry.
   */
  RegistryNamespace getRegistryNamespace(String namespace_name) {
    RegistryNamespace ns =
        (RegistryNamespace)
            coderun.restClient.getFirst(
                RegistryNamespace.class, Collections.singletonMap("name", namespace_name));
    if (ns == null)
      throw (new RegistryObjectNotFoundException("Can't find the namespace " + namespace_name));
    return ns;
  }

  /**
   * getRegistryData_product() is used by data_product_read to retrieve the data_product from
   * registry; it is used by data_product_write to check the data_product does not exist yet.
   *
   * @return The RegistryData_product from the registry with this.actualDataProduct_name,
   *     this.registryNamespace.get_id(), and this.version. returns null is not found.
   */
  RegistryData_product getRegistryData_product() {
    Map<String, String> dp_map =
        Map.of(
            "name",
            this.actualDataProduct_name,
            "namespace",
            this.registryNamespace.get_id().toString(),
            "version",
            version);
    return (RegistryData_product) coderun.restClient.getFirst(RegistryData_product.class, dp_map);
  }

  /**
   * getConfigItem searches the ConfigItems for this.givenDataProduct_name. Data_product_read will
   * override this and throw ConfigException if not found. Data_product_write will override this to
   * add globbing match for config items ending in /*, and also throw ConfigException if not found.
   *
   * @return the first of this.getConfigItems() that matches this dataProduct_name. returns null if
   *     not found.
   */
  ImmutableConfigItem getConfigItem() {

    return this.getConfigItems().stream()
        .filter(ci -> ci.data_product().equals(this.givenDataProduct_name))
        .findFirst()
        .orElse(null);
  }

  /**
   * getFilePath creates the path for this data_products file. it tries to create the directory if
   * it doesn't exist.
   *
   * @return the Path for this data product file, by appending registryStorage_location path to the
   *     registryStorage_root path.
   * @throws IOException if we fail to create the directory for this file.
   */
  Path getFilePath() {
    this.been_used = true;
    Path filePath =
        this.registryStorage_root
            .getPath()
            .resolve(Path.of(this.registryStorage_location.getPath()));
    if (!filePath.getParent().toFile().exists()) {
      try {
        Files.createDirectories(filePath.getParent());
      } catch (IOException e) {
        logger.error("failed to create directory {}", filePath.getParent());
        // throw, or continue?
        return null;
      }
    }
    return filePath;
  }

  /**
   * do_hash() is used by data_product_write to set the registryStorage_location.hash upon close.
   * nothing to do for data_product_read
   */
  abstract void do_hash();

  /**
   * stolo_obj_and_dp_to_registry() will post my registryStorage_location, RegistryObject, and
   * registryData_product to the registry. (find matching stolo hash and delete this file if there
   * already exists an identical) nothing to do for data_product_read
   */
  abstract void stolo_obj_and_dp_to_registry();

  void components_to_registry() {
    if (this.whole_obj_oc != null) this.whole_obj_oc.register_me_in_registry();
    this.componentMap.forEach((key, value) -> value.register_me_in_registry());
  }

  void InputsOutputsToCoderun() {
    if (this.whole_obj_oc != null) this.whole_obj_oc.register_me_in_code_run();
    this.componentMap.forEach((key, value) -> value.register_me_in_code_run());
  }

  @Override
  public void close() {
    this.do_hash();
    this.stolo_obj_and_dp_to_registry();
    this.components_to_registry();
    this.InputsOutputsToCoderun();
  }
}
