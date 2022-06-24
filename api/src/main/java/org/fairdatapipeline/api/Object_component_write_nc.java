package org.fairdatapipeline.api;

import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriteHandle;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayDefinition;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

import java.io.EOFException;
import java.io.IOException;

public class Object_component_write_nc extends Object_component_write {
  NumericalArrayDefinition nadef;
  Variable variable;
  int[] write_pointer;
  int[] shape;
  int[] fakeShape;
  boolean eof = false;

  Object_component_write_nc(Data_product_write_nc dp, String component_name) {
    super(dp, component_name);
  }

  public void prepareArray(NumericalArrayDefinition nadef){
    this.nadef = nadef;
    NetcdfBuilder nBuilder = ((Data_product_write_nc) this.dp).getNetCDFBuilder();
    nBuilder.prepareArray(this.component_name, nadef);
  }

  private void getVariable() {
    if(this.variable != null) return;
    NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
    this.variable = nWriter.getVariable(this.component_name, this.nadef);
    this.shape = this.variable.getShape();
    this.write_pointer = new int[this.shape.length];
  }

  public void writeArrayData(NumericalArray nadat) throws EOFException {
    if(eof) throw(new EOFException("trying to write beyond end of data"));
    NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
    if(this.variable == null) this.variable = nWriter.getVariable(this.component_name, this.nadef);
    if(nadat.getShape().equals(this.variable.getShape())) {
      // nadat contains ALL the data for the variable.. write it from its start.
      try {
        nWriter.writeArrayData(variable, nadat);
      }catch(InvalidRangeException e) {
        //
      }
      eof = true;
    }else{
      try {
        Array a = Array.makeFromJavaArray(nadat.asObject());
        while(a.getShape().length < this.shape.length) a = Array.makeArrayRankPlusOne(a);
        nWriter.writeArrayData(variable, a, this.write_pointer);
        int update_dimension = this.shape.length - a.getShape().length - 1;
        this.write_pointer[update_dimension] += 1;
        while(this.write_pointer[update_dimension] >= this.fakeShape[update_dimension]) {
          if(update_dimension == 0) {
            eof = true;
            return;
          }
          update_dimension -= 1;
          this.write_pointer[update_dimension] += 1;
          this.write_pointer[update_dimension+1] = 0;
        }
      }catch(InvalidRangeException e) {
        //
      }
    }
  }

  void writeDimensionVariables(NetcdfWriter nWriter) {
    nWriter.writeDimensionVariables(this.component_name, this.nadef);
  }



}
