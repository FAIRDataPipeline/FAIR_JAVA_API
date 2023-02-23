package org.fairdatapipeline.api;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

public class Object_component_write_array extends Object_component_write {
  private static final Logger LOGGER = LoggerFactory.getLogger(Object_component_write_array.class);
  DimensionalVariableDefinition nadef;
  Variable variable;
  int[] write_pointer;
  int[] shape;
  boolean eof = false;

  Object_component_write_array(Data_product_write_nc dp, DimensionalVariableDefinition nadef) {
    super(dp, nadef.getVariableName().toString());
    this.nadef = nadef;
    NetcdfBuilder nBuilder = ((Data_product_write_nc) this.dp).getNetCDFBuilder();
    nBuilder.prepare(nadef);
  }

  private void getVariable() {
    if (this.variable != null) return;
    this.been_used = true;
    NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
    this.variable = nWriter.getVariable(this.nadef.getVariableName());
    this.shape = this.variable.getShape();
    this.write_pointer = new int[this.shape.length];
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
   * writes the whole data (if nadat.getShape equals variable.getShape) or write a slice and update
   * a write-pointer.
   *
   * @param nadat the data to be written
   * @throws EOFException if we are trying to write beyong the end of the file.
   */
  public void writeArrayData(NumericalArray nadat) throws IOException {
    LOGGER.trace("writeArrayData");
    if (eof) throw (new EOFException("trying to write beyond end of data"));
    NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
    if (this.variable == null)
      this.getVariable(); // this.variable = nWriter.getVariable(this.component_name, this.nadef);
    if (Arrays.equals(nadat.getShape(), this.variable.getShape())) {
      // nadat contains ALL the data for the variable.. write it from its start.
      try {
        nWriter.writeArrayData(variable, nadat);
      } catch (InvalidRangeException e) {
        // TODO: error handling
      }
      eof = true;
    } else {
      try {
        Array a = Array.makeFromJavaArray(nadat.asObject());
        while (a.getShape().length < this.shape.length) {
          a = Array.makeArrayRankPlusOne(a);
          if (LOGGER.isTraceEnabled())
            LOGGER.trace("makeArrayRankPlusOne: {}", Arrays.toString(a.getShape()));
        }

        if (LOGGER.isTraceEnabled())
          LOGGER.trace(
              "nWriter.writeArrayData() a = {}; write_pointer: {}",
              Arrays.toString(a.getShape()),
              Arrays.toString(this.write_pointer));
        nWriter.writeArrayData(variable, a, this.write_pointer);
        int update_dimension = this.shape.length - 1;
        for (int i = 0; i < this.shape.length; i++) {
          if (a.getShape()[i] != 1) {
            if (a.getShape()[i] == this.shape[i]) {
              update_dimension = i - 1;
            } else {
              update_dimension = i;
            }
            break;
          }
        }
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("line 96 .. update_dimension: {}", update_dimension);
          LOGGER.trace("this.shape: {}", Arrays.toString(this.shape));
          LOGGER.trace("a.shape: {}", Arrays.toString(a.getShape()));
          LOGGER.trace("update_dim: {}", update_dimension);
        }
        this.write_pointer[update_dimension] += a.getShape()[update_dimension];
        while (this.shape[update_dimension] != 0
            && this.write_pointer[update_dimension] >= this.shape[update_dimension]) {
          if (update_dimension == 0) {
            eof = true;
            return;
          }
          update_dimension -= 1;
          this.write_pointer[update_dimension] += 1;
          this.write_pointer[update_dimension + 1] = 0;
        }
      } catch (InvalidRangeException e) {
        // TODO: error handling
      }
    }
  }

  void write_preset_data() {
    // do nothing. nadef doesn't have preset data.
  }
}
