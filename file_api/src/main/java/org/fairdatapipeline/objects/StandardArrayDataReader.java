package org.fairdatapipeline.objects;

import org.fairdatapipeline.file.CleanableFileChannel;

public interface StandardArrayDataReader {
  NumericalArray read(CleanableFileChannel fileChannel, String component);
}
