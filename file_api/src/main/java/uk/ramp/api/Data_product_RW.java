package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;

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
  protected Namespace namespace;
  protected String namespace_name;
  protected String extension;
  protected String version;
  protected String description;
  protected Data_product data_product;
  protected FDPObject fdpObject;
  protected Storage_location storage_location;
  protected Storage_root storage_root;
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
    System.out.println("Data_product_RW() constructor");
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
    this.description = configItem.description().orElse("");
    this.version = configItem.use().version();
    this.namespace = this.getNamespace(this.namespace_name);
    this.populate_dataproduct();
  }

  /*
   * this should populate the main data fields: data_product, storage_location and fdpObject
   * for READ: by reading these from the registry
   * for WRITE: by preparing empty ones that can later by posted to registry
   */
  public abstract void populate_dataproduct();

  public abstract List<ImmutableConfigItem> getConfigItems();

  public abstract String getDefaultNamespace_name();

  public Namespace getNamespace(String namespace_name) {
    return (Namespace)
        fileApi.restClient.getFirst(
            Namespace.class, Collections.singletonMap("name", namespace_name));
  }

  public Data_product getDataProduct() {
    Map<String, String> dp_map =
        Map.of(
            "name",
            this.actualDataProduct_name,
            "namespace",
            this.namespace.get_id().toString(),
            "version",
            version);
    return (Data_product) fileApi.restClient.getFirst(Data_product.class, dp_map);
  }

  public ImmutableConfigItem getConfigItem(String dataProduct_name) {
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
  protected abstract CleanableFileChannel getFilechannel() throws IOException;

  void closeFileChannel() {
    if (this.filechannel != null) {
      System.out.println("closing the filechannel for dp " + this.filePath);
      this.filechannel.close();
      this.filechannel = null;
    }
  }

  // public abstract Object_component_write getComponent(String component_name);

  protected abstract void objects_to_registry();

  protected void InputsOutputsToCoderun() {
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
