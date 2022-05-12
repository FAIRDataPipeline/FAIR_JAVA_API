package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import org.fairdatapipeline.file.CleanableFileChannel;

abstract class Data_product_write_filechannel extends Data_product_write {
  CleanableFileChannel filechannel;

  Data_product_write_filechannel(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  Data_product_write_filechannel(String dataProduct_name, Coderun coderun, String extension) {
    super(dataProduct_name, coderun, extension);
  }

  private void executeOnCloseFileHandleDP() {
    this.do_hash();
  }

  CleanableFileChannel getFilechannel() throws IOException {
    this.been_used = true;
    Runnable onClose = this::executeOnCloseFileHandleDP;
    if (this.filechannel == null) {
      if (!this.getFilePath().getParent().toFile().exists()) {
        Files.createDirectories(this.getFilePath().getParent());
      }
      this.filechannel =
          new CleanableFileChannel(
              FileChannel.open(this.getFilePath(), CREATE_NEW, WRITE), onClose);
    } else {
      if (!this.filechannel.isOpen()) {
        this.filechannel =
            new CleanableFileChannel(FileChannel.open(this.getFilePath(), APPEND, WRITE), onClose);
      }
    }
    this.is_hashed = false;
    return this.filechannel;
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
