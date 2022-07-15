package org.fairdatapipeline.netcdf;

import org.junit.jupiter.api.Test;

public class NetcdfReaderTest {

  @Test
  void test_read_int() {
    String resourceName = "/netcdf/test_build_prepare_write_INT.nc";
    String filename = getClass().getResource(resourceName).toString();
    NetcdfReader reader = new NetcdfReader(filename);
  }
}
