package org.fairdatapipeline.api;

import java.nio.file.Path;

public class Object_component_write_filechannel_link extends Object_component_write_filechannel {
  Object_component_write_filechannel_link(Data_product_write_link dp) {
    super(dp);
  }

  /**
   * get the filePath to write; only for whole_object component
   *
   * @return Path the Path of the data object.
   */
  public Path writeLink() {
    this.been_used = true;
    return this.dp.getFilePath();
  }

  void write_preset_data() {
    // no preset data to write.
  }
}
