package org.fairdatapipeline.api;

import java.util.Objects;

/**
 * Data_product_write is created by Coderun: {@link Coderun#get_dp_for_write_toml(String)}
 *
 * <p>Upon {@link Coderun#close()} it will register itself and its components in the registry, and
 * then register its components in the coderun.
 */
public class Data_product_write_toml extends Data_product_write_filechannel {

  Data_product_write_toml(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  /**
   * Obtain an Object_component for writing.
   *
   * @param component_name the name of the object component.
   * @return the Object_component_write_filechannel_toml object.
   */
  public Object_component_write_filechannel_toml getComponent(String component_name) {
    if (componentMap.containsKey(component_name))
      return (Object_component_write_filechannel_toml) componentMap.get(component_name);
    Object_component_write_filechannel_toml dc;
    dc = new Object_component_write_filechannel_toml(this, Objects.requireNonNull(component_name));
    componentMap.put(component_name, dc);
    return dc;
  }
}
