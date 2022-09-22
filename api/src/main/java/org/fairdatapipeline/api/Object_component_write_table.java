package org.fairdatapipeline.api;

import java.io.EOFException;
import java.io.IOException;

import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;

public class Object_component_write_table extends Object_component_write {
    TableDefinition tabledef;
    int[] write_index;
    //boolean[] eof;

    Variable[] variables;

    Object_component_write_table(Data_product_write_nc dp, TableDefinition tabledef) {
        super(dp, tabledef.getGroupName().toString());
        this.tabledef = tabledef;
        NetcdfBuilder nBuilder = ((Data_product_write_nc) this.dp).getNetCDFBuilder();
        nBuilder.prepare(tabledef);
    }

    private void getVariables() {
        if (this.variables != null) return;
        NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
        LocalVariableDefinition[] columns = tabledef.getColumns();
        this.variables = new Variable[columns.length];
        //this.eof = new boolean[columns.length];
        this.write_index = new int[columns.length];
        for(int i=0; i < columns.length; i++) {
            //this.eof[i] = false;
            this.write_index[i] = 0;
            this.variables[i] = nWriter.getVariable(new VariableName(columns[i].getLocalName(), tabledef.getGroupName()));
        }

        //this.shape = this.variable.getShape();
        //this.write_pointer = new int[this.shape.length];
    }

    public void writeData(int column_index, int[] ints) throws IOException {
        writeData((Object) ints, column_index);
    }

    public void writeData(int column_index, double[] doubles) throws IOException {
        writeData((Object) doubles, column_index);
    }


    private void writeData(Object data, int column_index) throws IOException {
        int[] origin = new int[1];
        if (this.tabledef.getSize() != 0 && this.write_index[column_index] >= this.tabledef.getSize()) throw (new EOFException("trying to write beyond end of data"));
        NetcdfWriter nWriter = ((Data_product_write_nc) this.dp).getNetCDFWriter();
        if (this.variables == null) this.getVariables(); // this.variable = nWriter.getVariable(this.component_name, this.nadef);
        if(column_index >= this.variables.length) throw(new IllegalArgumentException("This table doesn't have that many columns.."));
        origin[0] = this.write_index[column_index];
        try {
            Array a = Array.makeFromJavaArray(data);
            nWriter.writeArrayData(this.variables[column_index], a, origin);
            this.write_index[column_index] += a.getShape()[0];
        } catch (InvalidRangeException e) {
            // TODO: error handling
        }
    }
}
