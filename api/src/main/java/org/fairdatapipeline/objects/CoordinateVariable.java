package org.fairdatapipeline.objects;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;

public class CoordinateVariable extends Variable {
  private final int size;
  public static final int UNLIMITED = 0;
  private final @Nullable Object values;

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
  public CoordinateVariable(
      @Nonnull VariableName variableName,
      @Nonnull NetcdfDataType dataType,
      int size,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    super(variableName, dataType, description, units, long_name, Collections.emptyMap());
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
  public CoordinateVariable(
      @Nonnull VariableName variableName,
      @Nonnull NetcdfDataType dataType,
      int size,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String> optional_attribs) {
    super(variableName, dataType, description, units, long_name, optional_attribs);
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
  public CoordinateVariable(
      @Nonnull VariableName variableName,
      @Nonnull Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    super(
        variableName,
        NetcdfDataType.translateDatatype(values),
        description,
        units,
        long_name,
        Collections.emptyMap());
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
  public CoordinateVariable(
      @Nonnull VariableName variableName,
      @Nonnull Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String> optional_attribs) {
    super(
        variableName,
        NetcdfDataType.translateDatatype(values),
        description,
        units,
        long_name,
        optional_attribs);
    this.size = Array.getLength(values);
    this.values = values;
  }

  public boolean isUnlimited() {
    return this.size == UNLIMITED;
  }

  public int getSize() {
    return size;
  }

  public @Nullable Object getValues() {
    return values;
  }
}
