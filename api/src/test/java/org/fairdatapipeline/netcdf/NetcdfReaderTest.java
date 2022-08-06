package org.fairdatapipeline.netcdf;

import java.io.IOException;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NetcdfReaderTest {

  @Test
  void test_read_int_ardef() throws IOException {
    String resourceName = "/netcdf/test_build_prepare_write_INT.nc";
    String filename = getClass().getResource(resourceName).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    DimensionalVariableDefinition ardef = reader.getArray("aap/noot/mies/temperature");
    Assertions.assertEquals(NetcdfDataType.INT, ardef.getDataType());
    Assertions.assertEquals("a test dataset with temperatures in 2d space", ardef.getDescription());
    Assertions.assertEquals("C", ardef.getUnits());
    Assertions.assertEquals("surface temperature", ardef.getLong_name());
    Assertions.assertEquals(new VariableName("aap/noot/mies/X"), ardef.getDimensions()[0]);
    Assertions.assertEquals(new VariableName("aap/noot/mies/Y"), ardef.getDimensions()[1]);
  }

  @Test
  void test_read_int_Xdimdef() throws IOException {
    String resourceName = "/netcdf/test_build_prepare_write_INT.nc";
    String filename = getClass().getResource(resourceName).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    DimensionalVariableDefinition ardef = reader.getArray("aap/noot/mies/X");
    Assertions.assertEquals(NetcdfDataType.INT, ardef.getDataType());
    Assertions.assertEquals(
        "the x-axis is measured in along the length of my football pitch; (0,0) is the southwest corner.",
        ardef.getDescription());
    Assertions.assertEquals("m", ardef.getUnits());
    Assertions.assertEquals("", ardef.getLong_name());
  }
}
