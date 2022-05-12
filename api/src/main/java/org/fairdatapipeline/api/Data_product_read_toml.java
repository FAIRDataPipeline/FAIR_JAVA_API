package org.fairdatapipeline.api;

import java.util.Objects;

public class Data_product_read_toml extends Data_product_read_filechannel {
  Data_product_read_toml(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  /**
   * Obtain an Object_component for reading.
   *
   * @param component_name the name of the object component.
   * @return the Object_component_read_filechannel_toml object.
   */
  public Object_component_read_filechannel_toml getComponent(String component_name) {
    if (componentMap.containsKey(component_name))
      return (Object_component_read_filechannel_toml) componentMap.get(component_name);
    Object_component_read_filechannel_toml dc;
    dc = new Object_component_read_filechannel_toml(this, Objects.requireNonNull(component_name));
    componentMap.put(component_name, dc);
    return dc;
  }
}
