package org.fairdatapipeline.objects;

import org.junit.jupiter.api.*;

public class NumericalArrayTest {

  @Test
  void t1dArray() {
    Number[] n = new Number[] {1, 2, 3};
    NumericalArray na = new org.fairdatapipeline.objects.NumericalArrayImpl(n);
    Assertions.assertEquals(n, na.as1DArray());
  }

  @Test
  void t2dArray() {
    Number[][] n = new Number[][] {{1, 2, 3}, {4, 5, 6}};
    /*System.out.println(n.
    NumericalArray na = new org.fairdatapipeline.objects.NumericalArrayImpl(n);
    System.out.println(na.as2DArray());
    Assertions.assertEquals(n, na.as2DArray());*/
  }
}
