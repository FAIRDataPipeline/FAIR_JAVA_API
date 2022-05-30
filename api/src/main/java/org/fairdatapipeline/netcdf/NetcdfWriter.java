package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.Arrays;

import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.util.IO;
import ucar.nc2.write.NetcdfFormatWriter;


public class NetcdfWriter implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(NetcdfBuilder.class);
    private static final Cleaner cleaner = Cleaner.create();
    private final Cleanable cleanable;
    private final NetcdfWriterWrapper netcdfWriterWrapper;


    public NetcdfWriter(NetcdfBuilder b, Runnable onClose) {
        this.netcdfWriterWrapper = new NetcdfWriterWrapper(b, onClose);
        this.cleanable = cleaner.register(this, this.netcdfWriterWrapper);
    }

    // Defining a resource that requires cleaning
    private static class NetcdfWriterWrapper implements Runnable {
        private final NetcdfFormatWriter writer;
        private final NetcdfFile netcdfFile;

        private final Runnable runOnClose;

        NetcdfWriterWrapper(NetcdfBuilder b, Runnable runOnClose) {
            try {
                this.writer = b.build();
                this.netcdfFile = this.writer.getOutputFile();
            }catch(Exception e) {
                throw(new IllegalActionException("failed to build the netCDF file", e));
            }

            this.runOnClose = runOnClose;
        }

        // Invoked by close method or cleaner
        @Override
        public void run() {
            logger.trace("run() invoked by cleaner");
            runOnClose.run();
            try {
                this.writer.close();
            }catch(IOException e) {
                logger.error("can't close the netCDF writer.", e);
            }
        }
    }

    public void writeArrayData(String group_name, NumericalArrayDefinition nadef, NumericalArray nadat) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("group " + group_name + " not found for writing."));
        Variable v = g.findVariableLocal(nadef.getName());
        if(v == null) throw(new IllegalActionException("variable " + nadef.getName() + " (in group " + group_name + ") not found for writing."));
        //Array data = NetcdfDataType.translate_array(nadef.getDataType(), nadef.getDimension_sizes(), nadat.asOA());
        // the below works for primitive arrays.. if it might contain non primitives we need NetcdfDataType.translate_array instead.
        Array data = ucar.ma2.Array.makeFromJavaArray(nadat.asObject());
        try{
            this.netcdfWriterWrapper.writer.write(v, data);
        }catch(IOException e) {
            throw(new IllegalActionException("failed to write array data to file for group " + group_name));
        }catch(InvalidRangeException e) {
            throw(new IllegalActionException("invalid range for array data for group " + group_name));
        }
    }

    public void writeArrayDataPart(String group_name, NumericalArrayDefinition nadef, NumericalArray nadat, int[] origin) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("group " + group_name + " not found for writing."));
        Variable v = g.findVariableLocal(nadef.getName());
        if(v == null) throw(new IllegalActionException("variable " + nadef.getName() + " (in group " + group_name + ") not found for writing."));
        //Array data = NetcdfDataType.translate_array(nadef.getDataType(), nadef.getDimension_sizes(), nadat.asOA());
        // the below works for primitive arrays.. if it might contain non primitives we need NetcdfDataType.translate_array instead.
        Array data = ucar.ma2.Array.makeFromJavaArray(nadat.asObject());
        try{
            this.netcdfWriterWrapper.writer.write(v, origin, data);
        }catch(IOException e) {
            throw(new IllegalActionException("failed to write array data to file for group " + group_name));
        }catch(InvalidRangeException e) {
            throw(new IllegalActionException("invalid range for array data for group " + group_name));
        }
    }

    public NetcdfWriteHandle get_write_handle(String group_name, NumericalArrayDefinition nadef) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("group " + group_name + " not found for writing."));
        Variable v = g.findVariableLocal(nadef.getName());
        if(v == null) throw(new IllegalActionException("variable " + nadef.getName() + " (in group " + group_name + ") not found for writing."));
        return new NetcdfWriteHandle(v, this.netcdfWriterWrapper.writer);
    }


    public void writeDimensionVariables(String group_name, NumericalArrayDefinition nadef) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("can't find group " + group_name));
        int num_dims = nadef.getDimension_sizes().length;
        for(int i=0;i<num_dims;i++) {
            logger.debug("writeDimensionVariables i=" + i);

            Variable v = g.findVariableLocal(nadef.getDimension_names()[i]);
            if(v == null) throw(new IllegalActionException("can't find variable " + nadef.getDimension_names()[i] + " (in group " + group_name + ")"));
            logger.debug("type: " + nadef.getDimension_values()[i].getClass().getSimpleName());
            Array data = NetcdfDataType.translate_array(nadef.getDimension_values()[i]);
            // Array.makeFromJavaArray only works for primitives.. i want it to work for Strings too, and need to use NetcdfDataType.translate_array
            //Array data = Array.makeFromJavaArray(nadef.getDimension_values()[i]);

            try {
                this.netcdfWriterWrapper.writer.write(v, data);
            }catch(IOException e) {
                throw(new IllegalActionException("failed to write variable data to file for group " + group_name + ", dim var no " + i));
            }catch(InvalidRangeException e) {
                throw(new IllegalActionException("invalid range for group " + group_name + ", dim var no " + i));
            }
        }
    }



    @Override
    public void close() {
        logger.trace("close()");
        cleanable.clean();
    }

}
