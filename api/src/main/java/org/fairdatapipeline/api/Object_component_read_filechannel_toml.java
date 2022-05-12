package org.fairdatapipeline.api;

import java.io.IOException;
import java.util.List;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.parameters.ReadComponent;

public class Object_component_read_filechannel_toml extends Object_component_read_filechannel {
  Object_component_read_filechannel_toml(Data_product dp, String component_name) {
    super(dp, component_name);
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
          dp.coderun.parameterDataReader.read(fileChannel, this.registryObject_component.getName());
    } catch (IOException e) {
      throw (new RuntimeException("readEstimate() -- IOException trying to read from file", e));
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
      data = this.dp.coderun.parameterDataReader.read(fileChannel, this.component_name);
    } catch (IOException e) {
      throw (new RuntimeException(
          "readDistribution() -- IOException trying to read from file.", e));
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
      data = this.dp.coderun.parameterDataReader.read(fileChannel, this.component_name);
    } catch (IOException e) {
      throw (new RuntimeException("readSamples() -- IOException trying to read from file.", e));
    }
    return data.getSamples();
  }
}
