package org.fairdatapipeline.api;

import java.io.IOException;
import java.util.Arrays;
import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.NumericalArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.Variable;

public class Object_component_read_nc extends Object_component_read {
  private static final Logger LOGGER = LoggerFactory.getLogger(Object_component_read_nc.class);
  int[] origin_read_pointer;
  boolean eof = false;
  Variable variable;

  /**
   * @param dp the data product that we belong to
   * @param variableName the variable name (groupname and local name) of the variable to read.
   * @throws IOException if the file cannot be opened for reading.
   */
  Object_component_read_nc(Data_product dp, VariableName variableName) throws IOException {
    super(dp, variableName.getFullPath());
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    this.variable = r.getVariable(variableName);
    origin_read_pointer = r.getShape(this.variable);
    for (int i = 0; i < origin_read_pointer.length; i++) origin_read_pointer[i] = 0;
  }

  /**
   * read the complete data
   *
   * @return
   * @throws IOException
   */
  public NumericalArray readArray() throws IOException {
    this.been_used = true;
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    return r.read(r.getVariable(this.component_name));
  }

  void check_match(int[] shape, int[] vshape, int from) {
    if (shape.length != vshape.length)
      throw new IllegalArgumentException("shape has to match number of dims in variable");
    for (int i = from; i < shape.length; i++) {
      if (shape[i] != vshape[i])
        throw new IllegalArgumentException(
            "final dimensions in shape must match final dimensions in variable");
    }
    LOGGER.trace("shape and vshape match from {}", from);
  }

  /**
   * read a slice of data, of size 'shape'.
   *
   * @param shape
   * @return
   * @throws IOException
   */
  public NumericalArray readArray(int[] shape) throws IOException {
    if (shape.length != origin_read_pointer.length)
      throw new IllegalArgumentException("shape has to match number of dims in variable");
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    int[] vshape = r.getShape(this.variable);
    this.been_used = true;
    //
    NumericalArray na = r.read(this.variable, origin_read_pointer, shape);
    // which is the 'update' dimension; ie the dimension that we increment with each read?
    int update_dim = shape.length - 1;
    LOGGER.trace("readArray({})", Arrays.toString(shape));
    LOGGER.trace("origin_read_pointer: {}", Arrays.toString(origin_read_pointer));
    LOGGER.trace("variable shape: {}", Arrays.toString(vshape));
    for (int i = 0; i < shape.length; i++) {
      if (shape[i] != 1) {
        if (shape[i] == vshape[i]) {
          // make sure further shape matches vshape
          check_match(shape, vshape, i + 1);
          if (i > 0) {
            update_dim = i - 1;
            LOGGER.trace("i = {}; update_dim will be i-1={}", i, i - 1);
          } else {
            LOGGER.trace("i = 0; EOF=true");
            eof = true;
          }
        } else if (shape[i] > vshape[i]) {
          throw new IllegalArgumentException(
              "shape[" + i + "] is larger than the variable shape[" + i + "]");
        } else {
          check_match(shape, vshape, i + 1);
          update_dim = i;
        }
        break;
      }
    }
    LOGGER.trace("update dim: {}", update_dim);
    LOGGER.trace("EOF? {}", eof);
    if (!eof) {
      origin_read_pointer[update_dim] += shape[update_dim];
      LOGGER.trace(
          "updated origin_read_pointer[{}]: += {} becomes {}",
          update_dim,
          shape[update_dim],
          origin_read_pointer[update_dim]);
      while (update_dim > 0 && origin_read_pointer[update_dim] >= vshape[update_dim]) {
        LOGGER.trace(
            "reducing origin_read_pointer[{}] to 0, (and increasing origin_read_pointer[{}] to {})",
            update_dim,
            update_dim - 1,
            origin_read_pointer[update_dim - 1] + 1);
        origin_read_pointer[update_dim] = 0;
        origin_read_pointer[update_dim - 1] += 1;
        update_dim -= 1;
      }
      if (update_dim == 0 && origin_read_pointer[0] >= vshape[0]) eof = true;
    }
    return na;
  }
}
