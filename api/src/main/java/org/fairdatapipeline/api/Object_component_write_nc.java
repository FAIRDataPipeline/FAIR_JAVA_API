package org.fairdatapipeline.api;

import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriteHandle;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayDefinition;

public class Object_component_write_nc extends Object_component_write {
  NumericalArrayDefinition nadef;
  NetcdfWriteHandle write_handle;

  Object_component_write_nc(Data_product_write_nc dp, String component_name) {
    super(dp, component_name);
  }

  public void prepareArray(NumericalArrayDefinition nadef){
    this.nadef = nadef;
    NetcdfBuilder nBuilder = ((Data_product_write_nc) this.dp).getNetCDFBuilder();
    nBuilder.prepareArray(this.component_name, nadef);
  }

  public void writeArrayData(NumericalArray nadat){
    NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
    nWriter.writeArrayData(this.component_name, this.nadef, nadat);
  }

  public void writeArrayDataPart(NumericalArray nadat){
    if(this.write_handle == null) {
      NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
      this.write_handle = nWriter.get_write_handle(this.component_name, this.nadef);
    }

    //nWriter.writeArrayData(this.component_name, this.nadef, nadat);
  }

  void writeDimensionVariables(NetcdfWriter nWriter) {
    nWriter.writeDimensionVariables(this.component_name, this.nadef);
  }



}
