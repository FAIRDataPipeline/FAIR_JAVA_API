package org.fairdatapipeline.api;

import java.io.IOException;
import java.util.List;
import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.TableDefinition;
import ucar.nc2.Variable;

public class Object_component_read_table extends Object_component_read {
  int[] origin_read_pointer;
  boolean eof = false;
  List<Variable> variables;
  int length;

  /**
   * @param dp the data product that we belong to
   * @param variableName the variable name (groupname and local name) of the variable to read.
   * @throws IOException if the file cannot be opened for reading.
   */
  Object_component_read_table(Data_product dp, VariableName variableName) throws IOException {
    super(dp, variableName.getFullPath());
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    // do we need to check we are reading a table?
    variables = r.getVariables(variableName);
    this.length = variables.get(0).getShape()[0];
  }

  public TableDefinition getTabledef() throws IOException {
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    return r.getTable(new VariableName(this.component_name));
  }

  public Object readData(int variableNr) throws IOException {
    this.been_used = true;
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    return r.readObj(variables.get(variableNr));
  }
}
