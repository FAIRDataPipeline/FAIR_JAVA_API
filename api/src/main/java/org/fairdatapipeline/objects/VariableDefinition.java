package org.fairdatapipeline.objects;

import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;

/**
 * VariableDefinition is the abstract parent class of CoordinateVariableDefinition and
 * DimensionalVariableDefinition
 */
abstract class VariableDefinition {
  final @Nonnull VariableName variableName;
  final @Nonnull NetcdfDataType dataType;
  final @Nonnull String description;
  final @Nonnull String units;
  final @Nonnull String long_name;
  final @Nonnull Map<String, String> optional_attribs;

  VariableDefinition(
      @Nonnull VariableName variableName,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String> optional_attribs) {
    this.variableName = variableName;
    this.dataType = dataType;
    this.description = description;
    this.units = units;
    this.long_name = long_name;
    this.optional_attribs = optional_attribs;
  }

  public @Nonnull VariableName getVariableName() {
    return variableName;
  }

  public @Nonnull NetcdfDataType getDataType() {
    return dataType;
  }

  public @Nonnull String getDescription() {
    return description;
  }

  public @Nonnull String getUnits() {
    return units;
  }

  public @Nonnull String getLong_name() {
    return long_name;
  }

  public @Nonnull Map<String, String> getOptional_attribs() {
    return optional_attribs;
  }
}
