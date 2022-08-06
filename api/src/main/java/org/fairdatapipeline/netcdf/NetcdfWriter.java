package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.Arrays;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
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
      } catch (Exception e) {
        throw (new IllegalActionException("failed to build the netCDF file", e));
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
      } catch (IOException e) {
        logger.error("can't close the netCDF writer.", e);
      }
    }
  }

  public Variable getVariable(VariableName variableName) {
    Group g = this.netcdfWriterWrapper.netcdfFile.findGroup("/" + variableName.getGroupName());
    if (g == null)
      throw (new IllegalActionException(
          "group /" + variableName.getGroupName() + " not found for writing."));
    Variable v = g.findVariableLocal(variableName.getName());
    if (v == null)
      throw (new IllegalActionException(
          "variable "
              + variableName.getName()
              + " (in group /"
              + variableName.getGroupName()
              + ") not found for writing."));
    return v;
  }

  public void writeArrayData(Variable v, NumericalArray nadat)
      throws InvalidRangeException, IOException {
    writeArrayData(v, nadat, null);
  }

  public void writeArrayData(Variable v, Array data) throws InvalidRangeException, IOException {
    writeArrayData(v, data, null);
  }

  public void writeArrayData(Variable v, NumericalArray nadat, @Nullable int[] origin)
      throws InvalidRangeException, IOException {
    Array data = ucar.ma2.Array.makeFromJavaArray(nadat.asObject());
    writeArrayData(v, data, origin);
  }

  /**
   * write data to the file; from position 'origin' or from the start if origin is null.
   *
   * @param v
   * @param data
   * @param origin
   * @throws InvalidRangeException
   */
  public void writeArrayData(Variable v, Array data, @Nullable int[] origin)
      throws InvalidRangeException, IOException {
    // TODO: check if data fill fit? correct stride and size..
    // Array data = NetcdfDataType.translate_array(nadef.getDataType(), nadef.getDimension_sizes(),
    // nadat.asOA());
    // the below works for primitive arrays.. if it might contain non primitives we need
    // NetcdfDataType.translate_array instead.

    if (origin == null) {
      logger.debug("origin == null");
      this.netcdfWriterWrapper.writer.write(v, data);
    } else {
      if (logger.isDebugEnabled()) {
        String originstring = Arrays.toString(origin);
        logger.debug("origin: {}", originstring);
      }
      this.netcdfWriterWrapper.writer.write(v, origin, data);
    }
    if (logger.isDebugEnabled()) {
      String datashape = Arrays.toString(data.getShape());
      String vshape = Arrays.toString(v.getShape());
      logger.trace("data.shape: {}", datashape);
      logger.trace("v.shape: {}", vshape);
    }
  }

  public void writeDimensionVariable(CoordinateVariableDefinition coordinateVariable) {
    if (coordinateVariable.getValues() == null) return;
    Group g =
        this.netcdfWriterWrapper.netcdfFile.findGroup(
            coordinateVariable.getVariableName().getGroupName());
    if (g == null)
      throw (new IllegalActionException(
          "can't find group " + coordinateVariable.getVariableName().getGroupName()));
    Variable v = g.findVariableLocal(coordinateVariable.getVariableName().getName());
    if (v == null)
      throw (new IllegalActionException(
          "can't find variable " + coordinateVariable.getVariableName()));
    Array data = NetcdfDataType.translateArray(coordinateVariable.getValues());

    try {
      this.netcdfWriterWrapper.writer.write(v, data);
    } catch (IOException e) {
      throw (new IllegalActionException(
          "failed to write dimension values to file for variable "
              + coordinateVariable.getVariableName()));
    } catch (InvalidRangeException e) {
      throw (new IllegalActionException(
          "invalid range to write dimension values for variable "
              + coordinateVariable.getVariableName()));
    }
  }

  @Override
  public void close() {
    logger.trace("close()");
    cleanable.clean();
  }
}
