package org.fairdatapipeline.netcdf;

import com.fasterxml.jackson.core.io.NumberInput;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayImpl;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.write.NetcdfFormatWriter;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

public class NetcdfWriteHandle {
    Variable variable;
    int[] write_pointer;
    int[] shape;
    NetcdfFormatWriter writer;
    boolean eof = false;

    public NetcdfWriteHandle(Variable v, NetcdfFormatWriter w) {
        this.writer = w;
        this.variable = v;
        this.shape = v.getShape();
        this.write_pointer = new int[this.shape.length];
    }

    public void write_data(NumericalArray data) throws IOException {
        if(eof) throw(new EOFException("trying to write beyond end of data"));
        if(data.getShape().equals(variable.getShape())) {
            try {
                Array a = Array.makeFromJavaArray(data.asObject());
                writer.write(variable, a);
                eof = true;
            }catch(InvalidRangeException e) {
                //
            }
        }else{
            try {
                System.out.println("before write.. pointer: " + Arrays.toString(this.write_pointer));
                Array a = Array.makeFromJavaArray(data.asObject());
                while(a.getShape().length < this.shape.length) a = Array.makeArrayRankPlusOne(a);
                writer.write(variable, this.write_pointer, a);
                int update_dimension = this.shape.length - data.getShape().length - 1;
                this.write_pointer[update_dimension] += 1;
                while(this.write_pointer[update_dimension] >= this.shape[update_dimension]) {
                    if(update_dimension == 0) {
                        eof = true;
                        System.out.println("write.. EOF reached");
                        return;
                    }
                    update_dimension -= 1;
                    this.write_pointer[update_dimension] += 1;
                    this.write_pointer[update_dimension+1] = 0;
                }
                System.out.println("after write.. pointer: " + Arrays.toString(this.write_pointer));
            }catch(InvalidRangeException e) {
                //
            }
        }
    }


}
