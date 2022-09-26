package org.fairdatapipeline.objects;

import java.util.Collections;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.NetcdfName;
import org.fairdatapipeline.netcdf.VariableName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DimensionalVariableTest {

  /**
   * we can only reference a coordinatevariable/dimension existing in our current group or one of
   * its parent groups.
   */
  @Test
  void test_prepare_dimensionalvar() {
    String dimname = "name";
    String varname = "varname";
    DimensionalVariableDefinition dv =
        new DimensionalVariableDefinition(
            new VariableName(varname, ""),
            NetcdfDataType.INT,
            new NetcdfName[] {new NetcdfName(dimname)},
            "",
            "",
            "",
            Collections.singletonMap("optional_attrib", new String[] {"value1", "value2"}));
    Assertions.assertEquals(dimname, dv.getDimensions()[0].getName());
    Assertions.assertEquals("value1", dv.getOptional_attribs().get("optional_attrib")[0]);
  }
}
