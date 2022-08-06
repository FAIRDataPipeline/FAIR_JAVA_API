package org.fairdatapipeline.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.netcdf.*;

public class DimensionalVariableDefinition extends VariableDefinition {
  private final @Nonnull VariableName[] dimensions;

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
      @Nonnull String[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(
        name,
        dataType,
        Arrays.stream(dimensions)
            .map(s -> new VariableName(s, name.getGroupName()))
            .toArray(VariableName[]::new),
        description,
        units,
        long_name);
  }

  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      Map<String, String> optional_attribs) {
    this(
        name,
        dataType,
        Arrays.stream(dimensions)
            .map(s -> new VariableName(s, name.getGroupName()))
            .toArray(VariableName[]::new),
        description,
        units,
        long_name,
        optional_attribs);
  }

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
      @Nonnull VariableName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(name, dataType, dimensions, description, units, long_name, Collections.emptyMap());
  }

  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull VariableName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String> optional_attribs) {
    super(name, dataType, description, units, long_name, optional_attribs);
    int num_dims = dimensions.length;

    ArrayList<String> dimension_names = new ArrayList<>();

    for (int i = 0; i < num_dims; i++) {
      if (dimension_names.contains(dimensions[i].getFullPath()))
        throw (new IllegalActionException(
            "dimension names must be unique; duplicate: " + dimensions[i].getFullPath()));
      dimension_names.add(dimensions[i].getFullPath());
    }
    this.dimensions = dimensions;
  }

  public VariableName[] getDimensions() {
    return this.dimensions.clone();
  }
}
