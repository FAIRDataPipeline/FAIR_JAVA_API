package org.fairdatapipeline.objects;

import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.netcdf.DimensionName;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class DimensionalVariableTest {

    /** we can only reference a coordinatevariable/dimension existing in our current group or one of its parent groups.
     *
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test_prepare_dimensionalvar_badref() throws IOException {
        Assertions.assertThrows(IllegalActionException.class, () -> {new DimensionalVariableDefinition(
                new VariableName("name", ""),
                NetcdfDataType.INT,
                new DimensionName[] {new VariableName("name", "subgroup")},
                "",
                "",
                ""
        );});
    }
}
