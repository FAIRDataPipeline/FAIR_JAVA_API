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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This represents an object_component to write to (or raise issues with) An object_component
 * without a name is the 'whole_object' component. Ideally the user should only write to named
 * components on toml and h5 files, and only write to the 'whole_object' on any other files. This is
 * not enforced at the moment. You should only ever either write to the whole_object, OR to the
 * named components, not both. This also is not enforced at the moment.
 */
public class Object_component_write extends Object_component {
  private static final Logger logger = LoggerFactory.getLogger(Object_component_write.class);

  Object_component_write(Data_product dp, String component_name) {
    super(dp, component_name);
  }

  Object_component_write(Data_product dp) {
    super(dp, "whole_object", true);
  }

  protected void populate_component() {
    this.registryObject_component = new RegistryObject_component(component_name);
  }

  /**
   * get the filePath to write; only for whole_object component
   *
   * @return Path the Path of the data object.
   */
  public Path writeLink() {
    if (!this.whole_object) {
      String msg = "You shouldn't try to write directly to a Data Product with named components.";
      logger.error(msg);
      throw (new IllegalActionException(msg));
    }
    this.been_used = true;
    return this.dp.getFilePath();
  }

  /**
   * get the CleanableFileChannel to write directly to the file. only for whole_object component.
   *
   * @return CleanableFileChannel the filechannel to write to.
   * @throws IOException if the file can't be opened.
   */
  public CleanableFileChannel writeFileChannel() throws IOException {
    if (!this.whole_object) {
      String msg = "You shouldn't try to write directly to a Data Product with named components.";
      logger.error(msg);
      throw (new IllegalActionException(msg));
    }
    this.been_used = true;
    return this.getFileChannel();
  }

  /**
   * write a Number as an Estimate, as this named component in the data product.
   *
   * @param estimateNumber the number to write.
   */
  public void writeEstimate(Number estimateNumber) {
    var estimate =
        ImmutableEstimate.builder().internalValue(estimateNumber).rng(this.dp.coderun.rng).build();

    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      dp.coderun.stdApi.parameterDataWriter.write(fileChannel, this.component_name, estimate);
    } catch (IOException e) {
      String msg = "writeEstimate() -- IOException trying to write to file.";
      logger.error(msg + "\n" + e);
      throw (new RuntimeException(msg, e));
    }
  }

  /**
   * write a Distribution, as this named component in the data product.
   *
   * @param distribution the Distribution to write
   */
  public void writeDistribution(Distribution distribution) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.coderun.stdApi.parameterDataWriter.write(
          fileChannel, this.component_name, distribution);
    } catch (IOException e) {
      String msg = "writeDistribution() -- IOException trying to write to file.";
      logger.error(msg + "\n" + e);
      throw (new RuntimeException(msg, e));
    }
  }

  /**
   * write Samples, as this named component in the data product.
   *
   * @param samples a Samples object containing the samples
   */
  public void writeSamples(Samples samples) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.coderun.stdApi.parameterDataWriter.write(fileChannel, this.component_name, samples);
    } catch (IOException e) {
      String msg = "writeSamples() -- IOException trying to write to file.";
      logger.error(msg + "\n" + e);
      throw (new RuntimeException(msg, e));
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
              put("object", dp.registryObject.get_id().toString());
              put("whole_object", "true");
            }
          };
      RegistryObject_component objComponent =
          (RegistryObject_component)
              dp.coderun.restClient.getFirst(RegistryObject_component.class, find_whole_object);
      if (objComponent == null) {
        String msg =
            "Can't find the 'whole_object' component for obj " + dp.registryObject.get_id();
        logger.error(msg);
        throw (new RegistryObjectNotfoundException(msg));
      }
      this.registryObject_component = objComponent;
      // we store the found 'whole obj' component as the object_component of
      // the referenced Object_component_write so that this can later be stored as a
      // code_run output.
    } else {
      // component != whole_object
      this.registryObject_component.setObject(dp.registryObject.getUrl());
      RegistryObject_component objComponent =
          (RegistryObject_component) dp.coderun.restClient.post(this.registryObject_component);
      if (objComponent == null) {
        String msg =
            "Failed to create in registry: object component "
                + this.component_name
                + " ("
                + dp.registryObject.get_id()
                + ")";
        logger.error(msg);
        throw (new RegistryException(msg));
      }
      this.registryObject_component = objComponent;
      // store the created object component so that this can later be stored as a code_run
      // output
    }
  }
}
