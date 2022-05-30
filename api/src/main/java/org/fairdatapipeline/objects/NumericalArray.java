package org.fairdatapipeline.objects;

public interface NumericalArray {
  Object asObject();

  int[] getShape();

  public NumericalArray matchShape(int[] longershape);

  Number[] as1DArray();

  Number[][] as2DArray();

  Number[][][] as3DArray();

  Number[][][][] as4DArray();

  Number[][][][][] as5DArray();
}
