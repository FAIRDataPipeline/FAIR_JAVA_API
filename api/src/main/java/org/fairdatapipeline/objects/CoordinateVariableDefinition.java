package org.fairdatapipeline.objects;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;

/**
 * CoordinateVariableDefinition is used to create a variable that describes one of the dimensions of
 * a DimensionalVariableDefinition.
 *
 * <p>A CoordinateVariable is represented in the netCDF file by a variable and a dimension with the
 * same name, in the same group.
 */
public class CoordinateVariableDefinition extends VariableDefinition {
  private final int size;
  public static final int UNLIMITED = 0;
  private final @Nullable Object values;

  @Nonnull VariableName variableName;

  /**
   * if we don't supply values, we need to set size and type. size can be
   * CoordinateVariable.UNLIMITED
   *
   * @param variableName Name and Group of this Coordinate Variable
   * @param dataType dataType of the Variable
   * @param size length of the Dimension. (or UNLIMITED)
   * @param description a short description of the variable. (ie. "temperature at ground level")
   * @param units the units used for this variable. (ie. "C" or "K" for temperature)
   * @param long_name a more descriptive name than can be fitted in the VariableName (could be used
   *     on a plot axis)
   */
  public CoordinateVariableDefinition(
      @Nonnull VariableName variableName,
      @Nonnull NetcdfDataType dataType,
      int size,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    super(dataType, description, units, long_name, Collections.emptyMap());
    this.variableName = variableName;
    this.size = size;
    this.values = null;
  }

  /**
   * if we don't supply values, we need to set size and type. size can be
   * CoordinateVariable.UNLIMITED
   *
   * @param variableName Name and Group of this Coordinate Variable
   * @param dataType dataType of the Variable
   * @param size length of the Dimension. (or UNLIMITED)
   * @param description a short description of the variable. (ie. "temperature at ground level")
   * @param units the units used for this variable. (ie. "C" or "K" for temperature)
   * @param long_name a more descriptive name than can be fitted in the VariableName (could be used
   *     on a plot axis)
   * @param optional_attribs extra metadata attributes to be added to this variable. the values are
   *     String[] as we mostly want to add a single String but sometimes more than one String.
   */
  public CoordinateVariableDefinition(
      @Nonnull VariableName variableName,
      @Nonnull NetcdfDataType dataType,
      int size,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    super(dataType, description, units, long_name, optional_attribs);
    this.variableName = variableName;
    this.size = size;
    this.values = null;
  }

  /**
   * if we give 'values', we don't need to give type and length as these can be taken from the
   * 'values' array.
   *
   * @param variableName Name and Group of this Coordinate Variable
   * @param values an Array of values for this Variable. (ie. [1, 2, 3] will make an Int variable of
   *     length 3.)
   * @param description a short description of the variable. (ie. "temperature at ground level")
   * @param units the units used for this variable. (ie. "C" or "K" for temperature)
   * @param long_name a more descriptive name than can be fitted in the VariableName (could be used
   *     on a plot axis)
   */
  public CoordinateVariableDefinition(
      @Nonnull VariableName variableName,
      @Nonnull Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    super(
        NetcdfDataType.translateDatatype(values),
        description,
        units,
        long_name,
        Collections.emptyMap());
    this.variableName = variableName;
    this.size = Array.getLength(values);
    this.values = values;
  }

  /**
   * if we give 'values', we don't need to give type and length as these can be taken from the
   * 'values' array.
   *
   * @param variableName Name and Group of this Coordinate Variable
   * @param values an Array of values for this Variable. (ie. [1, 2, 3] will make an Int variable of
   *     length 3.)
   * @param description a short description of the variable. (ie. "temperature at ground level")
   * @param units the units used for this variable. (ie. "C" or "K" for temperature)
   * @param long_name a more descriptive name than can be fitted in the VariableName (could be used
   *     on a plot axis)
   * @param optional_attribs extra metadata attributes to be added to this variable. the values are
   *     String[] as we mostly want to add a single String but sometimes more than one String.
   */
  public CoordinateVariableDefinition(
      @Nonnull VariableName variableName,
      @Nonnull Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    super(
        NetcdfDataType.translateDatatype(values), description, units, long_name, optional_attribs);
    this.variableName = variableName;
    this.size = Array.getLength(values);
    this.values = values;
  }

  public boolean isUnlimited() {
    return this.size == UNLIMITED;
  }

  public int getSize() {
    return size;
  }

  public @Nonnull VariableName getVariableName() {
    return this.variableName;
  }

  public @Nullable Object getValues() {
    return values;
  }
}
