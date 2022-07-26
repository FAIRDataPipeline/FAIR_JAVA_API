package org.fairdatapipeline.netcdf;

import java.lang.reflect.Array;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DimensionDefinition {
  private final @Nonnull VariableName variableName;
  private final @Nonnull NetcdfDataType dataType;
  private final int size;
  public static final int UNLIMITED = 0;
  private final @Nullable Object values;
  private final @Nonnull String description;
  private final @Nonnull String units;
  private final @Nonnull String long_name;

  /**
   * if we don't supply values, we need to set length and type. length can be
   * DimensionDefinitionLocal.UNLIMITED
   *
   * @param name
   * @param dataType
   * @param size
   * @param description
   * @param units
   * @param long_name
   */
  public DimensionDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      int size,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(name, dataType, size, null, description, units, long_name);
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
  public DimensionDefinition(
      @Nonnull VariableName variableName,
      @Nonnull Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(
        variableName,
        NetcdfDataType.translateDatatype(values),
        Array.getLength(values),
        values,
        description,
        units,
        long_name);
  }

  private DimensionDefinition(
      @Nonnull VariableName variableName,
      @Nonnull NetcdfDataType dataType,
      int size,
      @Nullable Object values,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this.variableName = variableName;
    this.dataType = dataType;
    this.size = size;
    this.values = values;
    this.description = description;
    this.units = units;
    this.long_name = long_name;
  }

  public boolean isUnlimited() {
    return this.size == UNLIMITED;
  }

  public @Nonnull VariableName getVariableName() {
    return variableName;
  }

  public @Nonnull NetcdfDataType getDataType() {
    return dataType;
  }

  public int getSize() {
    return size;
  }

  public @Nullable Object getValues() {
    return values;
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
}
