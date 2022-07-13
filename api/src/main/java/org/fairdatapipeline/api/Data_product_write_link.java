package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.*;

import org.fairdatapipeline.dataregistry.content.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data_product_write_link is created by Coderun: {@link Coderun#get_dp_for_write_link(String,
 * String)}
 *
 * <p>Upon {@link Coderun#close()} it will register itself and its components in the registry, and
 * then register its components in the coderun.
 */
public class Data_product_write_link extends Data_product_write_filechannel {

  Data_product_write_link(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  Data_product_write_link(String dataProduct_name, Coderun coderun, String extension) {
    super(dataProduct_name, coderun, extension);
  }

  /**
   * Obtain an Object_component_write_filechannel_link (whole_object) for writing.
   *
   * @return the Object_component_write_filechannel_link
   */
  public Object_component_write_filechannel_link getComponent() {
    if (this.whole_obj_oc == null)
      this.whole_obj_oc = new Object_component_write_filechannel_link(this);
    return (Object_component_write_filechannel_link) this.whole_obj_oc;
  }
}
