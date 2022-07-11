package org.fairdatapipeline.objects;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import org.fairdatapipeline.api.IllegalActionException;

public class NumericalArrayImpl implements NumericalArray {
  private final Object nDArray;
  private int[] shape;

  public NumericalArrayImpl(Object nDArray) {
    if (!nDArray.getClass().isArray()) throw (new IllegalActionException("no array"));
    this.shape = checkShape(nDArray, true).stream().mapToInt(Integer::intValue).toArray();
    this.nDArray = nDArray;
  }

  @Override
  public int[] getShape() {
    return this.shape;
  }

  /**
   * wrap my current array in extra dimensions until it matches the number of dimensions in shape.
   * the added (top level) dimensions will have length 1. make sure that the dimensions that already
   * existed have the same length as in shape. (the first (top-level) lengths in shape are ignored)
   * ie. if current shape is {3, 4} and longershape is {5, 3, 4}, we will return a NumericalArray
   * with shape {1, 3, 4} (the array with shape {1, 3, 4} can we written to a variable with shape
   * {5, 3, 4} with offsets {0, 0, 0} up to {4, 0, 0})
   *
   * @param longershape the shape we want to match.
   * @return a new NumericalArray with the number of dimensions of longershape.
   */
  @Override
  public NumericalArray matchShape(int[] longershape) {
    if (longershape.length <= this.shape.length)
      throw (new IllegalArgumentException(
          "matchShape(): the asked for longershape must be longer than the current shape."));
    if (!Arrays.equals(
        this.shape,
        0,
        this.shape.length,
        longershape,
        longershape.length - shape.length,
        longershape.length))
      throw (new IllegalArgumentException(
          "the non-padded/added right-hand-side dimensions in longershape must match current shape!"));
    Object r = nDArray;
    for (int i = this.shape.length; i < longershape.length; i++) {
      r = new Object[] {r};
    }
    return new NumericalArrayImpl(r);
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

  @Override
  public Object asObject() {
    return nDArray;
  }

  @Override
  public Number[] as1DArray() {
    return (Number[]) nDArray;
  }

  @Override
  public Number[][] as2DArray() {
    return (Number[][]) nDArray;
  }

  @Override
  public Number[][][] as3DArray() {
    return (Number[][][]) nDArray;
  }

  @Override
  public Number[][][][] as4DArray() {
    return (Number[][][][]) nDArray;
  }

  @Override
  public Number[][][][][] as5DArray() {
    return (Number[][][][][]) nDArray;
  }
}
