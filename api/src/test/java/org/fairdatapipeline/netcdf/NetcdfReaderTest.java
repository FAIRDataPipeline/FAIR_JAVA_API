package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.util.Arrays;

import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.VariableDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ucar.nc2.Variable;

class NetcdfReaderTest {
  private static final String read_int_resource = "/netcdf/test_build_prepare_write_INT.nc";

  @Test
  void test_read_int_ardef() throws IOException {
    String filename = getClass().getResource(read_int_resource).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    VariableDefinition ardef = reader.getArray("aap/noot/mies/temperature");
    Assertions.assertEquals(NetcdfDataType.INT, ardef.getDataType());
    Assertions.assertEquals("a test dataset with temperatures in 2d space", ardef.getDescription());
    Assertions.assertEquals("C", ardef.getUnits());
    Assertions.assertEquals("surface temperature", ardef.getLong_name());
    Assertions.assertEquals(org.fairdatapipeline.objects.DimensionalVariableDefinition.class, ardef.getClass());
    DimensionalVariableDefinition dimvar = (DimensionalVariableDefinition) ardef;
    Assertions.assertEquals(new VariableName("aap/noot/mies/X"), dimvar.getDimensions()[0]);
    Assertions.assertEquals(new VariableName("aap/noot/mies/Y"), dimvar.getDimensions()[1]);
  }

  @Test
  void test_read_int_Xdimdef() throws IOException {
    String filename = getClass().getResource(read_int_resource).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    VariableDefinition ardef = reader.getArray("aap/noot/mies/X");
    Assertions.assertEquals(NetcdfDataType.INT, ardef.getDataType());
    Assertions.assertEquals(org.fairdatapipeline.objects.CoordinateVariableDefinition.class, ardef.getClass());
    Assertions.assertEquals(
        "the x-axis is measured in along the length of my football pitch; (0,0) is the southwest corner.",
        ardef.getDescription());
    Assertions.assertEquals("m", ardef.getUnits());
    Assertions.assertEquals("", ardef.getLong_name());
  }

  @Test
  void test_read_int_data() throws IOException {
    String filename = getClass().getResource(read_int_resource).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    Variable v = reader.getVariable("aap/noot/mies/temperature");
    NumericalArray na = reader.read(v);
    Assertions.assertEquals(1, na.as2DArray()[0][0]);
    Assertions.assertTrue(Arrays.equals(new Number[] {1,2,3}, na.as2DArray()[0]));
    Assertions.assertTrue(Arrays.equals(new Number[] {11,12,13}, na.as2DArray()[1]));

  }
}
