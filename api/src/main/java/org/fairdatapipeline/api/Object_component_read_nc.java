package org.fairdatapipeline.api;

import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.NumericalArray;
import ucar.nc2.Variable;

public class Object_component_read_nc extends Object_component_read {
  int[] origin_read_pointer;
  Variable variable;

  // Object_component_read_nc(Data_product dp, String component_name) {
  //    this(dp, new VariableName(component_name));
  // }

  Object_component_read_nc(Data_product dp, VariableName variableName) {
    super(dp, variableName.getFullPath());
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    this.variable = r.getVariable(variableName);
    origin_read_pointer = r.getShape(this.variable);
    for (int i = 0; i < origin_read_pointer.length; i++) origin_read_pointer[i] = 0;
  }

  public NumericalArray readArray() {
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    return r.read(r.getVariable(this.component_name));
  }

  public NumericalArray readArray(int[] shape) {
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();

    return r.read(this.variable, origin_read_pointer, shape);
  }
}
