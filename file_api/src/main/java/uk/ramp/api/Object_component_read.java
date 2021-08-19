package uk.ramp.api;

import java.io.IOException;
import java.util.List;
import uk.ramp.distribution.Distribution;
import uk.ramp.file.CleanableFileChannel;
import uk.ramp.parameters.ReadComponent;

public class Object_component_read extends Object_component_RW {

  public Object_component_read(Data_product_read dp, String component_name) {
    super(dp, component_name);
  }

  public Object_component_read(Data_product_read dp) {
    super(dp);
  }

  /*private Object_component_read(Data_product_read dp, String component_name, boolean whole_object) {
      super(dp, component_name, whole_object);
  }*/

  protected void populate_component() {
    this.object_component = this.retrieveObject_component();
    if (this.object_component == null) {
      throw (new IllegalArgumentException(
          "Object Component '"
              + this.component_name
              + "' for FDPObj "
              + this.dp.fdpObject.get_id().toString()
              + " not found in registry."));
    }
  }

  public Number readEstimate() {
    System.out.println(".readEstimate()");
    ReadComponent data;
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      System.out.println("\n\nreadEstimate\n\nopened the fileChannel - trying to read component");
      System.out.println("component_name: " + this.object_component.getName());
      data =
          dp.fileApi.stdApi.parameterDataReader.read(fileChannel, this.object_component.getName());
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open the file."));
    }
    if (data == null) {
      System.out.println("parameterDataReader.read() returned NULL");
    }
    return data.getEstimate();
  }

  public Distribution readDistribution() {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      data = this.dp.fileApi.stdApi.parameterDataReader.read(fileChannel, this.component_name);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for read " + e.toString()));
    }
    return data.getDistribution();
  }

  public List<Number> readSamples() {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      data = this.dp.fileApi.stdApi.parameterDataReader.read(fileChannel, this.component_name);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for read " + e.toString()));
    }
    return data.getSamples();
  }

  /*public NumericalArray readArray(String dataProduct, String component) {
      Path filepath = fileApi.fileApi.getFilePathForRead(dataProduct, component);
      // HDF5 hdf5 = new HDF5(filepath);
      return null; // hdf5.read(component);
  }

  public Table<Integer, String, Number> readTable(String dataProduct, String component) {
      throw new UnsupportedOperationException();
  }*/

  protected void register_me_in_code_run_session(Code_run_session crs) {
    crs.addInput(this.object_component.getUrl());
  }
}
