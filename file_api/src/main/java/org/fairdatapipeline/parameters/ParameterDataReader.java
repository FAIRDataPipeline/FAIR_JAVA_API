package org.fairdatapipeline.parameters;

import org.fairdatapipeline.file.CleanableFileChannel;

public interface ParameterDataReader {

  ReadComponent read(CleanableFileChannel fileChannel, String component);
}
