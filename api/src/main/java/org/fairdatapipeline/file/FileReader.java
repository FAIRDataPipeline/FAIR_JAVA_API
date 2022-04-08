package org.fairdatapipeline.file;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.commons.io.IOUtils;

public class FileReader {
  public String read(String fileName) {
    try (java.io.FileReader fileReader = new java.io.FileReader(fileName)) {
      return IOUtils.toString(fileReader);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
