package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.fairdatapipeline.objects.*;
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

  public VariableDefinition getArray(String variableName) {
    return getArray(new VariableName(variableName));
  }

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
    NetcdfName[] dims =
        v.getDimensions().stream()
            .map(dim -> new NetcdfName(dim.getName()))
            .toArray(NetcdfName[]::new);
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

  public Variable getVariable(VariableName variableName) {
    return this.getVariable(variableName.getFullPath());
  }

  public Variable getVariable(String variablefullname) {
    Variable v = file.findVariable(variablefullname);
    if (v == null) throw (new IllegalArgumentException("can't find variable " + variablefullname));
    return v;
  }

  public int[] getShape(Variable v) {
    return v.getShape();
  }

  public NumericalArray read(Variable v) {
    Array a;
    try {
      a = v.read();
    } catch (IOException e) {
      throw (new IllegalArgumentException("problem"));
    }
    return new NumericalArrayImpl(a.copyToNDJavaArray());
  }

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
