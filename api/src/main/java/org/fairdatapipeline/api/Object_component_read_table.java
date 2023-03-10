package org.fairdatapipeline.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.fairdatapipeline.netcdf.NetcdfComponentNotfoundException;
import org.fairdatapipeline.netcdf.NetcdfComponentWrongTypeException;
import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.TableDefinition;
import ucar.nc2.Variable;

public class Object_component_read_table extends Object_component_read {
  int[] origin_read_pointer;
  boolean eof = false;
  TableDefinition tableDef;
  List<Variable> variables;
  int length;

  /**
   * @param dp the data product that we belong to
   * @param variableName the variable name (groupname and local name) of the variable to read.
   * @throws IOException if the file cannot be opened for reading.
   */
  Object_component_read_table(Data_product dp, VariableName variableName)
      throws IOException, NetcdfComponentNotfoundException, NetcdfComponentWrongTypeException {
    super(dp, variableName.getFullPath());
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    this.tableDef = r.getTable(variableName);
    this.length = tableDef.getSize();
  }

  public TableDefinition getTabledef() {
    return this.tableDef;
  }

  void getVariables(NetcdfReader r) {
    this.variables = new ArrayList<>();
    for (int i = 0; i < this.tableDef.getColumns().length; i++) {
      this.variables.add(r.getVariable(this.tableDef.getVariableName(i)));
    }
  }

  public Object readData(int variableNr) throws IOException {
    this.been_used = true;
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    if (this.variables == null) this.getVariables(r);
    return r.readObj(variables.get(variableNr));
  }
}
