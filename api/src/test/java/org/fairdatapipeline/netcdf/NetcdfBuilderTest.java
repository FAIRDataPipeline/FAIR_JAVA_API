package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFileFormat;
import ucar.nc2.write.NetcdfFormatWriter;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NetcdfBuilderTest {
  final Runnable onClose = this::myClose;

  private void myClose() {
    // do nothing
  }

  /**
   * testing creation of a single group in netcdf using NetcdfBuilder.getGroup()
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_create_group_with_dim() throws IOException, URISyntaxException {
    String filename = "test_create_group_with_dim";
    String resourceName = "/netcdf/test_create_group_with_dim.nc";
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      Group.Builder g = b.getGroup(null, "/aap/noot/mies");
      g.addDimension(new Dimension("bla", 3));
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   * testing creation of a group and its subgroup (each containing a dimension), using
   * NetcdfBuilder.getGroup()
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_create_2groups_with_dims() throws IOException, URISyntaxException {
    String filename = "test_create_2groups_with_dims";
    String resourceName = "/netcdf/test_create_2groups_with_dims.nc";
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      Group.Builder g1 = b.getGroup(null, "/aap/noot/mies");
      Group.Builder g2 = b.getGroup(null, "/aap/noot/mies/pis");
      g1.addDimension(new Dimension("miesdim", 3));
      g2.addDimension(new Dimension("pisdim", 2));
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /** can't use Array.makeFromJavaArray() on a non-primitives array, such as String[] */
  @Test
  void makeFromJavaNonPrimitive() {
    Object o = new String[] {"bram"};
    Assertions.assertThrows(
        NullPointerException.class,
        () -> {
          ucar.ma2.Array.makeFromJavaArray(o);
        });
  }


  /** test that Array.makeFromJavaArray works on an int[][] */
  @Test
  void test_make_from_javaarray() {
    ucar.ma2.Array a = ucar.ma2.Array.makeFromJavaArray(new int[][] {{1, 2, 3}, {11, 12, 13}});
    Assertions.assertEquals(13, a.getInt(Index.factory(a.getShape()).set0(1).set1(2)));
  }
}
