package uk.ramp;

import java.nio.file.Path;
import java.util.Random;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import uk.ramp.api.*;
import uk.ramp.samples.ImmutableSamples;

public class fdpuser {
  static String dataProduct = "human/population";
  static String component = "estimate-component";
  static RandomGenerator rng;

  public static void main(String[] args) {
    try (FileApi fileApi =
        new FileApi(
            Path.of("d:\\Datastore\\coderun\\20210808T123456\\config.yaml"),
            Path.of("d:\\Datastore\\coderun\\20210808T123456\\script.sh"))) {
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent(component);
      System.out.println("Estimate: " + oc.readEstimate());
      Issue i = fileApi.raise_issue("bla", 1);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try (FileApi fileApi =
        new FileApi(
            Path.of("d:\\Datastore\\coderun\\20210808T123456\\config.yaml"),
            Path.of("d:\\Datastore\\coderun\\20210808T123456\\script.sh"))) {
      rng = new RandomDataGenerator().getRandomGenerator();
      ImmutableSamples samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
      String dataproduct_name =
          "animal/"
              + new Random()
                  .ints(97, 123)
                  .limit(8)
                  .collect(
                      StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                  .toString();
      Data_product_write dp = fileApi.get_dp_for_write(dataproduct_name, "toml");
      Object_component_write c = dp.getComponent("numberOfLegs");
      c.writeSamples(samples);
    }
  }
}
