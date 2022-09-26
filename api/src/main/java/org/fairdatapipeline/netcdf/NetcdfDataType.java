package org.fairdatapipeline.netcdf;

import java.lang.reflect.Array;
import org.jetbrains.annotations.NotNull;
import ucar.ma2.DataType;

public enum NetcdfDataType {
  BOOLEAN,
  BYTE,
  CHAR,
  SHORT,
  INT,
  LONG,
  FLOAT,
  DOUBLE,
  SEQUENCE,
  STRING;

  DataType translate() {
    return NetcdfDataType.translate(this);
  }

  public static DataType translate(NetcdfDataType dataType) {
    switch (dataType) {
      case BOOLEAN:
        return DataType.BOOLEAN;
      case BYTE:
        return DataType.BYTE;
      case CHAR:
        return DataType.CHAR;
      case SHORT:
        return DataType.SHORT;
      case INT:
        return DataType.INT;
      case LONG:
        return DataType.LONG;
      case FLOAT:
        return DataType.FLOAT;
      case DOUBLE:
        return DataType.DOUBLE;
      case SEQUENCE:
        return DataType.SEQUENCE;
      case STRING:
        return DataType.STRING;
    }
    throw (new IllegalArgumentException("unknown datatype: " + dataType));
  }

  public static NetcdfDataType translate(DataType dataType) {
    if (dataType == DataType.BOOLEAN) return NetcdfDataType.BOOLEAN;
    if (dataType == DataType.BYTE) return NetcdfDataType.BYTE;
    if (dataType == DataType.CHAR) return NetcdfDataType.CHAR;
    if (dataType == DataType.SHORT) return NetcdfDataType.SHORT;
    if (dataType == DataType.INT) return NetcdfDataType.INT;
    if (dataType == DataType.LONG) return NetcdfDataType.LONG;
    if (dataType == DataType.FLOAT) return NetcdfDataType.FLOAT;
    if (dataType == DataType.DOUBLE) return NetcdfDataType.DOUBLE;
    if (dataType == DataType.SEQUENCE) return NetcdfDataType.SEQUENCE;
    if (dataType == DataType.STRING) return NetcdfDataType.STRING;
    throw (new IllegalArgumentException("unknown datatype: " + dataType));
  }

  public static NetcdfDataType translateDatatype(@NotNull Object o) {
    if (Array.newInstance(Integer.class, 0).getClass().equals(o.getClass())
        || Array.newInstance(int.class, 0).getClass().equals(o.getClass())) {
      return NetcdfDataType.INT;
    } else if (Array.newInstance(Long.class, 0).getClass().equals(o.getClass())
        || Array.newInstance(long.class, 0).getClass().equals(o.getClass())) {
      return NetcdfDataType.LONG;
    } else if (Array.newInstance(Double.class, 0).getClass().equals(o.getClass())
        || Array.newInstance(double.class, 0).getClass().equals(o.getClass())) {
      return NetcdfDataType.DOUBLE;
    } else if (Array.newInstance(String.class, 0).getClass().equals(o.getClass())) {
      return NetcdfDataType.STRING;
    } else if (Array.newInstance(Float.class, 0).getClass().equals(o.getClass())
        || Array.newInstance(float.class, 0).getClass().equals(o.getClass())) {
      return NetcdfDataType.FLOAT;
    }
    throw (new UnsupportedOperationException(
        "can't translate object of class "
            + o.getClass().getSimpleName()
            + " to NetCDF data type."));
  }

  public static ucar.ma2.Array translateArray(@NotNull Object o) {
    return ucar.ma2.Array.factory(
        translate(translateDatatype(o)), new int[] {Array.getLength(o)}, o);
  }

  /**
   * @param dataType
   * @param dimSizes
   * @param o
   * @return
   */
  public static ucar.ma2.Array translateArray(
      @NotNull NetcdfDataType dataType, @NotNull int[] dimSizes, @NotNull Object o) {
    return ucar.ma2.Array.factory(dataType.translate(), dimSizes, o);
  }
}
