package org.fairdatapipeline.api;

import java.io.IOException;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.estimate.ImmutableEstimate;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.samples.Samples;

public class Object_component_write_filechannel_toml extends Object_component_write_filechannel {
  Object_component_write_filechannel_toml(
      Data_product_write_filechannel dp, String component_name) {
    super(dp, component_name);
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
      dp.coderun.parameterDataWriter.write(fileChannel, this.component_name, estimate);
    } catch (IOException e) {
      throw (new RuntimeException("writeEstimate() -- IOException trying to write to file.", e));
    }
  }
  /**
   * write a Distribution, as this named component in the data product.
   *
   * @param distribution the Distribution to write
   */
  public void writeDistribution(Distribution distribution) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.coderun.parameterDataWriter.write(fileChannel, this.component_name, distribution);
    } catch (IOException e) {
      throw (new RuntimeException(
          "writeDistribution() -- IOException trying to write to file.", e));
    }
  }

  /**
   * write Samples, as this named component in the data product.
   *
   * @param samples a Samples object containing the samples
   */
  public void writeSamples(Samples samples) {
    try (CleanableFileChannel fileChannel = this.getFileChannel()) {
      this.dp.coderun.parameterDataWriter.write(fileChannel, this.component_name, samples);
    } catch (IOException e) {
      throw (new RuntimeException("writeSamples() -- IOException trying to write to file.", e));
    }
  }
}
