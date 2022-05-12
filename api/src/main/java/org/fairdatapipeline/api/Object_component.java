package org.fairdatapipeline.api;

import org.fairdatapipeline.dataregistry.content.RegistryObject_component;

/**
 * This represents an object_component to read from or write to (or raise issues with) An
 * object_component without a name is the 'whole_object' component.
 */
abstract class Object_component {
  /** component_name is the name of this component; if not given we will use "whole_object" */
  final String component_name;
  /** is this a 'whole_object' or a named part for a toml or netcdf file? */
  final boolean whole_object;
  /** the Data_product we are part of */
  final Data_product dp;
  /** registryObject_component is the */
  RegistryObject_component registryObject_component;

  boolean been_used = false;

  Object_component(Data_product dp, String component_name) {
    this(dp, component_name, false);
  }

  Object_component(Data_product dp) {
    this(dp, "whole_object", true);
  }

  Object_component(Data_product dp, String component_name, boolean whole_object) {
    this.dp = dp;
    this.whole_object = whole_object;
    this.component_name = component_name;
    this.populate_component();
  }

  /**
   * raise an issue with this component
   *
   * @param description the text description of this issue
   * @param severity Integer - higher means more severe
   */
  public void raise_issue(String description, Integer severity) {
    Issue i = this.dp.coderun.raise_issue(description, severity);
    i.add_components(this);
  }

  /**
   * populate_component() - sets this.registryObject_component - for READ component: retrieve from
   * registry - for WRITE component: create an empty new registryObject_component.
   */
  abstract void populate_component();

  /** POST this registryObject_component to the registry, unless it's not been used */
  abstract void register_me_in_registry();

  /** Add this registryObject_component to the Coderun inputs/outputs. */
  abstract void register_me_in_code_run();
}
