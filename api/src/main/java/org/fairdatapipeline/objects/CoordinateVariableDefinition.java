package org.fairdatapipeline.objects;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.NetcdfGroupName;
import org.fairdatapipeline.netcdf.VariableName;

/**
 *  this class
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
   * @param variableName
   * @param dataType
   * @param size
   * @param description
   * @param units
   * @param long_name
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
   * @param variableName
   * @param dataType
   * @param size
   * @param description
   * @param units
   * @param long_name
   * @param optional_attribs
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
   * @param variableName
   * @param values
   * @param description
   * @param units
   * @param long_name
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
   * @param variableName
   * @param values
   * @param description
   * @param units
   * @param long_name
   * @param optional_attribs
   */
  public CoordinateVariableDefinition(
      @Nonnull VariableName variableName,
      @Nonnull Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    super(
        NetcdfDataType.translateDatatype(values),
        description,
        units,
        long_name,
        optional_attribs);
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
