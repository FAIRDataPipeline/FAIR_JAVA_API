package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Group;
import ucar.nc2.Variable;
import ucar.nc2.write.NetcdfFormatWriter;


public class NetcdfWriter implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(NetcdfWriter.class);
    private static final Cleaner cleaner = Cleaner.create();
    // todo: we should not create more cleaners than necessary
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

    public Variable getVariable(String group_name, NumericalArrayDefinition nadef) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("group " + group_name + " not found for writing."));
        Variable v = g.findVariableLocal(nadef.getName());
        if(v == null) throw(new IllegalActionException("variable " + nadef.getName() + " (in group " + group_name + ") not found for writing."));
        return v;
    }

    public void writeArrayData(Variable v, NumericalArray nadat) throws InvalidRangeException {
        writeArrayData(v, nadat, null);
    }

    public void writeArrayData(Variable v, Array data)  throws InvalidRangeException{
        writeArrayData(v, data, null);
    }
    public void writeArrayData(Variable v, NumericalArray nadat, @Nullable int[] origin) throws InvalidRangeException {
        Array data = ucar.ma2.Array.makeFromJavaArray(nadat.asObject());
        writeArrayData(v, data, origin);
    }

    public void writeArrayData(Variable v, Array data, @Nullable int[] origin) throws InvalidRangeException {
        //Array data = NetcdfDataType.translate_array(nadef.getDataType(), nadef.getDimension_sizes(), nadat.asOA());
        // the below works for primitive arrays.. if it might contain non primitives we need NetcdfDataType.translate_array instead.

        try{
            if(origin == null) this.netcdfWriterWrapper.writer.write(v, data);
            else this.netcdfWriterWrapper.writer.write(v, origin, data);
        }catch(IOException e) {
            throw(new IllegalActionException("failed to write array data to file"));
        //}catch(InvalidRangeException e) {
        //    throw(new IllegalActionException("invalid range for array data"));
        }
    }

    public void writeDimensionVariable(String group_name, DimensionDefinitionLocal dimensionDefinition) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("can't find group " + group_name));
        Variable v = g.findVariableLocal(dimensionDefinition.getName());
        if(v == null) throw(new IllegalActionException("can't find variable " + dimensionDefinition.getName() + " (in group " + group_name + ")"));
        Array data = NetcdfDataType.translate_array(dimensionDefinition.getValues());

        try {
            this.netcdfWriterWrapper.writer.write(v, data);
        }catch(IOException e) {
            throw(new IllegalActionException("failed to write dimension values to file for group " + group_name));
        }catch(InvalidRangeException e) {
            throw(new IllegalActionException("invalid range to write dimension values for group " + group_name));
        }
    }

    public void writeDimensionVariables(String group_name, NumericalArrayDefinition nadef) {
        Group g = this.netcdfWriterWrapper.netcdfFile.findGroup(group_name);
        if(g == null) throw(new IllegalActionException("can't find group " + group_name));
        int num_dims = nadef.getDimensions().length;
        for(int i=0;i<num_dims;i++) {
            if(nadef.getDimensions()[i].isLocal()) {
                DimensionDefinitionLocal dimdef = (DimensionDefinitionLocal) nadef.getDimensions()[i];
                Variable v = g.findVariableLocal(dimdef.getName());
                if (v == null)
                    throw (new IllegalActionException("can't find variable " + dimdef.getName() + " (in group " + group_name + ")"));
                Array data = NetcdfDataType.translate_array(dimdef.getValues());
                // Array.makeFromJavaArray only works for primitives.. i want it to work for Strings too, and need to use NetcdfDataType.translate_array
                //Array data = Array.makeFromJavaArray(nadef.getDimension_values()[i]);

                try {
                    this.netcdfWriterWrapper.writer.write(v, data);
                } catch (IOException e) {
                    throw (new IllegalActionException("failed to write variable data to file for group " + group_name + ", dim var no " + i));
                } catch (InvalidRangeException e) {
                    throw (new IllegalActionException("invalid range for group " + group_name + ", dim var no " + i));
                }
            }
        }
    }



    @Override
    public void close() {
        logger.trace("close()");
        cleanable.clean();
    }

}
