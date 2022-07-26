package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayDefinition;
import org.fairdatapipeline.objects.NumericalArrayImpl;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;

public class NetcdfReader {
  NetcdfFile file;

  public NetcdfReader(String fileName) throws IOException {
    this.file = NetcdfFiles.open(fileName);
  }

  public NumericalArrayDefinition getArray(String variableName) {
    return getArray(new VariableName(variableName));
  }

  public NumericalArrayDefinition getArray(VariableName variableName) {
    Variable v = this.getVariable(variableName);
    Map<String, String> argument_attribs = new HashMap<>();
    argument_attribs.put("long_name", "");
    argument_attribs.put("standard_name", "");
    argument_attribs.put("description", "");
    argument_attribs.put("units", "");
    Map<String, String> optional_attribs = new HashMap<>();
    for (Attribute attribute : v.attributes()) {
      if (attribute.isString()) {
        if (argument_attribs.containsKey(attribute.getName())) {
          argument_attribs.put(attribute.getName(), attribute.getStringValue());
        } else {
          optional_attribs.put(attribute.getName(), attribute.getStringValue());
        }
      }
    }
    String[] dims = v.getDimensions().stream().map(Dimension::getName).toArray(String[]::new);
    NetcdfDataType dataType = NetcdfDataType.translate(v.getDataType());
    return new NumericalArrayDefinition(
        variableName,
        dataType,
        dims,
        argument_attribs.get("description"),
        argument_attribs.get("units"),
        argument_attribs.get("long_name"),
        optional_attribs);
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
    return new NumericalArrayImpl(a);
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
    return new NumericalArrayImpl(a);
  }

  public void close() {
    try {
      if (this.file != null) this.file.close();
    } catch (IOException e) {
      //
    }
  }
}
