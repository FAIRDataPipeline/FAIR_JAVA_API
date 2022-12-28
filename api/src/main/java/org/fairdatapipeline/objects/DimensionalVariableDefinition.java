package org.fairdatapipeline.objects;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.netcdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DimensionVariableDefinition, aka 'Array'.. multidimensional array.
 *
 * <p>The dimensions are defined by linking to CoordinateVariables, with a link only to the 'local'
 * NetcdfName part of the variable name; this means the name COULD be ambiguous, as there could be
 * different variables with the same name, located in the current group and its parent groups.
 */
public class DimensionalVariableDefinition extends VariableDefinition {
  private static final Logger LOGGER = LoggerFactory.getLogger(DimensionalVariableDefinition.class);
  private final @Nonnull Dimension[] dimensions;
  @Nonnull VariableName variableName;

  /**
   * @param name Name and Group of this Variable
   * @param dataType dataType of the Variable
   * @param dimensions names/sizes of the dimensions. (no group names given; the dimensions used will be
   *     the nearest dimension with the given name)
   * @param description a short description of the variable. (ie. "temperature at ground level")
   * @param units the units used for this variable. (ie. "C" or "K" for temperature)
   * @param long_name a more descriptive name than can be fitted in the VariableName (could be used
   *     on a plot axis)
   */
  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull Dimension[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(name, dataType, dimensions, description, units, long_name, Collections.emptyMap());
  }

  /**
   * @param name Name and Group of this Variable
   * @param dataType dataType of the Variable
   * @param dimensions names of the dimensions. (no group names given; the dimensions used will be
   *     the nearest dimension with the given name)
   * @param description a short description of the variable. (ie. "temperature at ground level")
   * @param units the units used for this variable. (ie. "C" or "K" for temperature)
   * @param long_name a more descriptive name than can be fitted in the VariableName (could be used
   *     on a plot axis)
   * @param optional_attribs extra metadata attributes to be added to this variable. the values are
   *     String[] as we mostly want to add a single String but sometimes more than one String.
   */
  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull Dimension[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    this(name, dataType, dimensions, description, units, long_name, optional_attribs, null);
  }

  /**
   * To turn a localVarDef (local variable
   *
   * @param localVarDef
   * @param groupName
   * @param dimensionName
   */
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
    LOGGER.trace(
        "Creating DimensionalVariableDefinition({}, {}) from LocalVariableDefinition({})",
        localVarDef.getLocalName(),
        groupName,
        localVarDef.getLocalName());
    this.variableName = new VariableName(localVarDef.getLocalName(), groupName);
    this.dimensions = new Dimension[] {new Dimension(dimensionName)};
  }

  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull Dimension[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs,
      Object missingValue) {
    super(dataType, description, units, long_name, optional_attribs, missingValue);
    LOGGER.trace(
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

  public Dimension[] getDimensions() {
    return this.dimensions.clone();
  }
}
