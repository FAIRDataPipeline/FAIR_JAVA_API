package org.fairdatapipeline.objects;

public interface NumericalArray {
  Object[] asOA();

  Number[] as1DArray();

  Number[][] as2DArray();

  Number[][][] as3DArray();

  Number[][][][] as4DArray();

  Number[][][][][] as5DArray();
}
