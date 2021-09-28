package org.fairdatapipeline.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fairdatapipeline.config.ImmutableConfigItem;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data product is created: {@link Coderun#get_dp_for_write(String, String)} or {@link
 * Coderun#get_dp_for_read(String)}
 *
 * <p>Upon {@link Coderun#close()} it will try to register itself and its components in the
 * registry, and then register itself in the coderun.
 */
public abstract class Data_product implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(Data_product.class);
  Coderun coderun;
  RegistryNamespace registryNamespace;
  String namespace_name;
  String extension;
  String version;
  String description;
  RegistryData_product registryData_product;
  RegistryObject registryObject;
  RegistryStorage_location registryStorage_location;
  RegistryStorage_root registryStorage_root;
  Path filePath;
  CleanableFileChannel filechannel;
  Object_component whole_obj_oc;
  /** the name given by the user (asked for in the FileApi.get_dp_for_xxx() call) */
  String givenDataProduct_name;
  /**
   * usually the same as the given name, unless the config use section has given an alternative data
   * product name to open.
   */
  String actualDataProduct_name;

  Map<String, Object_component> componentMap = new HashMap<>();
  List<ImmutableConfigItem> configItems;
  boolean been_used = false;

  Data_product(String dataProduct_name, Coderun coderun) {
    this(dataProduct_name, coderun, null);
  }

  Data_product(String dataProduct_name, Coderun coderun, String extension) {
    this.extension = extension;
    this.coderun = coderun;
    this.givenDataProduct_name = dataProduct_name;
    this.actualDataProduct_name = dataProduct_name;
    this.configItems = this.getConfigItems();
    this.namespace_name = this.getDefaultNamespace_name();
    ImmutableConfigItem configItem = this.getConfigItem(dataProduct_name);
    if (configItem.use().namespace().isPresent()) {
      namespace_name = configItem.use().namespace().get();
    }
    if (configItem.use().data_product().isPresent()) {
      this.actualDataProduct_name = configItem.use().data_product().get();
    }
    if (configItem.file_type().isPresent()) {
      if (extension != null && !configItem.file_type().get().equals(extension)) {
        logger.warn(
            "file type conflict: code says "
                + extension
                + ", config says "
                + configItem.file_type().get());
      }
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
   * for WRITE: by preparing empty ones that can later by posted to registry
   */
  abstract void populate_data_product();

  abstract List<ImmutableConfigItem> getConfigItems();

  abstract String getDefaultNamespace_name();

  RegistryNamespace getRegistryNamespace(String namespace_name) {
    return (RegistryNamespace)
        coderun.restClient.getFirst(
            RegistryNamespace.class, Collections.singletonMap("name", namespace_name));
  }

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

  ImmutableConfigItem getConfigItem(String dataProduct_name) {

    return this.getConfigItems().stream()
        .filter(ci -> ci.data_product().equals(dataProduct_name))
        .findFirst()
        .orElse(null);
  }

  /*
   *
   */
  abstract Path getFilePath();

  /*
   * please make sure the implementation set been_used = true;
   */
  abstract CleanableFileChannel getFilechannel() throws IOException;

  void closeFileChannel() {
    if (this.filechannel != null) {
      this.filechannel.close();
      this.filechannel = null;
    }
  }

  // public abstract Object_component_write getComponent(String component_name);

  abstract void objects_to_registry();

  void InputsOutputsToCoderun() {
    if (this.whole_obj_oc != null) this.whole_obj_oc.register_me_in_code_run();
    this.componentMap.forEach((key, value) -> value.register_me_in_code_run());
  }

  @Override
  public void close() {
    this.closeFileChannel();
    this.objects_to_registry();
    this.InputsOutputsToCoderun();
  }
}
