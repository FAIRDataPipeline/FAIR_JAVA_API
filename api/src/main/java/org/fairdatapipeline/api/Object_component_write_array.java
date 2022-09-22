package org.fairdatapipeline.api;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

public class Object_component_write_array extends Object_component_write {
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
    NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
    this.variable = nWriter.getVariable(this.nadef.getVariableName());
    this.shape = this.variable.getShape();
    this.write_pointer = new int[this.shape.length];
  }

  /**
   * writes the whole data (if nadat.getShape equals variable.getShape) or write a slice and update
   * a write-pointer.
   *
   * @param nadat
   * @throws EOFException
   */
  public void writeArrayData(NumericalArray nadat) throws IOException {
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
        while (a.getShape().length < this.shape.length) a = Array.makeArrayRankPlusOne(a);
        nWriter.writeArrayData(variable, a, this.write_pointer);
        int update_dimension = this.shape.length - a.getShape().length - 1;
        System.out.println("this.shape: " + Arrays.toString(this.shape));
        System.out.println("a.shape: " + Arrays.toString(a.getShape()));
        System.out.println("update_dim: " + update_dimension);
        this.write_pointer[update_dimension] += 1;
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
}
