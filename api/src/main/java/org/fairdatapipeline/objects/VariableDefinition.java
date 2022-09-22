package org.fairdatapipeline.objects;

import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;

/**
 * VariableDefinition is the parent class of CoordinateVariableDefinition and
 * DimensionalVariableDefinition. It has the full definition of a variable, only lacking 'dimensions' or 'size'.
 * It is used in TableDefinition to define the columns of the table.
 */
public abstract class VariableDefinition extends NetcdfDataComponentDefinition {
  final @Nonnull NetcdfDataType dataType;
  final @Nonnull String units;

  final Object missingValue;

  VariableDefinition(
      @Nonnull NetcdfDataType dataType,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String> optional_attribs) {
    super(description, long_name, optional_attribs);
    this.dataType = dataType;
    this.units = units;
    this.missingValue = null;
  }
  VariableDefinition(
          @Nonnull NetcdfDataType dataType,
          @Nonnull String description,
          @Nonnull String units,
          @Nonnull String long_name,
          @Nonnull Map<String, String> optional_attribs,
          Object missingValue) {
    super(description, long_name, optional_attribs);
    this.dataType = dataType;
    this.units = units;
    this.missingValue = missingValue;
  }

    public @Nonnull NetcdfDataType getDataType() {
    return dataType;
  }

  public Object getMissingValue() {
    return missingValue;
  }

  public @Nonnull String getUnits() {
    return units;
  }

}
