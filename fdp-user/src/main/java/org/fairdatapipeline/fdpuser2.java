package org.fairdatapipeline;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.api.Data_product_read;
import org.fairdatapipeline.api.FileApi;
import org.fairdatapipeline.api.Issue;
import org.fairdatapipeline.api.Object_component_read;

public class fdpuser2 {

  public static void main(String[] args) {
    {
      String dataProduct = "human/population";
      String component = "estimate-component";
      RandomGenerator rng;

      FileApi fileApi =
              new FileApi(
                      Path.of("d:\\Datastore\\coderun\\20210808T123456\\config.yaml"),
                      Path.of("d:\\Datastore\\coderun\\20210808T123456\\script.sh"));
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent(component);
      System.out.println("Estimate: " + oc.readEstimate());
      Issue i = fileApi.raise_issue("bla", 1);
    }
    System.out.println("after the code");
    System.gc();
    try {
      TimeUnit.SECONDS.sleep(10);
    }catch(InterruptedException e) {
      System.out.println(e);
    }
    System.out.println("after the sleep");
  }
}
