package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.LocalVariableDefinition;
import org.fairdatapipeline.objects.TableDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ucar.ma2.Index;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;

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

  @Test
  void test_create_2table_groups() throws IOException, URISyntaxException {
    String filename = "test_create_table_group";
    String resourceName = "/netcdf/test_create_table_group.nc";
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      Group.Builder g1 = b.getGroup(null, "/my/little/table", true);
      g1.addAttribute(new Attribute("_group_type", "table"));
      Group.Builder g2 = b.getGroup(null, "/my/little/othertable", true);
      g2.addAttribute(new Attribute("_group_type", "table"));
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   *   show that it is not possible to create a table within another table.
   */
  @Test
  void test_create_table_in_table() throws IOException, URISyntaxException {
    String filename = "test_create_table_in_table";
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      Group.Builder g1 = b.getGroup(null, "/my/little/table", true);
      g1.addAttribute(new Attribute("_group_type", "table"));
      Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> {b.getGroup(null, "/my/little/table/subtable", true);}
      );

    }
  }


  /**
   *   show that it is not possible to 'get' a group within another table.
   */
  @Test
  void test_create_something_in_table() throws IOException, URISyntaxException {
    String filename = "test_create_something_in_table";
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      Group.Builder g1 = b.getGroup(null, "/my/little/table", true);
      g1.addAttribute(new Attribute("_group_type", "table"));
      Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> {b.getGroup(null, "/my/little/table/subgroup");}
      );

    }
  }

  /**
   *   this shows that the 'mustBeFresh' argument prevents 'get'ting a group that already exists.
   *   this is in order to make sure a table always creates its own unique group to exist in.
   */
  @Test
  void test_create_table_in_existing_group() throws IOException, URISyntaxException {
    String filename = "test_create_table_in_existing_group";
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.getGroup(null, "/my/little/group");
      Assertions.assertThrows(
              IllegalArgumentException.class,
              () -> {b.getGroup(null, "/my/little/group", true);}
      );

    }
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

  @Test
  void test_prepare_coordinatevar() throws IOException, URISyntaxException {
    String filename = "test_prepare_coordinatevar";
    String extension = ".nc";
    String resourceName = "/netcdf/" + filename + extension;
    Path filePath = Files.createTempFile(filename, extension);
    CoordinateVariableDefinition cvdef = new CoordinateVariableDefinition(
      new VariableName("coordinatevariable", ""),
            new int[] {1, 2, 3},
            "my first coordinate",
            "cm",
            "very long name of my coordinate variable"
    );
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {

      b.prepare(cvdef);
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   * using a dimensionalvar with zero length dimensions; results in a scalar variable.
   * not sure if i had meant this to be so, and if it should be so.
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_prepare_scalar_dimensionalvar() throws IOException, URISyntaxException {
    String filename = "test_prepare_scalar_dimensionalvar";
    String extension = ".nc";
    String resourceName = "/netcdf/" + filename + extension;
    Path filePath = Files.createTempFile(filename, extension);
    DimensionalVariableDefinition dimdef = new DimensionalVariableDefinition(
            new VariableName("dimensionalvariable", ""),
            NetcdfDataType.INT,
            new NetcdfName[] {},
            "my first dimensional variable",
            "cm",
            "very long name of my coordinate variable"
    );
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {

      b.prepare(dimdef);
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /** when trying to create a dimensionalvariable the referenced dimensions in the dimensions[] must exist.
   *
   * @throws IOException
   */
  @Test
  void test_prepare_dimensionalvar_missing_dim() throws IOException {
    String filename = "test_prepare_dimensionalvar";
    String extension = ".nc";
    Path filePath = Files.createTempFile(filename, extension);
    DimensionalVariableDefinition dimdef = new DimensionalVariableDefinition(
            new VariableName("dimensionalvariable", ""),
            NetcdfDataType.INT,
            new DimensionName[] {new NetcdfName("dimension1")},
            "my first dimensional variable",
            "cm",
            "very long name of my coordinate variable"
    );

    try (NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      Assertions.assertThrows(IllegalArgumentException.class, () -> {
        b.prepare(dimdef);
      });
    }
  }

  /** creating a coordinatevariable and a 1-d dimensional variable referencing the coordinate variable.
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_prepare_dimensionalvar() throws IOException, URISyntaxException {
    String filename = "test_prepare_dimensionalvar";
    String extension = ".nc";
    String resourceName = "/netcdf/" + filename + extension;
    Path filePath = Files.createTempFile(filename, extension);

    DimensionalVariableDefinition dimdef = new DimensionalVariableDefinition(
            new VariableName("dimensionalvariable", ""),
            NetcdfDataType.INT,
            new DimensionName[] {new NetcdfName("dimension1")},
            "my first dimensional variable",
            "cm",
            "very long name of my coordinate variable"
    );
    CoordinateVariableDefinition cvdef = new CoordinateVariableDefinition(
            new VariableName("dimension1", ""),
            new int[] {1, 2, 3},
            "my first coordinate",
            "cm",
            "very long name of my coordinate variable"
    );
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(cvdef);
      b.prepare(dimdef);
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }


  /**
   *
   */
  @Test
  void test_linked_from_multi_attribute() throws IOException, URISyntaxException  {
    String filename = "test_linked_from_multi_attribute";
    String extension = ".nc";
    String resourceName = "/netcdf/" + filename + extension;
    Path filePath = Files.createTempFile(filename, extension);

    TableDefinition td = new TableDefinition(
            new NetcdfGroupName("table"),
            1,
            "",
            "",
            Collections.singletonMap("linked_from", new String[] {"apples", "pears"}),
            new LocalVariableDefinition[] {
                    new LocalVariableDefinition(
                            new NetcdfName("column1"),
                            NetcdfDataType.INT,
                            "",
                            "",
                            "")
            }
    );
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(td);
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());


  }



}
