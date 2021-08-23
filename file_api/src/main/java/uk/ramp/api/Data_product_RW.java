package uk.ramp.api;

import java.io.IOException;
import java.nio.file.Path;
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
public abstract class Data_product_RW implements AutoCloseable {
  protected FileApi fileApi;
  protected RegistryNamespace registryNamespace;
  protected String namespace_name;
  protected String extension;
  protected String version;
  protected String description;
  protected RegistryData_product registryData_product;
  protected RegistryObject fdpObject;
  protected RegistryStorage_location registryStorage_location;
  protected RegistryStorage_root registryStorage_root;
  protected Path filePath;
  protected CleanableFileChannel filechannel;
  protected String givenDataProduct_name;
  protected String actualDataProduct_name;
  protected Map<String, Object_component_RW> componentMap = new HashMap<>();
  protected List<ImmutableConfigItem> configItems;
  protected boolean been_used = false;

  Data_product_RW(String dataProduct_name, FileApi fileApi) {
    this(dataProduct_name, fileApi, null);
  }

  Data_product_RW(String dataProduct_name, FileApi fileApi, String extension) {
    this.extension = extension;
    this.fileApi = fileApi;
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
        // TODO: logger WARNING conflict between 2 file_types
        System.out.println(
            "file type conflict: code says "
                + extension
                + ", config says "
                + configItem.file_type().get());
      }
      this.extension = configItem.file_type().get();
    }
    this.description = configItem.description().orElse("");
    this.version = configItem.use().version();
    this.registryNamespace = this.getNamespace(this.namespace_name);
    this.populate_dataproduct();
  }

  /*
   * this should populate the main data fields: data_product, storage_location and fdpObject
   * for READ: by reading these from the registry
   * for WRITE: by preparing empty ones that can later by posted to registry
   */
  abstract void populate_dataproduct();

  abstract List<ImmutableConfigItem> getConfigItems();

  abstract String getDefaultNamespace_name();

  RegistryNamespace getNamespace(String namespace_name) {
    return (RegistryNamespace)
        fileApi.restClient.getFirst(
            RegistryNamespace.class, Collections.singletonMap("name", namespace_name));
  }

  public RegistryData_product getDataProduct() {
    Map<String, String> dp_map =
        Map.of(
            "name",
            this.actualDataProduct_name,
            "namespace",
            this.registryNamespace.get_id().toString(),
            "version",
            version);
    return (RegistryData_product) fileApi.restClient.getFirst(RegistryData_product.class, dp_map);
  }

  ImmutableConfigItem getConfigItem(String dataProduct_name) {
    ImmutableConfigItem configItem =
        this.getConfigItems().stream()
            .filter(ci -> ci.data_product().equals(dataProduct_name))
            .findFirst()
            .orElse(null);

    return configItem;
  }

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
    this.componentMap.entrySet().stream()
        .filter(obj_comp -> obj_comp.getValue().been_used)
        .forEach(
            obj_comp -> {
              obj_comp.getValue().register_me_in_code_run_session(fileApi.code_run_session);
            });
  }

  @Override
  public void close() {
    this.closeFileChannel();
    this.objects_to_registry();
    this.InputsOutputsToCoderun();
  }
}
