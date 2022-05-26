package org.fairdatapipeline.netcdf;

import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;

public class NetcdfDataTypeTest {
    @Test
    void test_translate_datatype_integer() {
        Object o = new Integer[] {1, 2, 3};
        DataType dt = NetcdfDataType.translate_datatype(o);
        Assertions.assertEquals(DataType.INT, dt);
    }

    @Test
    void test_translate_datatype_int() {
        Object o = new int[] {1, 2, 3};
        DataType dt = NetcdfDataType.translate_datatype(o);
        Assertions.assertEquals(DataType.INT, dt);
    }

    @Test
    void test_translate_datatype_double() {
        Object o = new double[] {1.1, 2.2, 3.3};
        DataType dt = NetcdfDataType.translate_datatype(o);
        Assertions.assertEquals(DataType.DOUBLE, dt);
    }

    @Test
    void test_translate_datatype_string() {
        Object o = new String[] {"a", "s", "ee"};
        DataType dt = NetcdfDataType.translate_datatype(o);
        Assertions.assertEquals(DataType.STRING, dt);
    }

    @Test
    void get_length() {
        Object[] o = new Object[5];
        o[0] = new int[]{1, 2, 3};
        o[1] = new Integer[]{1, 2, 3};
        o[2] = new String[]{"a", "b", "c"};
        o[3] = new double[]{1.1, 2.2, 3.3};
        o[4] = new Double[]{1.1, 2.2, 3.3};
        for (int i = 0; i < o.length; i++) {
            Assertions.assertEquals(3, NetcdfDataType.get_array_length(o[i]));
        }
        Assertions.assertThrows(IllegalArgumentException.class, () -> {NetcdfDataType.get_array_length(1);});
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
    @Disabled("this one failed because makeFromJavaArray only works on an Array of primitives")
    void makeArrayFromJavaArrayString() {
        Object o = new String[] {"aap", "noot", "mies"};
        Array a = Array.makeFromJavaArray(o);
        Assertions.assertEquals("noot", a.getObject(1));
    }


}
