package org.fairdatapipeline.netcdf;

import java.io.IOException;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayImpl;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

public class NetcdfReader {
  NetcdfFile file;

  public NetcdfReader(String fileName) {
    try {
      this.file = NetcdfFiles.open(fileName);
    } catch (IOException e) {

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
}
