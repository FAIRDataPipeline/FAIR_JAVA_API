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

  private void setNumbers(Object old_with_primitives, Object new_with_numbers, int[] dimensions) {
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

  private Object unPrimitive(Object o) {
    if (!o.getClass().isArray())
      throw (new IllegalArgumentException("unPrimitive must work on an array"));
    // int[] dimensions = checkShape(o, false).stream().flatMapToInt(IntStream::of).toArray();
    Object new_array = Array.newInstance(Number.class, this.shape);
    setNumbers(o, new_array, this.shape);
    return new_array;
  }

  @Override
  public Object asObject() {
    return nDArray;
  }

  @Override
  public Number[] as1DArray() {
    if (Number.class.isAssignableFrom(nDArray.getClass().getComponentType())) {
      return (Number[]) nDArray;
    }
    return (Number[]) unPrimitive(nDArray);
  }

  @Override
  public Number[][] as2DArray() {
    if (Number.class.isAssignableFrom(nDArray.getClass().getComponentType().getComponentType())) {
      return (Number[][]) nDArray;
    }
    return (Number[][]) unPrimitive(nDArray);
  }

  @Override
  public Number[][][] as3DArray() {
    if (Number.class.isAssignableFrom(
        nDArray.getClass().getComponentType().getComponentType().getComponentType())) {
      return (Number[][][]) nDArray;
    }
    return (Number[][][]) unPrimitive(nDArray);
  }

  @Override
  public Number[][][][] as4DArray() {
    if (Number.class.isAssignableFrom(
        nDArray
            .getClass()
            .getComponentType()
            .getComponentType()
            .getComponentType()
            .getComponentType())) {
      return (Number[][][][]) nDArray;
    }
    return (Number[][][][]) unPrimitive(nDArray);
  }

  @Override
  public Number[][][][][] as5DArray() {
    if (Number.class.isAssignableFrom(
        nDArray
            .getClass()
            .getComponentType()
            .getComponentType()
            .getComponentType()
            .getComponentType()
            .getComponentType())) {
      return (Number[][][][][]) nDArray;
    }
    return (Number[][][][][]) unPrimitive(nDArray);
  }
}
