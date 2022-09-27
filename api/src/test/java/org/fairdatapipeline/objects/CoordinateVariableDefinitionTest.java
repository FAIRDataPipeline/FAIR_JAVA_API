package org.fairdatapipeline.objects;

import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CoordinateVariableDefinitionTest {

  @Test
  void test_constructor() {
    CoordinateVariableDefinition c =
        new CoordinateVariableDefinition(
            new VariableName("bla", ""),
            NetcdfDataType.INT,
            CoordinateVariableDefinition.UNLIMITED,
            "a little description",
            "cm",
            "a very long name");
    Assertions.assertTrue(c.isUnlimited());
  }

  @Test
  void test_constructor2() {
    CoordinateVariableDefinition c =
        new CoordinateVariableDefinition(
            new VariableName("bla", ""),
            new int[] {1, 2, 3},
            "a little description",
            "cm",
            "a very long name");
    Assertions.assertEquals(3, c.getSize());
  }
}
