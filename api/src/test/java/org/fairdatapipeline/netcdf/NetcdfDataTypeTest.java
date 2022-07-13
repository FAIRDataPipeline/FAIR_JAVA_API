package org.fairdatapipeline.netcdf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

class NetcdfDataTypeTest {
  @Test
  void test_translate_datatype_integer() {
    Object o = new Integer[] {1, 2, 3};
    DataType dt = NetcdfDataType.translate(NetcdfDataType.translate_datatype(o));
    Assertions.assertEquals(DataType.INT, dt);
  }

  @Test
  void test_translate_datatype_int() {
    Object o = new int[] {1, 2, 3};
    DataType dt = NetcdfDataType.translate(NetcdfDataType.translate_datatype(o));
    Assertions.assertEquals(DataType.INT, dt);
  }

  @Test
  void test_translate_datatype_double() {
    Object o = new double[] {1.1, 2.2, 3.3};
    DataType dt = NetcdfDataType.translate(NetcdfDataType.translate_datatype(o));
    Assertions.assertEquals(DataType.DOUBLE, dt);
  }

  @Test
  void test_translate_datatype_string() {
    Object o = new String[] {"a", "s", "ee"};
    DataType dt = NetcdfDataType.translate(NetcdfDataType.translate_datatype(o));
    Assertions.assertEquals(DataType.STRING, dt);
  }

  @Test
  void translate_array_test_int() {
    Object o = new int[] {1, 2, 3, 4};
    ucar.ma2.Array a = NetcdfDataType.translate_array(o);
    Assertions.assertEquals(2, a.getInt(1));
  }

  @Test
  void translate_array_test_double() {
    Object o = new double[] {1.1, 2.2, 3.3, 4.4};
    ucar.ma2.Array a = NetcdfDataType.translate_array(o);
    Assertions.assertEquals(2.2, a.getDouble(1));
  }

  @Test
  void translate_array_test_string() {
    Object o = new String[] {"aap", "noot", "mies"};
    ucar.ma2.Array a = NetcdfDataType.translate_array(o);
    Assertions.assertEquals("noot", a.getObject(1));
  }

  @Test
  void translate_array_test_multidim() {
    Object o = new int[] {1, 2, 3, 11, 12, 13};
    ucar.ma2.Array a = NetcdfDataType.translate_array(NetcdfDataType.INT, new int[] {2, 3}, o);
    Assertions.assertEquals(13, a.getInt(Index.factory(a.getShape()).set0(1).set1(2)));
  }

  @Test
  void makeArrayFromJavaArrayint() {
    Object o = new int[] {1, 2, 3};
    Array a = Array.makeFromJavaArray(o);
    Assertions.assertEquals(2, a.getInt(1));
  }

  @Test
  void makeArrayFromJavaArraydouble() {
    Object o = new double[] {1.1, 2.2, 3.3};
    Array a = Array.makeFromJavaArray(o);
    Assertions.assertEquals(2.2, a.getDouble(1));
  }

  @Test
  void makeArrayFromJavaInt2d() {
    Object o = new int[][] {{1, 2, 3}, {11, 22, 22}};
    Array a = Array.makeFromJavaArray(o);
    Assertions.assertEquals(22, a.getInt(Index.factory(a.getShape()).set0(1).set1(1)));
  }

  @Test
  void makeArrayFromJava_addedTopDimension() {
    int[] i = new int[] {1, 2, 3};
    Array a = Array.makeArrayRankPlusOne(Array.makeFromJavaArray(i));
    Assertions.assertEquals(2, a.getShape().length);
  }

  @Test
  @Disabled("this one failed because makeFromJavaArray only works on an Array of primitives")
  void makeArrayFromJavaArrayString() {
    Object o = new String[] {"aap", "noot", "mies"};
    Array a = Array.makeFromJavaArray(o);
    Assertions.assertEquals("noot", a.getObject(1));
  }
}
