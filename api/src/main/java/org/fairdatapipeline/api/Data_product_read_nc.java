package org.fairdatapipeline.api;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.fairdatapipeline.netcdf.NetcdfReader;
import org.fairdatapipeline.netcdf.VariableName;

public class Data_product_read_nc extends Data_product_read {
  NetcdfReader reader;

  Data_product_read_nc(String dataProduct_name, Coderun coderun) {
    super(dataProduct_name, coderun);
  }

  /**
   * Obtain an Object_component_read_nc for reading.
   *
   * @param component_name the name of the object component.
   * @return the Object_component_read_nc object.
   * @throws IOException if the file cannot be opened for reading.
   */
  public Object_component_read_nc getComponent(@Nonnull String component_name) throws IOException {
    if (componentMap.containsKey(component_name))
      return (Object_component_read_nc) componentMap.get(component_name);
    Object_component_read_nc dc;
    VariableName vn = new VariableName(component_name);
    dc = new Object_component_read_nc(this, vn);
    componentMap.put(component_name, dc);
    return dc;
  }

  NetcdfReader getNetcdfReader() throws IOException {
    this.been_used = true;
    if (this.reader == null) {
      this.reader = new NetcdfReader(this.getFilePath().toString());
    }
    return reader;
  }

  void closeNetcdfReader() {
    // this.netCDFBuilder.build??
  }

  @Override
  public void close() {
    closeNetcdfReader();
    super.close();
  }
}
