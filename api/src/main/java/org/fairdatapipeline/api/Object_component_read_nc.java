package org.fairdatapipeline.api;

import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.objects.NumericalArray;

public class Object_component_read_nc extends Object_component_read {
    int[] origin_read_pointer;


    Object_component_read_nc(Data_product dp, String component_name) {
        super(dp, component_name);
        Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
        NetcdfReader r = dpr.getNetcdfReader();
        origin_read_pointer = r.getShape(this.component_name);
        for(int i=0;i<origin_read_pointer.length;i++) origin_read_pointer[i] = 0;
    }

  public NumericalArray readArray() {
      Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
      NetcdfReader r = dpr.getNetcdfReader();
      return r.read(this.component_name);
  }

    public NumericalArray readArray(int[] shape) {
        Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
        NetcdfReader r = dpr.getNetcdfReader();

        return r.read(this.component_name, origin_read_pointer, shape);
    }


}
