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
  boolean[] eof;

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
    this.eof = new boolean[this.length];
    this.origin_read_pointer = new int[this.length];
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
    this.eof[variableNr] = true;
    return r.readObj(variables.get(variableNr));
  }

  public Object readData(int variableNr, int num_items) throws IOException {
    this.been_used = true;
    Data_product_read_nc dpr = (Data_product_read_nc) this.dp;
    NetcdfReader r = dpr.getNetcdfReader();
    if (this.variables == null) this.getVariables(r);
    int[] origin = new int[] {origin_read_pointer[variableNr]};
    int[] shape = new int[] {num_items};
    Object o = r.readObj(variables.get(variableNr), origin, shape);
    this.origin_read_pointer[variableNr] = this.origin_read_pointer[variableNr] + num_items;
    if (num_items >= this.length) this.eof[variableNr] = true;
    return o;
  }
}
