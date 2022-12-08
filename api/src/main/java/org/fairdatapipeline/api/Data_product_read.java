package org.fairdatapipeline.api;

import java.util.List;
import org.fairdatapipeline.config.ConfigException;
import org.fairdatapipeline.config.ImmutableConfigItem;
import org.fairdatapipeline.dataregistry.content.RegistryObject;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_location;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_root;

/**
 * Data_product_read_* are created by Coderun.
 *
 * <p>Upon {@link Coderun#close()} it will register its components in the coderun.
 */
abstract class Data_product_read extends Data_product {
  Data_product_read(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  /**
   * requires: this.registryData_product is set this.registryNamespace is set
   * this.actualDataProduct_name is set
   */
  void populate_data_product() {
    // called from the constructor
    this.registryData_product = this.getRegistryData_product();
    if (this.registryData_product == null) {
      throw (new RegistryObjectNotFoundException(
          "Trying to read from non-existing data_product "
              + this.actualDataProduct_name
              + "; NS "
              + this.registryNamespace.getName()
              + "; version "
              + version));
    }
    this.registryObject =
        (RegistryObject)
            coderun.restClient.get(RegistryObject.class, this.registryData_product.getObject());
    if (this.registryObject == null) {
      throw (new RegistryObjectNotFoundException(
          "couldn't retrieve the fdpObject for this READ dp "
              + this.givenDataProduct_name
              + " ("
              + this.actualDataProduct_name
              + ")"));
    }
    this.registryStorage_location =
        (RegistryStorage_location)
            coderun.restClient.get(
                RegistryStorage_location.class, this.registryObject.getStorage_location());
    if (this.registryStorage_location == null) {
      throw (new RegistryObjectNotFoundException(
          "Couldn't retrieve the StorageLocation for this READ dp "
              + this.givenDataProduct_name
              + " ("
              + this.actualDataProduct_name
              + ")"));
    }
    this.registryStorage_root =
        (RegistryStorage_root)
            coderun.restClient.get(
                RegistryStorage_root.class, this.registryStorage_location.getStorage_root());
    if (this.registryStorage_root == null) {
      throw (new RegistryObjectNotFoundException(
          "Couldn't retrieve the StorageRoot for this READ dp "
              + this.givenDataProduct_name
              + " ("
              + this.actualDataProduct_name
              + ")"));
    }
  }

  List<ImmutableConfigItem> getConfigItems() {
    return coderun.config.readItems();
  }

  String getDefaultNamespace_name() {
    return this.coderun.config.run_metadata().default_input_namespace().orElse("");
  }

  /**
   * getConfigItem searches the ConfigItems for this.givenDataProduct_name.
   *
   * @return the configItem that matches this.givenDataProduct_name
   * @throws ConfigException if no configItem matches.
   */
  @Override
  ImmutableConfigItem getConfigItem() {
    ImmutableConfigItem configItem = super.getConfigItem();
    if (configItem == null) {
      throw (new ConfigException(
          "dataProduct " + this.givenDataProduct_name + " not found in config"));
    }
    return configItem;
  }

  void do_hash() {
    // no hashing to be done for read
  }

  void stolo_obj_and_dp_to_registry() {
    // nothing to post to registry for read
  }
}
