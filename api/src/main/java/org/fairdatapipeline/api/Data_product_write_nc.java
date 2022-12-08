package org.fairdatapipeline.api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.TableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.write.Nc4Chunking;

/**
 * Data_product_write_nc is created by Coderun: {@link Coderun#get_dp_for_write_nc(String)}
 *
 * <p>Upon {@link Coderun#close()} it will register itself and its components in the registry, and
 * then register its components in the coderun.
 */
public class Data_product_write_nc extends Data_product_write {
  private static final Logger LOGGER = LoggerFactory.getLogger(Data_product_write_nc.class);
  private NetcdfBuilder netCDFBuilder;
  private NetcdfWriter netCDFWriter;
  private static final String TOOLATE =
      "too late.. you can only set chunking details BEFORE any of the components has prepared any array variables.";

  public enum chunkingStrategies {
    GRIB,
    NONE,
    STANDARD;

    private Nc4Chunking.Strategy getNc4Strategy() {
      switch (this) {
        case GRIB:
          return Nc4Chunking.Strategy.grib;
        case NONE:
          return Nc4Chunking.Strategy.none;
        case STANDARD:
          return Nc4Chunking.Strategy.standard;
      }
      return Nc4Chunking.Strategy.standard;
    }
  }

  private chunkingStrategies chunkingStrategy = chunkingStrategies.STANDARD;
  private int nc4deflateLevel = 2;
  private boolean nc4shuffle = true;

  Data_product_write_nc(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun, "nc");
  }

  public Object_component_write_array getComponent(@NonNull DimensionalVariableDefinition nadef) {
    if (componentMap.containsKey(nadef.getVariableName().getFullPath())) {
      // TODO: check that component in map is an Object_component_write_array
      return (Object_component_write_array) componentMap.get(nadef.getVariableName().getFullPath());
    }
    Object_component_write_array dc;
    dc = new Object_component_write_array(this, nadef);
    LOGGER.trace(
        "new Object_component (DimensionalVariable) created: {}",
        nadef.getVariableName().getFullPath());
    componentMap.put(nadef.getVariableName().getFullPath(), dc);
    return dc;
  }

  public Object_component_write_table getComponent(@NonNull TableDefinition tabledef) {
    if (componentMap.containsKey(tabledef.getGroupName().toString())) {
      // TODO: check that component in map is an Object_component_write_table
      return (Object_component_write_table) componentMap.get(tabledef.getGroupName().toString());
    }
    Object_component_write_table tc;
    tc = new Object_component_write_table(this, tabledef);
    LOGGER.trace("new Object_component (table) created: {}", tabledef.getGroupName());
    componentMap.put(tabledef.getGroupName().toString(), tc);
    return tc;
  }

  public Object_component_write_dimension getComponent(
      @NonNull CoordinateVariableDefinition dimdef) {
    if (componentMap.containsKey(dimdef.getVariableName().getFullPath())) {
      // TODO: check that component in map is an Object_component_write_nc
      return (Object_component_write_dimension)
          componentMap.get(dimdef.getVariableName().getFullPath());
    }
    Object_component_write_dimension dc;
    dc = new Object_component_write_dimension(this, dimdef);
    LOGGER.trace(
        "new Object_component (dimension) created: {}", dimdef.getVariableName().getFullPath());
    componentMap.put(dimdef.getVariableName().getFullPath(), dc);
    return dc;
  }

  private void executeOnCloseDP() {
    this.do_hash();
  }

  public void setChunkingStrategy(chunkingStrategies chunkingStrategy) {
    if (this.netCDFWriter != null || this.netCDFBuilder != null)
      throw (new IllegalActionException(TOOLATE));
    this.chunkingStrategy = chunkingStrategy;
  }

  public void setChunkingDeflate(int deflateLevel) {
    if (this.netCDFWriter != null || this.netCDFBuilder != null)
      throw (new IllegalActionException(TOOLATE));
    this.nc4deflateLevel = deflateLevel;
  }

  public void setChunkingShuffle(boolean shuffle) {
    if (this.netCDFWriter != null || this.netCDFBuilder != null)
      throw (new IllegalActionException(TOOLATE));
    this.nc4shuffle = shuffle;
  }

  NetcdfBuilder getNetCDFBuilder() {
    if (this.netCDFWriter != null) {
      throw (new IllegalActionException(
          "you've already started writing data to the netCDF file, you can't go back preparing arrays once writing has started."));
    }
    this.been_used = true;
    Runnable onClose = this::executeOnCloseDP;
    if (this.netCDFBuilder == null) {
      this.netCDFBuilder =
          new NetcdfBuilder(
              this.getFilePath().toString(),
              this.chunkingStrategy.getNc4Strategy(),
              this.nc4deflateLevel,
              this.nc4shuffle,
              onClose);
    }
    return this.netCDFBuilder;
  }

  NetcdfWriter getNetCDFWriter() {
    if (this.netCDFWriter != null) return this.netCDFWriter;
    if (this.netCDFBuilder == null) {
      throw (new IllegalActionException(
          "you must prepare one or more arrays to write to before trying to write data to netCDF"));
    }
    Runnable onClose =
        this::executeOnCloseDP; // is it going to get called twice, both for the closing of
    // netcdfwriter AND for netcdfBuilder?
    this.netCDFWriter = new NetcdfWriter(this.netCDFBuilder, onClose);
    this.netCDFBuilder.close();
    this.netCDFBuilder = null;
    this.componentMap.forEach((key, value) -> ((Object_component_write) value).write_preset_data());
    return this.netCDFWriter;
  }

  void closeNetcdfBuilder() {
    if (this.netCDFWriter != null) {
      LOGGER.trace("closeNetcdfBuilder() closing writer");
      this.netCDFWriter.close();
      this.netCDFWriter = null;
    } else if (this.netCDFBuilder != null) {
      LOGGER.trace("closeNetcdfBuilder() closing builder");
      this.netCDFBuilder.close();
      this.netCDFBuilder = null;
    } else {
      LOGGER.trace("closeNetcdfBuilder() doing nothing");
    }
  }

  @Override
  public void close() {
    LOGGER.trace("close()");
    closeNetcdfBuilder();
    super.close();
  }
}
