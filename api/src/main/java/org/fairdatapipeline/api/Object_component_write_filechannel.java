package org.fairdatapipeline.api;

import java.io.IOException;
import org.fairdatapipeline.file.CleanableFileChannel;

abstract class Object_component_write_filechannel extends Object_component_write {
  Object_component_write_filechannel(Data_product_write_filechannel dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_write_filechannel(Data_product_write_filechannel dp) {
    super(dp);
  }

  /**
   * get the CleanableFileChannel to write directly to the file. only for whole_object component.
   *
   * @return CleanableFileChannel the filechannel to write to.
   * @throws IOException if the file can't be opened.
   */
  public CleanableFileChannel writeFileChannel() throws IOException {
    if (!this.whole_object) {
      throw (new IllegalActionException(
          "You shouldn't try to write directly to a Data Product with named components."));
    }
    this.been_used = true;
    return this.getFileChannel();
  }

  CleanableFileChannel getFileChannel() throws IOException {
    this.been_used = true;
    return ((Data_product_write_filechannel) this.dp).getFilechannel();
  }
}
