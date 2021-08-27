package org.fairdatapipeline.objects;

import org.fairdatapipeline.file.CleanableFileChannel;

public interface StandardTableDataReader {
  StandardTable readTable(CleanableFileChannel fileChannel, String component);
}
