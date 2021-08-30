package org.fairdatapipeline.parameters;

import org.fairdatapipeline.file.CleanableFileChannel;

public interface ParameterDataWriter {

  void write(CleanableFileChannel fileChannel, String component, Component data);
}
