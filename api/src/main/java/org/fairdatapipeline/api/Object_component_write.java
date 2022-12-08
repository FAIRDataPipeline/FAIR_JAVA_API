package org.fairdatapipeline.api;

import java.util.Map;
import org.fairdatapipeline.dataregistry.content.RegistryObject_component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents an object_component to write to (or raise issues with) An object_component
 * without a name is the 'whole_object' component. Ideally the user should only write to named
 * components on toml and h5 files, and only write to the 'whole_object' on any other files. This is
 * not enforced at the moment. You should only ever either write to the whole_object, OR to the
 * named components, not both. This also is not enforced at the moment.
 */
abstract class Object_component_write extends Object_component {
  private static final Logger LOGGER = LoggerFactory.getLogger(Object_component_write.class);

  Object_component_write(Data_product dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_write(Data_product dp) {
    super(dp);
  }

  protected void populate_component() {
    this.registryObject_component = new RegistryObject_component(component_name);
  }

  void register_me_in_code_run() {
    if (this.been_used) this.dp.coderun.addOutput(this.registryObject_component.getUrl());
  }

  void register_me_in_registry() {
    if (!been_used) {
      LOGGER.trace("unused Object_component_write not being stored in registry.");
      return; // don't register a component unless it has been written to
    }
    if (this.whole_object) {
      LOGGER.trace("storing Object_component_write (whole_object)");
      Map<String, String> find_whole_object =
          Map.of("object", dp.registryObject.get_id().toString(), "whole_object", "true");
      RegistryObject_component objComponent =
          (RegistryObject_component)
              dp.coderun.restClient.getFirst(RegistryObject_component.class, find_whole_object);
      if (objComponent == null) {
        throw (new RegistryObjectNotFoundException(
            "Can't find the 'whole_object' component for obj " + dp.registryObject.get_id()));
      }
      this.registryObject_component = objComponent;
      // we store the found 'whole obj' component as the object_component of
      // the referenced Object_component_write so that this can later be stored as a
      // code_run output.
    } else {
      LOGGER.trace("storing Object_component_write (not whole)");
      // component != whole_object
      this.registryObject_component.setObject(dp.registryObject.getUrl());
      RegistryObject_component objComponent =
          (RegistryObject_component) dp.coderun.restClient.post(this.registryObject_component);
      if (objComponent == null) {
        throw (new RegistryException(
            "Failed to create in registry: object component "
                + this.component_name
                + " ("
                + dp.registryObject.get_id()
                + ")"));
      }
      this.registryObject_component = objComponent;
      // store the created object component so that this can later be stored as a code_run
      // output
    }
  }

  abstract void write_preset_data();
}
