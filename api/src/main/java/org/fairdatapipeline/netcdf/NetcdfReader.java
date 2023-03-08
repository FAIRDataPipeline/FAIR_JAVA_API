package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fairdatapipeline.objects.*;
import org.fairdatapipeline.objects.Dimension;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

public class NetcdfReader {
  NetcdfFile file;
  private static final String[] attrib_names =
      new String[] {"description", "units", "long_name", "standard_name"};

  /**
   * @param fileName the fileName of the file to open.
   * @throws IOException if the file cannot be opened for reading.
   */
  public NetcdfReader(String fileName) throws IOException {
    this.file = NetcdfFiles.open(fileName);
  }

  /**
   * retrieves the variable definition (all the metadata) for the given VariableName.
   *
   * @param variableName
   * @return the variable definition (all the metadata) for the given VariableName.
   */
  public VariableDefinition getArray(String variableName) {
    return getArray(new VariableName(variableName));
  }

  /**
   * retrieves the variable definition (all the metadata) for the given VariableName.
   *
   * @param variableName
   * @return the variable definition (all the metadata) for the given VariableName.
   */
  public VariableDefinition getArray(VariableName variableName) {
    Variable v = this.getVariable(variableName);
    Map<String, String> argument_attribs = new HashMap<>();
    Arrays.stream(attrib_names).forEach(s -> argument_attribs.put(s, ""));
    Map<String, String[]> optional_attribs = new HashMap<>();
    for (Attribute attribute : v.attributes()) {
      if (attribute.isString()) {
        if (argument_attribs.containsKey(attribute.getName())) {
          argument_attribs.put(attribute.getName(), attribute.getStringValue());
        } else {
          if (attribute.getValues() != null) {
            optional_attribs.put(
                attribute.getName(),
                (String[]) attribute.getValues().get1DJavaArray(DataType.STRING));
          }
        }
      }
    }
    int[] sh = v.getShape();

    Dimension[] dims =
        v.getDimensions().stream()
            .map(
                dim -> {
                  if (dim.getName().startsWith("__fdp"))
                    return new Dimension(sh[Integer.valueOf(dim.getName().split("_dim_")[1])]);
                  else return new Dimension(dim.getName());
                })
            .toArray(Dimension[]::new);

    NetcdfDataType dataType = NetcdfDataType.translate(v.getDataType());
    if (v.isCoordinateVariable()) {
      return new CoordinateVariableDefinition(
          variableName,
          dataType,
          v.getShape(0),
          argument_attribs.get(attrib_names[0]),
          argument_attribs.get(attrib_names[1]),
          argument_attribs.get(attrib_names[2]),
          optional_attribs);

    } else {

      return new DimensionalVariableDefinition(
          variableName,
          dataType,
          dims,
          argument_attribs.get(attrib_names[0]),
          argument_attribs.get(attrib_names[1]),
          argument_attribs.get(attrib_names[2]),
          optional_attribs);
    }
  }

  private Object getMissingValue(Variable v) {
    Attribute a = v.findAttribute(NetcdfBuilder.ATTRIB_FILLVALUE);
    if (a == null) return null;
    if (a.isString()) return a.getStringValue();
    return a.getNumericValue();
  }

  private LocalVariableDefinition makeVarDef(Variable v) {
    NetcdfName localName = new NetcdfName(v.getShortName());
    NetcdfDataType dataType = NetcdfDataType.translate(v.getDataType());
    Map<String, String> argument_attribs = new HashMap<>();
    Arrays.stream(attrib_names).forEach(s -> argument_attribs.put(s, ""));
    Map<String, String[]> optional_attribs = new HashMap<>();
    Object missingValue = getMissingValue(v);
    for (Attribute attribute : v.attributes()) {
      if (attribute.isString()) {
        if (argument_attribs.containsKey(attribute.getName())) {
          argument_attribs.put(attribute.getName(), attribute.getStringValue());
        } else {
          if (attribute.getValues() != null) {
            optional_attribs.put(
                attribute.getName(),
                Arrays.stream((Object[]) attribute.getValues().get1DJavaArray(DataType.STRING))
                    .map(String.class::cast)
                    .toArray(String[]::new));
          }
        }
      }
    }

    return new LocalVariableDefinition(
        localName,
        dataType,
        argument_attribs.get(attrib_names[0]),
        argument_attribs.get(attrib_names[1]),
        argument_attribs.get(attrib_names[2]),
        optional_attribs,
        missingValue);
  }

