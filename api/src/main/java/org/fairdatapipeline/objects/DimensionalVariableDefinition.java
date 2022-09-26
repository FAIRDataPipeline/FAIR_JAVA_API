package org.fairdatapipeline.objects;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.netcdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DimensionVariableDefinition, aka 'Array'.. multidimensional array. */
public class DimensionalVariableDefinition extends VariableDefinition {
  private static final Logger logger = LoggerFactory.getLogger(DimensionalVariableDefinition.class);
  private final @Nonnull NetcdfName[] dimensions;
  @Nonnull VariableName variableName;

  /**
   * @param name
   * @param dataType
   * @param dimensions
   * @param description
   * @param units
   * @param long_name
   */
  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull NetcdfName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(name, dataType, dimensions, description, units, long_name, Collections.emptyMap());
  }

  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull NetcdfName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    this(name, dataType, dimensions, description, units, long_name, optional_attribs, null);
  }

  public DimensionalVariableDefinition(
      @Nonnull LocalVariableDefinition localVarDef,
      NetcdfGroupName groupName,
      String dimensionName) {
    super(
        localVarDef.dataType,
        localVarDef.description,
        localVarDef.units,
        localVarDef.long_name,
        localVarDef.optional_attribs,
        localVarDef.missingValue);
    logger.trace(
        "Creating DimensionalVariableDefinition({}, {}) from LocalVariableDefinition({})",
        localVarDef.getLocalName(),
        groupName,
        localVarDef.getLocalName());
    this.variableName = new VariableName(localVarDef.getLocalName(), groupName);
    this.dimensions = new NetcdfName[] {new NetcdfName(dimensionName)};
  }

  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull NetcdfName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs,
      Object missingValue) {
    super(dataType, description, units, long_name, optional_attribs, missingValue);
    logger.trace(
        "Creating DimensionalVariableDefinition({},{},{},{},{},{})",
        name,
        dataType,
        description,
        units,
        long_name,
        missingValue);
    this.variableName = name;
    this.dimensions = dimensions.clone();
  }

  public @Nonnull VariableName getVariableName() {
    return this.variableName;
  }

  public NetcdfName[] getDimensions() {
    return this.dimensions.clone();
  }
}
