package org.fairdatapipeline.objects;

import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.netcdf.*;

public class NumericalArrayDefinition {
  private final @Nonnull VariableName variableName;
  private final @Nonnull NetcdfDataType dataType;
  private final @Nonnull VariableName[] dimensions;
  private final @Nonnull String description;
  private final @Nonnull String units;
  private final @Nonnull String long_name;

  /**
   * @param name
   * @param dataType
   * @param dimensions
   * @param description
   * @param units
   * @param long_name
   */
  public NumericalArrayDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(
        name,
        dataType,
        (VariableName[]) Arrays.stream(dimensions).map(s -> new VariableName(s, null)).toArray(),
        description,
        units,
        long_name);
  }

  /**
   * @param name
   * @param dataType
   * @param dimensions
   * @param description
   * @param units
   * @param long_name
   */
  public NumericalArrayDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull VariableName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {

    int num_dims = dimensions.length;
    ArrayList<String> dimension_names = new ArrayList<>();

    for (int i = 0; i < num_dims; i++) {
      if (dimension_names.contains(dimensions[i].getFullPath()))
        throw (new IllegalActionException(
            "dimension names must be unique; duplicate: " + dimensions[i].getFullPath()));
      dimension_names.add(dimensions[i].getFullPath());
    }
    this.variableName = name;
    this.dataType = dataType;
    this.description = description;
    this.units = units;
    this.dimensions = dimensions;
    this.long_name = long_name;
  }

  public VariableName getVariableName() {
    return variableName;
  }

  public NetcdfDataType getDataType() {
    return dataType;
  }

  public String getDescription() {
    return description;
  }

  public VariableName[] getDimensions() {
    return this.dimensions.clone();
  }

  public String getUnits() {
    return this.units;
  }

  public String getLong_name() {
    return long_name;
  }
}
