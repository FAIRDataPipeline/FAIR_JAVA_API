package uk.ramp.api;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.List;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.dataregistry.content.*;
import uk.ramp.file.CleanableFileChannel;

/**
 * retrieving and storing the dataRegistry details for a dataproduct; as requested from the API
 * user, and possibly amended by the config.
 */
public class Data_product_read extends Data_product_RW {
  private boolean hash_checked = false;

  public Data_product_read(String dataProduct_name, FileApi fileApi) {
    super(dataProduct_name, fileApi);
  }

  public void populate_dataproduct() {
    // called from the constructor
    this.data_product = this.getDataProduct();
    if (this.data_product == null) {
      throw (new IllegalArgumentException(
          "Trying to read from non-existing data_product "
              + this.actualDataProduct_name
              + "; NS "
              + this.namespace.getName()
              + "; version "
              + version));
    }
    this.fdpObject =
        (FDPObject) fileApi.restClient.get(FDPObject.class, this.data_product.getObject());
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
            fileApi.restClient.get(Storage_location.class, this.fdpObject.getStorage_location());
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
            fileApi.restClient.get(Storage_root.class, this.storage_location.getStorage_root());
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
  }

  public List<ImmutableConfigItem> getConfigItems() {
    return fileApi.config.readItems();
  }

  public String getDefaultNamespace_name() {
    return this.fileApi.config.run_metadata().default_input_namespace().orElse("");
  }

  public Namespace getNamespace(String namespace_name) {
    // for a READ dp we must have the namespace from the config or we will have to give up
    Namespace ns = super.getNamespace(namespace_name);
    if (ns == null) {
      throw (new IllegalArgumentException("can't find the namespace " + namespace_name));
    }
    return ns;
  }

  public ImmutableConfigItem getConfigItem(String dataProduct_name) {
    ImmutableConfigItem configItem = super.getConfigItem(dataProduct_name);
    if (configItem == null) {
      throw (new IllegalArgumentException(
          "dataProduct " + dataProduct_name + " not found in config"));
    }
    return configItem;
  }

  private void check_hash() {
    // if(this.fileApi.config.run_metadata().)
    // String hash = fileApi.hasher.fileHash(this.filePath.toString());
    // not sure if we're supposed to check the hash

  }

  private void executeOnCloseFileHandleDP() {
    System.out.println("executeOnCloseFileHandleDP() (DP READ).. " + this.actualDataProduct_name);
  }

  protected CleanableFileChannel getFilechannel() throws IOException {
    this.been_used = true;
    Runnable onClose = this::executeOnCloseFileHandleDP;
    if (!hash_checked) this.check_hash();
    if (this.filechannel == null) {
      System.out.println("filePath: " + this.filePath.toString());
      this.filechannel = new CleanableFileChannel(FileChannel.open(this.filePath, READ), onClose);
    } else {
      if (!this.filechannel.isOpen()) {
        System.out.println("re-opening the filechannel");
        this.filechannel = new CleanableFileChannel(FileChannel.open(this.filePath, READ), onClose);
      }
    }
    return this.filechannel;
  }

  public Object_component_read getComponent(String component_name) {
    System.out.println("getComponent()");
    if (componentMap.containsKey(component_name))
      return (Object_component_read) componentMap.get(component_name);
    Object_component_read dc = new Object_component_read(this, component_name);
    System.out.println("getComponent() after creating dc");
    componentMap.put(component_name, dc);
    return dc;
  }

  protected void objects_to_registry() {
    // this is called upon close; but since this is a READ dp, it won't have a DP, Stolo, fdpObj, or
    // any new components to register
  }
}
