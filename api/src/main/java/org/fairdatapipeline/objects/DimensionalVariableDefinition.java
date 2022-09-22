package org.fairdatapipeline.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.netcdf.*;

/**
 * DimensionVariableDefinition, aka 'Array'.. multidimensional array.
 */
public class DimensionalVariableDefinition extends VariableDefinition {
  private final @Nonnull VariableName[] dimensions;
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
      @Nonnull DimensionName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    this(name, dataType, dimensions, description, units, long_name, Collections.emptyMap());
  }

  public DimensionalVariableDefinition(
          @Nonnull VariableName name,
          @Nonnull NetcdfDataType dataType,
          @Nonnull DimensionName[] dimensions,
          @Nonnull String description,
          @Nonnull String units,
          @Nonnull String long_name,
          @Nonnull Map<String, String> optional_attribs) {
    this(name, dataType, dimensions, description, units, long_name, optional_attribs, null);
  }

  public DimensionalVariableDefinition(@Nonnull LocalVariableDefinition localVarDef, NetcdfGroupName groupName, int size, String dimensionName) {
    super(localVarDef.dataType, localVarDef.description, localVarDef.units, localVarDef.long_name, localVarDef.optional_attribs, localVarDef.missingValue);
    this.variableName = new VariableName(localVarDef.getLocalName(), groupName);
    this.dimensions = new VariableName[] {new VariableName(new NetcdfName(dimensionName), groupName) };
  }

  public DimensionalVariableDefinition(
      @Nonnull VariableName name,
      @Nonnull NetcdfDataType dataType,
      @Nonnull DimensionName[] dimensions,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String> optional_attribs,
      Object missingValue) {
    super(dataType, description, units, long_name, optional_attribs, missingValue);
    this.variableName = name;
    int num_dims = dimensions.length;

    ArrayList<String> dimension_names = new ArrayList<>();
    this.dimensions = new VariableName[dimensions.length];

    for (int i = 0; i < num_dims; i++) {
      if(dimensions[i].getClass() == VariableName.class) this.dimensions[i] = (VariableName) dimensions[i];
      else this.dimensions[i] = new VariableName((NetcdfName) dimensions[i], name.getGroupName());
      if (dimension_names.contains(this.dimensions[i].getFullPath()))
        throw (new IllegalActionException(
            "dimension names must be unique; duplicate: " + this.dimensions[i].getFullPath()));
      dimension_names.add(this.dimensions[i].getFullPath());
    }
  }

  public @Nonnull VariableName getVariableName() {
    return this.variableName;
  }
  public VariableName[] getDimensions() {
    return this.dimensions.clone();
  }
}
