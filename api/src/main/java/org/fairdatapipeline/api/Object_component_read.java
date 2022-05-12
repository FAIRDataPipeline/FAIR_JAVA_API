package org.fairdatapipeline.api;

import java.util.HashMap;
import java.util.Map;
import org.fairdatapipeline.dataregistry.content.RegistryObject_component;

/**
 * This represents an object_component to read from (or raise issues with) An object_component
 * without a name is the 'whole_object' component. Ideally the user should only read from named
 * components on toml and h5 files, and only read from the 'whole_object' on any other files. This
 * is not enforced at the moment.
 */
abstract class Object_component_read extends Object_component {

  Object_component_read(Data_product dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_read(Data_product dp) {
    super(dp);
  }

  /**
   * retrieves the registryObject_component from the registry (given dp.registryObject.get_id(), and
   * either whole_object=true OR name=component_name)
   *
   * @return the registryObject_component found, or null if none found.
   */
  RegistryObject_component retrieveObject_component() {
    Map<String, String> objcompmap = new HashMap<>();
    objcompmap.put("object", dp.registryObject.get_id().toString());
    if (this.whole_object) objcompmap.put("whole_object", "true");
    else objcompmap.put("name", component_name);
    return (RegistryObject_component)
        dp.coderun.restClient.getFirst(RegistryObject_component.class, objcompmap);
  }

  /**
   * sets this.registryObject_component by retrieving it from the registry.
   *
   * @throws RegistryObjectNotFoundException if the Object_component does not exist.
   */
  protected void populate_component() {
    this.registryObject_component = this.retrieveObject_component();
    if (this.registryObject_component == null) {
      throw (new RegistryObjectNotFoundException(
          "Object Component '"
              + this.component_name
              + "' for Object "
              + this.dp.registryObject.get_id().toString()
              + " not found in registry."));
    }
  }

  void register_me_in_registry() {
    // I am a read component, so I am already registered.
  }

  void register_me_in_code_run() {
    if (this.been_used) this.dp.coderun.addInput(this.registryObject_component.getUrl());
  }
}
