package org.fairdatapipeline.objects;

import org.junit.jupiter.api.*;

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
}
