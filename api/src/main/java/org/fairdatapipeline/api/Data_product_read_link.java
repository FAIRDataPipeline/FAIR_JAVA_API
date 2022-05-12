package org.fairdatapipeline.api;

public class Data_product_read_link extends Data_product_read_filechannel {
  Data_product_read_link(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  /**
   * Obtain an Object_component (whole_object) for reading.
   *
   * @return this.whole_obj_oc the Object_component_read_filechannel_link
   */
  public Object_component_read_filechannel_link getComponent() {
    if (this.whole_obj_oc == null)
      this.whole_obj_oc = new Object_component_read_filechannel_link(this);
    return (Object_component_read_filechannel_link) this.whole_obj_oc;
  }
}
