package org.fairdatapipeline.api;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.fairdatapipeline.dataregistry.content.RegistryObject_component;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.estimate.ImmutableEstimate;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.samples.Samples;

/** the object component class for reading data from data product (components or whole objects) */
public class Object_component_write extends Object_component {

  Object_component_write(Data_product dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_write(Data_product dp) {
    super(dp, "whole_object", true);
  }

  protected void populate_component() {
    this.registryObject_component = new RegistryObject_component(component_name);
  }

  RegistryObject_component getObject_component() {
    return this.registryObject_component;
  }

  /**
   * get the filePath to write; only for whole_object component
   *
   * @return Path the Path of the data object.
   */
  public Path writeLink() {
    if (!this.whole_object) {
      throw (new IllegalArgumentException(
          "you shouldn't try to write directly to a Data Product with named components."));
    }
    this.been_used = true;
    return this.dp.getFilePath();
  }

  /**
   * get the CleanableFileChannel to write directly to the file. only for whole_object component.
   *
   * @return CleanableFileChannel the filechannel to write to.
   * @throws IOException
   */
  public CleanableFileChannel writeFileChannel() throws IOException {
    if (!this.whole_object) {
      throw (new IllegalArgumentException(
          "you shouldn't try to write directly to a Data Product with named components."));
    }
    this.been_used = true;
    return this.getFileChannel();
  }

  /**
   * write a Number as an Estimate, as this named component in the data product.
   *
   * @param estimateNumber
   */
  public void writeEstimate(Number estimateNumber) {
    var estimate =
        ImmutableEstimate.builder().internalValue(estimateNumber).rng(this.dp.coderun.rng).build();

    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      dp.coderun.stdApi.parameterDataWriter.write(fileChannel, this.component_name, estimate);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file" + e.toString()));
    }
  }

  /**
   * write a Distribution, as this named component in the data product.
   *
   * @param distribution
   */
  public void writeDistribution(Distribution distribution) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.coderun.stdApi.parameterDataWriter.write(
          fileChannel, this.component_name, distribution);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for write " + e.toString()));
    }
  }

  /*public void writeSamples(String dataProduct, String component, Samples samples) {
    try (CleanableFileChannel fileChannel =
        fileApi.fileApi.openForWrite(dataProduct, component, "toml")) {
      parameterDataWriter.write(fileChannel, component, samples);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for write " + e.toString()));
    }
  }*/

  /**
   * write Samples, as this named component in the data product.
   *
   * @param samples a Samples object containing the samples
   */
  public void writeSamples(Samples samples) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.coderun.stdApi.parameterDataWriter.write(fileChannel, this.component_name, samples);
    } catch (IOException e) {
      throw (new IllegalArgumentException(e));
    }
  }

  protected void register_me_in_code_run() {
    if (this.been_used) this.dp.coderun.addOutput(this.registryObject_component.getUrl());
  }

  protected void register_me_in_registry() {
    if (!been_used) return; // don't register a component unless it has been written to
    if (this.whole_object) {
      Map<String, String> find_whole_object =
          new HashMap<>() {
            {
              put("object", dp.fdpObject.get_id().toString());
              put("whole_object", "true");
            }
          };
      RegistryObject_component objComponent =
          (RegistryObject_component)
              dp.coderun.restClient.getFirst(RegistryObject_component.class, find_whole_object);
      if (objComponent == null) {
        throw (new IllegalArgumentException(
            "can't find the 'whole_object' component for obj " + dp.fdpObject.get_id().toString()));
      }
      this.registryObject_component = objComponent;
      // we store the found 'whole obj' component as the object_component of
      // the referenced Object_component_write so that this can later be stored as a
      // code_run output.
    } else {
      // component != whole_object
      this.registryObject_component.setObject(dp.fdpObject.getUrl());
      RegistryObject_component objComponent =
          (RegistryObject_component) dp.coderun.restClient.post(this.registryObject_component);
      if (objComponent == null) {
        throw (new IllegalArgumentException(
            "failed to create in registry: object component "
                + this.component_name
                + " ("
                + dp.fdpObject.get_id()
                + ")"));
      }
      this.registryObject_component = objComponent;
      // store the created object component so that this can later be stored as a code_run
      // output
    }
  }

  /*
  public void writeTable(
          String dataProduct, String component, Table<Integer, String, Number> table) {
      throw new UnsupportedOperationException();
  }

  public void writeArray(String dataProduct, String component, Number[] arr) {
      Path filepath = fileApi.fileApi.getFilePathForWrite(dataProduct, component, "h5");
      // HDF5 hdf5 = new HDF5(filepath);
      // hdf5.write(component, arr);
  }*/

}
