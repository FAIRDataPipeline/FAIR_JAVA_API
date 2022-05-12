package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.nio.channels.FileChannel;
import org.fairdatapipeline.file.CleanableFileChannel;

abstract class Data_product_read_filechannel extends Data_product_read {
  CleanableFileChannel filechannel;

  Data_product_read_filechannel(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  CleanableFileChannel getFilechannel() throws IOException {
    this.been_used = true;
    Runnable onClose = this::executeOnCloseFileHandleDP;
    if (this.filechannel == null) {
      this.filechannel =
          new CleanableFileChannel(FileChannel.open(this.getFilePath(), READ), onClose);
    } else {
      if (!this.filechannel.isOpen()) {
        this.filechannel =
            new CleanableFileChannel(FileChannel.open(this.getFilePath(), READ), onClose);
      }
    }
    return this.filechannel;
  }

  private void executeOnCloseFileHandleDP() {
    // don't need to Hash READ objects
  }

  void closeFileChannel() {
    if (this.filechannel != null) {
      this.filechannel.close();
      this.filechannel = null;
    }
  }

  @Override
  public void close() {
    closeFileChannel();
    super.close();
  }
}
