package org.fairdatapipeline.objects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import org.fairdatapipeline.api.IllegalActionException;
import org.junit.jupiter.api.*;

class NumericalArrayTest {

  @Test
  void t1dArray() {
    Number[] n = new Number[] {1, 2, 3};
    NumericalArray na = new NumericalArrayImpl(n);

    Assertions.assertEquals(n, na.as1DArray());
  }

  @Test
  void t2dArray() {
    Number[][] n = new Number[][] {{1, 2, 3}, {4, 5, 6}};
    Number[][] r;
    NumericalArray na = new NumericalArrayImpl(n);
    r = na.as2DArray();
    Assertions.assertEquals(n, r);
  }

  private ArrayList<Integer> checkShape(Object nDArray, boolean deepCheck) {
    ArrayList<Integer> dims = new ArrayList<>();
    int l = Array.getLength(nDArray);
    dims.add(l);
    if (l == 0) return dims;
    Object o = Array.get(nDArray, 0);
    if (o.getClass().isArray()) {
      ArrayList<Integer> subshape = checkShape(o, deepCheck);
      if (deepCheck) {
        for (int i = 1; i < Array.getLength(nDArray); i++) {
          if (!subshape.equals(checkShape(Array.get(nDArray, i), deepCheck))) {
            throw (new IllegalActionException("not a proper array"));
          }
        }
      }
      dims.addAll(subshape);
    }
    return dims;
  }

  void setNumbers(Object old_with_primitives, Object new_with_numbers, int[] dimensions) {
    for (int i = 0; i < dimensions[0]; i++) {
      if (dimensions.length == 1) {
        ((Number[]) new_with_numbers)[i] = (Number) Array.get(old_with_primitives, i);
      } else {
        setNumbers(
            Array.get(old_with_primitives, i),
            Array.get(new_with_numbers, i),
            Arrays.stream(dimensions).skip(1).toArray());
      }
    }
  }

  Object unprim(Object o) {
    if (!o.getClass().isArray())
      throw (new IllegalArgumentException("unprim must work on an array"));
    int[] dimensions = checkShape(o, false).stream().mapToInt(i -> i).toArray();
    Object new_array = Array.newInstance(Number.class, dimensions);
    setNumbers(o, new_array, dimensions);
    return new_array;
  }

  @Test
  void test_unprimitivize5() {
    Object o = new int[][][] {{{1, 2}, {3, 4}}, {{4, 5}, {6, 7}}};
    System.out.println(Arrays.deepToString((int[][][]) o));
    Object oo = unprim(o);
    System.out.println(Arrays.deepToString((Number[][][]) oo));
  }

  @Test
  void check_type() {
    int i = 1;
    Object oo = (Object) i;
    System.out.println(oo.getClass());
    Object o = new int[][] {{1, 2, 3}, {4, 5, 6}};

    NumericalArray na = new NumericalArrayImpl(o);
    na.as2DArray();

    o = new int[] {1, 2, 3, 4, 5};
    new NumericalArrayImpl(o);

    o = new double[] {1.1, 2.2, 3.3, 4.4, 5.5};
    new NumericalArrayImpl(o);

    o = new Integer[] {1, 2, 3, 4, 5};
    new NumericalArrayImpl(o);

    Assertions.assertThrows(
        IllegalActionException.class,
        () -> {
          new NumericalArrayImpl(new Integer[][] {{1, 2, 3}, {4, 5}});
        });

    Assertions.assertThrows(
        IllegalActionException.class,
        () -> {
          new NumericalArrayImpl(new Integer[][][] {{{1, 2}, {3, 4}}, {{5, 6}, {7, 8, 9}}});
        });

    Assertions.assertThrows(
        IllegalActionException.class,
        () -> {
          new NumericalArrayImpl(new Integer[][][] {{{1, 2}, {3, 4}}, {{5, 6}, {7, 8}, {9, 10}}});
        });

    // int[] o2 = new int[] {1,2,3,4,5,6};
    // check_type2(o2);

  }
}
