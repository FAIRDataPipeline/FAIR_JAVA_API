package org.fairdatapipeline.api;

import java.io.IOException;
import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.NumericalArray;
import ucar.nc2.Variable;

public class Object_component_read_nc extends Object_component_read {
  int[] origin_read_pointer;
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

  public NumericalArray readArray() throws IOException {
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    return r.read(r.getVariable(this.component_name));
  }

  public NumericalArray readArray(int[] shape) throws IOException {
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();

    return r.read(this.variable, origin_read_pointer, shape);
  }
}