  public TableDefinition getTable(VariableName tablename) {
    Group g = file.findGroup(tablename.getFullPath());
    if (g == null) throw (new IllegalArgumentException("tablename group not found"));
    List<Variable> variables = getVariables(tablename);
    Map<String, String> argument_attribs = new HashMap<>();
    Arrays.stream(attrib_names).forEach(s -> argument_attribs.put(s, ""));
    Map<String, String[]> optional_attribs = new HashMap<>();
    for (Attribute attribute : g.attributes()) {
      if (attribute.isString()) {
        if (argument_attribs.containsKey(attribute.getName())) {
          argument_attribs.put(attribute.getName(), attribute.getStringValue());
        } else {
          if (attribute.getValues() != null) {
            optional_attribs.put(
                attribute.getName(),
                Arrays.stream((Object[]) attribute.getValues().get1DJavaArray(DataType.STRING))
                    .map(String.class::cast)
                    .toArray(String[]::new));
          }
        }
      }
    }
    LocalVariableDefinition[] columns =
        variables.stream().map(this::makeVarDef).toArray(LocalVariableDefinition[]::new);

    return new TableDefinition(
        new NetcdfGroupName(tablename.getFullPath()),
        (int) variables.get(0).getSize(),
        argument_attribs.get(attrib_names[0]),
        argument_attribs.get(attrib_names[2]),
        optional_attribs,
        columns);
  }

  public List<Variable> getVariables(VariableName tableName) {
    Group g = file.findGroup(tableName.getFullPath());
    return g.getVariables();
  }

  /**
   * get the Variable, which is needed for the read methods.
   *
   * @param variableName the name of the variable.
   * @return the Variable (needed for read methods)
   * @throws IllegalArgumentException when the variable can't be found
   */
  public Variable getVariable(VariableName variableName) throws IllegalArgumentException {
    return this.getVariable(variableName.getFullPath());
  }

  /**
   * get the Variable, which is needed for the read methods.
   *
   * @param variablefullname the name of the variable (as a string)
   * @return the Variable (needed for read methods)
   * @throws IllegalArgumentException when the variable can't be found
   */
  public Variable getVariable(String variablefullname) throws IllegalArgumentException {
    Variable v = file.findVariable(variablefullname);
    if (v == null) throw (new IllegalArgumentException("can't find variable " + variablefullname));
    return v;
  }

  /**
   * get the shape of the Variable. (int array containing the sizes of all dimensions)
   *
   * @param v
   * @return int array containing the sizes of all dimensions
   */
  public int[] getShape(Variable v) {
    return v.getShape();
  }

  /**
   * read ALL data in the Variable.
   *
   * @param v the variable
   * @return all data in this array
   * @throws IllegalArgumentException if it runs into a problem reading.
   */
  public NumericalArray read(Variable v) throws IllegalArgumentException {
    return new NumericalArrayImpl(this.readObj(v));
  }

  /**
   * read ALL data in the Variable.
   *
   * @param v the variable
   * @return all data in this array
   * @throws IllegalArgumentException if it runs into a problem reading.
   */
  public Object readObj(Variable v) throws IllegalArgumentException {
    Array a;
    try {
      a = v.read();
    } catch (IOException e) {
      throw (new IllegalArgumentException("problem"));
    }
    return a.copyToNDJavaArray();
  }

  /**
   * read part of the data from Variable.
   *
   * <p>example for variable with shape {2, 3, 4} <code>
   * int[] origin = new int[] {0,0,0};
   * int[] shape = new int[] {1, 3, 4};
   * for(int i=0;i<2;i++) {
   *   origin[0] = i;
   *   NumericalArray na = read(v,origin,shape);
   *   // na now contains a 3d-array with shape 1, 3, 4
   * }
   * </code>
   *
   * @param v the variable to read from
   * @param origin where to start reading.
   * @param shape how big a chunk to read.
   * @return the data read
   */
  public NumericalArray read(Variable v, int[] origin, int[] shape) {
    Array a;
    try {
      a = v.read(origin, shape);
    } catch (IOException e) {
      throw (new IllegalArgumentException("problem"));
    } catch (InvalidRangeException e) {
      throw (new IllegalArgumentException("other problem"));
    }
    return new NumericalArrayImpl(a.copyToNDJavaArray());
  }

  public void close() {
    try {
      if (this.file != null) this.file.close();
    } catch (IOException e) {
      //
    }
  }
}
