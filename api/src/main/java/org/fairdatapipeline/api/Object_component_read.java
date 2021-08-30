package org.fairdatapipeline.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.parameters.ReadComponent;

/** An object_component for reading from. */
public class Object_component_read extends Object_component {

  Object_component_read(Data_product_read dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_read(Data_product_read dp) {
    super(dp);
  }

  protected void populate_component() {
    this.registryObject_component = this.retrieveObject_component();
    if (this.registryObject_component == null) {
      throw (new IllegalArgumentException(
          "Object Component '"
              + this.component_name
              + "' for FDPObj "
              + this.dp.fdpObject.get_id().toString()
              + " not found in registry."));
    }
  }

  /**
   * get the filePath to read from; only for whole_object component
   *
   * @return Path the Path of the data object.
   */
  public Path readLink() {
    if (!this.whole_object) {
      throw (new IllegalArgumentException(
          "you shouldn't try to read directly from a Data Product with named components."));
    }
    this.been_used = true;
    return this.dp.getFilePath();
  }

  /**
   * get the CleanableFileChannel to read directly from the file. only for whole_object component.
   *
   * @return CleanableFileChannel the filechannel to read from.
   * @throws IOException
   */
  public CleanableFileChannel readFileChannel() throws IOException {
    if (!this.whole_object) {
      throw (new IllegalArgumentException(
          "you shouldn't try to read directly from a Data Product with named components."));
    }
    this.been_used = true;
    return this.getFileChannel();
  }

  /**
   * read the Estimate that was stored as this component in a TOML file.
   *
   * @return the estimate as Number
   */
  public Number readEstimate() {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      data =
          dp.coderun.stdApi.parameterDataReader.read(
              fileChannel, this.registryObject_component.getName());
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open the file."));
    }
    return data.getEstimate();
  }

  /**
   * read the Distribution that was stored as this component in a TOML file.
   *
   * @return the Distribution
   */
  public Distribution readDistribution() {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      data = this.dp.coderun.stdApi.parameterDataReader.read(fileChannel, this.component_name);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for read " + e.toString()));
    }
    return data.getDistribution();
  }

  /**
   * read the Samples that were stored as this component in a TOML file.
   *
   * @return the Samples object
   */
  public List<Number> readSamples() {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      data = this.dp.coderun.stdApi.parameterDataReader.read(fileChannel, this.component_name);
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

  protected void register_me_in_registry() {
    // i am a read component, so i am already registered.
  }

  protected void register_me_in_code_run() {
    if (this.been_used) this.dp.coderun.addInput(this.registryObject_component.getUrl());
  }
}
