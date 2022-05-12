package org.fairdatapipeline.api;

import static java.nio.file.StandardOpenOption.*;

import java.util.Objects;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.netcdf.NetcdfBuilder;
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

  Data_product_write_nc(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun, "nc");
  }

  /**
   * Obtain an Object_component for writing.
   *
   * @param component_name the name of the object component.
   * @return the Object_component_write object.
   */
  public Object_component_write_nc getComponent(String component_name) {
    if (componentMap.containsKey(component_name))
      return (Object_component_write_nc) componentMap.get(component_name);
    Object_component_write_nc dc;
    dc = new Object_component_write_nc(this, Objects.requireNonNull(component_name));
    componentMap.put(component_name, dc);
    return dc;
  }

  NetcdfBuilder getNetCDFBuilder() {
    this.been_used = true;
    if (this.netCDFBuilder == null) {
      this.netCDFBuilder = new NetcdfBuilder(this.getFilePath());
    }
    // Runnable onClose = this::executeOnCloseNetCDFBuilder;
    return this.netCDFBuilder;
  }

  void closeNetcdfBuilder() {
    // this.netCDFBuilder.build??
  }

  @Override
  public void close() {
    closeNetcdfBuilder();
    super.close();
  }
}
