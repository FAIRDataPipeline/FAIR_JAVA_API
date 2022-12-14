package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.util.Objects;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.VariableDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ucar.nc2.Variable;

class NetcdfReaderTest {
  private static final String read_int_resource = "/netcdf/test_build_prepare_write_INT.nc";

  /**
   * test only the reading of metadata (of the 'temperature' variable). does not test reading of
   * data.
   *
   * @throws IOException
   */
  @Test
  void test_read_int_ardef() throws IOException {
    String filename = Objects.requireNonNull(getClass().getResource(read_int_resource)).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    VariableDefinition ardef = reader.getArray("aap/noot/mies/temperature");
    Assertions.assertEquals(NetcdfDataType.INT, ardef.getDataType());
    Assertions.assertEquals("a test dataset with temperatures in 2d space", ardef.getDescription());
    Assertions.assertEquals("C", ardef.getUnits());
    Assertions.assertEquals("surface temperature", ardef.getLong_name());
    Assertions.assertEquals(
        org.fairdatapipeline.objects.DimensionalVariableDefinition.class, ardef.getClass());
    DimensionalVariableDefinition dimvar = (DimensionalVariableDefinition) ardef;
    Assertions.assertEquals(new NetcdfName("X"), dimvar.getDimensions()[0]);
    Assertions.assertEquals(new NetcdfName("Y"), dimvar.getDimensions()[1]);
  }

  /**
   * test only the reading of metadata (of the 'X' variable). does not test reading of data.
   *
   * @throws IOException
   */
  @Test
  void test_read_int_Xdimdef() throws IOException {
    String filename = Objects.requireNonNull(getClass().getResource(read_int_resource)).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    VariableDefinition ardef = reader.getArray("aap/noot/mies/X");
    Assertions.assertEquals(NetcdfDataType.INT, ardef.getDataType());
    Assertions.assertEquals(
        org.fairdatapipeline.objects.CoordinateVariableDefinition.class, ardef.getClass());
    Assertions.assertEquals(
        "the x-axis is measured in along the length of my football pitch; (0,0) is the southwest corner.",
        ardef.getDescription());
    Assertions.assertEquals("m", ardef.getUnits());
    Assertions.assertEquals("", ardef.getLong_name());
  }

  /**
   * test reading the whole array
   *
   * @throws IOException
   */
  @Test
  void test_read_int_data() throws IOException {
    String filename = Objects.requireNonNull(getClass().getResource(read_int_resource)).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    Variable v = reader.getVariable("aap/noot/mies/temperature");
    NumericalArray na = reader.read(v);
    Assertions.assertEquals(1, na.as2DArray()[0][0]);
    Assertions.assertArrayEquals(new Number[] {1, 2, 3}, na.as2DArray()[0]);
    Assertions.assertArrayEquals(new Number[] {11, 12, 13}, na.as2DArray()[1]);
  }

  /**
   * test reading the array row by row
   *
   * @throws IOException
   */
  @Test
  void test_read_int_data_by_row() throws IOException {
    String filename = Objects.requireNonNull(getClass().getResource(read_int_resource)).toString();
    NetcdfReader reader = new NetcdfReader(filename);
    Variable v = reader.getVariable("aap/noot/mies/temperature");
    int[] ori = new int[] {0, 0};
    int[] shape = new int[] {1, 3};
    for (int i = 0; i <= 1; i++) {
      ori[0] = i;
      NumericalArray na = reader.read(v, ori, shape);
      if (i == 0) Assertions.assertArrayEquals(new Number[] {1, 2, 3}, na.as2DArray()[0]);
      else Assertions.assertArrayEquals(new Number[] {11, 12, 13}, na.as2DArray()[0]);
    }
  }
}
