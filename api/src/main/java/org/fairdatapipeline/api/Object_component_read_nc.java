package org.fairdatapipeline.api;

import org.fairdatapipeline.objects.NumericalArray;

public class Object_component_read_nc extends Object_component_read {
  Object_component_read_nc(Data_product dp, String component_name) {
    super(dp, component_name);
  }

  public void readArray(NumericalArray na) {
    /*try(NetcdfBuilder nBuilder = ((Data_product_write_nc) this.dp).getNetCDFBuilder()) {
        NetCDFGroup nGroup = nBuilder.makeGroup(this.component_name);
        nGroup.
    }*/
  }
}
