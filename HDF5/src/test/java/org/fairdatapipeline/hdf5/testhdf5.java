package org.fairdatapipeline.hdf5;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class testhdf5 {

  @Test
  public void testhdf() throws URISyntaxException {
    Path p =
        Paths.get(getClass().getResource("/2c8777f89e18b8c73e234961da4670aab4c7f4c8.h5").toURI());
    hdf5 h = new hdf5(p);
    ArrayList<Object> ao = h.read_arraylist("/movement-multipliers/table");
    Assertions.assertEquals(String[].class, ao.get(0).getClass());
    Assertions.assertEquals(double[].class, ao.get(1).getClass());
    ao.forEach(o -> print_array(o));
  }

  void print_array(Object obj) {
    System.out.println("***");
    if (obj.getClass() == String[].class) {
      String[] sa = (String[]) obj;
      Stream.of(sa).forEach(str -> System.out.println(str));
    } else if (obj.getClass() == double[].class) {
      double[] da = (double[]) obj;
      int i = da.length;
      for (int j = 0; j < i; j++) {
        System.out.println(da[j]);
      }
    } else {
      System.out.println("unimplemented: " + obj.getClass().getSimpleName());
    }
  }
}
