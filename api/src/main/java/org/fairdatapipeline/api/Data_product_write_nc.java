package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.*;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data_product_write_nc is created by Coderun: {@link Coderun#get_dp_for_write_nc(String)}
 *
 * <p>Upon {@link Coderun#close()} it will register itself and its components in the registry, and
 * then register its components in the coderun.
 */
public class Data_product_write_nc extends Data_product_write {
  private static final Logger logger = LoggerFactory.getLogger(Data_product_write_nc.class);
  private NetcdfBuilder netCDFBuilder;
  private NetcdfWriter netCDFWriter;

  Data_product_write_nc(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun, "nc");
  }

  /**
   * Obtain an Object_component for writing.
   *
   * @param component_name the name of the object component.
   * @return the Object_component_write object.
   */
  public Object_component_write_nc getComponent(@NonNull String component_name) {
    if (componentMap.containsKey(component_name)) {
      // TODO: check that component in map is an Object_component_write_nc
      return (Object_component_write_nc) componentMap.get(component_name);
    }

    Object_component_write_nc dc;
    dc = new Object_component_write_nc(this, component_name);
    componentMap.put(component_name, dc);
    return dc;
  }

  private void executeOnCloseDP() {
    this.do_hash();
  }

  NetcdfBuilder getNetCDFBuilder() {
    if(this.netCDFWriter != null) {
      throw(new IllegalActionException("you've already started writing data to the netCDF file, you can't go back preparing arrays once writing has started."));
    }
    this.been_used = true;
    Runnable onClose = this::executeOnCloseDP;
    if (this.netCDFBuilder == null) {
      this.netCDFBuilder = new NetcdfBuilder(this.getFilePath().toString(), onClose);
    }
    return this.netCDFBuilder;
  }

  NetcdfWriter getNetCDFWriter() {
    if(this.netCDFWriter != null) return this.netCDFWriter;
    if (this.netCDFBuilder == null) {
      throw(new IllegalActionException("you must prepare one or more arrays to write to before trying to write data to netCDF"));
    }
    Runnable onClose = this::executeOnCloseDP; // is it going to get called twice, both for the closing of netcdfwriter AND for netcdfBuilder?
    this.netCDFWriter = new NetcdfWriter(this.netCDFBuilder, onClose);
    this.netCDFBuilder.close();
    this.netCDFBuilder = null;
    return this.netCDFWriter;
  }

  void closeNetcdfBuilder() {
    if(this.netCDFWriter != null) {
      this.netCDFWriter.close();
      this.netCDFWriter = null;
    }else if(this.netCDFBuilder != null) {
      this.netCDFBuilder.close();
      this.netCDFBuilder = null;
    }
  }

  @Override
  public void close() {
    closeNetcdfBuilder();
    super.close();
  }
}
