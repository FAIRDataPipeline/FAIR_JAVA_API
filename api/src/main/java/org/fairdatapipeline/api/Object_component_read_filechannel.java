package org.fairdatapipeline.api;

import java.io.IOException;
import org.fairdatapipeline.file.CleanableFileChannel;

abstract class Object_component_read_filechannel extends Object_component_read {
  Object_component_read_filechannel(Data_product dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_read_filechannel(Data_product dp) {
    super(dp);
  }

  /**
   * get the CleanableFileChannel to read directly from the file. only for whole_object component.
   *
   * @return CleanableFileChannel the filechannel to read from.
   * @throws IOException if the file can't be opened.
   */
  public CleanableFileChannel readFileChannel() throws IOException {
    if (!this.whole_object) {
      throw (new IllegalActionException(
          "You shouldn't try to read directly from a Data Product with named components."));
    }
    this.been_used = true;
    return this.getFileChannel();
  }

  CleanableFileChannel getFileChannel() throws IOException {
    this.been_used = true;
    return ((Data_product_read_filechannel) this.dp).getFilechannel();
  }
}
