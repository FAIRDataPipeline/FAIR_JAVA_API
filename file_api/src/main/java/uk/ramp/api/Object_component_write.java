package uk.ramp.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import uk.ramp.dataregistry.content.RegistryObject_component;
import uk.ramp.distribution.Distribution;
import uk.ramp.estimate.ImmutableEstimate;
import uk.ramp.file.CleanableFileChannel;
import uk.ramp.samples.Samples;

public class Object_component_write extends Object_component_RW {

  public Object_component_write(Data_product_RW dp, String component_name) {
    super(dp, component_name);
  }

  public Object_component_write(Data_product_RW dp) {
    super(dp, "whole_object", true);
  }

  public void populate_component() {
    this.registryObject_component = new RegistryObject_component(component_name);
  }

  RegistryObject_component getObject_component() {
    return this.registryObject_component;
  }

  public void writeEstimate(Number estimateNumber) {
    var estimate =
        ImmutableEstimate.builder().internalValue(estimateNumber).rng(this.dp.fileApi.rng).build();

    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      dp.fileApi.stdApi.parameterDataWriter.write(fileChannel, this.component_name, estimate);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file" + e.toString()));
    }
  }

  public void writeDistribution(Distribution distribution) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.fileApi.stdApi.parameterDataWriter.write(
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

  public void writeSamples(Samples samples) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.fileApi.stdApi.parameterDataWriter.write(fileChannel, this.component_name, samples);
    } catch (IOException e) {
      throw (new IllegalArgumentException(e));
    }
  }

  protected void register_me_in_code_run_session(Code_run_session crs) {
    crs.addOutput(this.registryObject_component.getUrl());
  }

  protected void register_me_in_registry() {
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
              dp.fileApi.restClient.getFirst(RegistryObject_component.class, find_whole_object);
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
          (RegistryObject_component) dp.fileApi.restClient.post(this.registryObject_component);
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
