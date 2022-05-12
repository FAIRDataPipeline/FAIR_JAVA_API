package org.fairdatapipeline.api;

import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.objects.NumericalArray;

public class Object_component_write_nc extends Object_component_write {
  Object_component_write_nc(Data_product_write_nc dp, String component_name) {
    super(dp, component_name);
  }

  public void writeArray(NumericalArray na) {
    NetcdfBuilder nBuilder = ((Data_product_write_nc) this.dp).getNetCDFBuilder();
    // NetCDFGroup nGroup = nBuilder.makeGroup(this.component_name);
    // nGroup
  }
}
