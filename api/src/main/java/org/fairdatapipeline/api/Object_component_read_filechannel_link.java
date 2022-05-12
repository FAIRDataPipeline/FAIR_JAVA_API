package org.fairdatapipeline.api;

import java.nio.file.Path;

public class Object_component_read_filechannel_link extends Object_component_read_filechannel {
  Object_component_read_filechannel_link(Data_product dp) {
    super(dp);
  }

  /**
   * get the filePath to read from; only for whole_object component
   *
   * @return Path the Path of the data object.
   */
  public Path readLink() {
    if (!this.whole_object) {
      throw (new IllegalActionException(
          "You shouldn't try to read directly from a Data Product with named components."));
    }
    this.been_used = true;
    return this.dp.getFilePath();
  }
}
