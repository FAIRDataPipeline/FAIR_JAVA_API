package org.fairdatapipeline.objects;

import org.fairdatapipeline.api.IllegalActionException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

public class NumericalArrayTest {

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
    System.out.println(n.length);
    System.out.println(n[1][1]);
    NumericalArray na = new NumericalArrayImpl(n);
    r = na.as2DArray();
    System.out.println(r.length);
    System.out.println(r[1][1]);
    System.out.println(na.as1DArray().length);
    System.out.println(na.as1DArray()[1]);
    Assertions.assertEquals(n, r);
  }

  public void check_type2(Object a){
    System.out.println(a.getClass().getSimpleName());
  }

  @Test
  void  check_type() {
    Object o = new int[][] {{1,2,3},{4,5,6}};
    new NumericalArrayImpl(o);

    o = new int[] {1,2,3,4,5};
    new NumericalArrayImpl(o);

    o = new double[] {1.1,2.2,3.3,4.4,5.5};
    new NumericalArrayImpl(o);

    o = new Integer[] {1,2,3,4,5};
    new NumericalArrayImpl(o);

    Assertions.assertThrows(IllegalActionException.class, () -> {new NumericalArrayImpl(
            new Integer[][] {{1,2,3},{4,5}});});

    Assertions.assertThrows(IllegalActionException.class, () -> {new NumericalArrayImpl(
            new Integer[][][] {{{1,2}, {3,4}},{{5,6},{7,8,9}}});});

    Assertions.assertThrows(IllegalActionException.class, () -> {new NumericalArrayImpl(
            new Integer[][][] {{{1,2},{3,4}},{{5,6},{7,8},{9,10}}});});






    //int[] o2 = new int[] {1,2,3,4,5,6};
    //check_type2(o2);

  }
}
