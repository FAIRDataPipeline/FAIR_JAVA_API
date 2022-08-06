package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NetcdfWriterTest {
  final Runnable onClose = this::myClose;

  private void myClose() {
    // do nothing
  }

  /**
   * test a simple INT array stored using NetcdfBuilder.prepareArray and .writeDimensionVariables
   * and .writeArrayData
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_build_prepare_write_INT() throws IOException, URISyntaxException {
    String filename = "test_build_prepare_write_INT";
    String resourceName = "/netcdf/test_build_prepare_write_INT.nc";
    String group = "aap/noot/mies";
    VariableName xName = new VariableName("X", group);
    VariableName yName = new VariableName("Y", group);
    VariableName tempName = new VariableName("temperature", group);

    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(
            xName,
            new int[] {2, 4},
            "the x-axis is measured in along the length of my football pitch; (0,0) is the southwest corner.",
            "m",
            "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(
            yName,
            new int[] {3, 6, 9},
            "the y-axis is measured in along the width of my football pitch; (0,0) is the southwest corner.",
            "m",
            "");
    DimensionalVariableDefinition nadef =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {xName, yName},
            "a test dataset with temperatures in 2d space",
            "C",
            "surface temperature");
    NumericalArray nadat = new NumericalArrayImpl(new int[][] {{1, 2, 3}, {11, 12, 13}});
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepareDimension(xdim);
      b.prepareDimension(ydim);
      b.prepareArray(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        w.writeArrayData(w.getVariable(nadef.getVariableName()), nadat);
      } catch (InvalidRangeException e) {
        //
      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /** testing the prepare/write sequence with 2 arrays. */
  @Test
  void test_build_write_two_arrays() throws IOException, URISyntaxException {
    String filename = "test_build_write_two_arrays";
    String resourceName = "/netcdf/test_build_write_two_arrays.nc";
    String group1 = "my/group/temps";
    String group2 = "my/othergroup/heights";
    VariableName xName = new VariableName("X", group1);
    VariableName yName = new VariableName("Y", group1);
    VariableName tempName = new VariableName("temp", group1);

    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(
            xName,
            new int[] {2, 4},
            "the x-axis runs east-west with 0 = south-east corner of my garden",
            "cm",
            "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(
            yName,
            new int[] {3, 6, 9},
            "the y-axis runs south-north with 0 = south-east corner of my garden",
            "cm",
            "");
    DimensionalVariableDefinition temperature =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {xName, yName},
            "a test dataset with int temperatures in 2d space, measure in a 2cm grid",
            "C",
            "surface temperature");

    VariableName personName = new VariableName("person", group2);
    VariableName dateName = new VariableName("date", group2);
    VariableName personheightName = new VariableName("personheight", group2);
    CoordinateVariableDefinition persondim =
        new CoordinateVariableDefinition(
            personName,
            new String[] {"Bram Boskamp", "Rosalie Boskamp"},
            "the person's name is good enough an identifier for me",
            "",
            "named person");
    CoordinateVariableDefinition datedim =
        new CoordinateVariableDefinition(
            dateName,
            new int[] {1640995200, 1643673600, 1646092800},
            "the date the measurement was taken",
            "seconds since 01-01-1970 00:00:00",
            "");

    DimensionalVariableDefinition heights =
        new DimensionalVariableDefinition(
            personheightName,
            NetcdfDataType.DOUBLE,
            new VariableName[] {personName, dateName},
            "a test dataset with real height in 2d space, with measurements for each person on a number of dates",
            "m",
            "");

    NumericalArray temp_data = new NumericalArrayImpl(new int[][] {{1, 2, 3}, {11, 12, 13}});
    NumericalArray height_data =
        new NumericalArrayImpl(new double[][] {{1.832, 1.828, 1.823}, {1.229, 1.232, 1.239}});
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepareDimension(xdim);
      b.prepareDimension(ydim);
      b.prepareArray(temperature);
      b.prepareDimension(persondim);
      b.prepareDimension(datedim);
      b.prepareArray(heights);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        w.writeDimensionVariable(persondim);
        w.writeDimensionVariable(datedim);
        w.writeArrayData(w.getVariable(tempName), temp_data);
        w.writeArrayData(w.getVariable(personheightName), height_data);
      } catch (InvalidRangeException e) {
        //
      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   * test using NetcdfWritehandle to write a 3d array in timeslices: an XY 2d set for 1 write each t
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_build_write_in_parts1() throws IOException, URISyntaxException {
    String filename = "test_build_write_in_parts1";
    String resourceName = "/netcdf/test_build_write_in_parts.nc";

    String group = "three/d/intime";

    VariableName timeName = new VariableName("time", group);
    VariableName xName = new VariableName("X", group);
    VariableName yName = new VariableName("Y", group);

    CoordinateVariableDefinition timedim =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {1640995200, 1640995201, 1640995202, 1640995203, 1640995204},
            "",
            "seconds since 01-01-1970",
            "");
    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(xName, new int[] {2, 4}, "my x axis", "cm", "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(yName, new int[] {3, 6, 9}, "my y axis", "cm", "");

    VariableName temperatureName = new VariableName("temperature", group);

    DimensionalVariableDefinition nadef =
        new DimensionalVariableDefinition(
            temperatureName,
            NetcdfDataType.INT,
            new VariableName[] {timeName, xName, yName},
            "a test dataset with temperatures in time and space",
            "C",
            "");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepareDimension(timedim);
      b.prepareDimension(xdim);
      b.prepareDimension(ydim);
      b.prepareArray(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(timedim);
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        Variable v = w.getVariable(temperatureName);
        int[][] xyMeasurements = new int[2][3];
        int i = 0;
        int[] origin = new int[] {0, 0, 0};
        for (int time = 0; time < 5; time++) {
          for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) xyMeasurements[x][y] = i++;
          Array data = ucar.ma2.Array.makeFromJavaArray(xyMeasurements);
          origin[0] = time;
          w.writeArrayData(v, Array.makeArrayRankPlusOne(data), origin);
        }
      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   * test using NetcdfWritehandle to write a TXY 3d array in 1d vectors: write a Y 1d vector set for
   * 1 write each TX
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_build_write_in_parts2() throws IOException, URISyntaxException {
    String filename = "test_build_write_in_parts2";
    String resourceName = "/netcdf/test_build_write_in_parts.nc";
    String group = "three/d/intime";
    VariableName timeName = new VariableName("time", group);
    VariableName xName = new VariableName("X", group);
    VariableName yName = new VariableName("Y", group);

    VariableName tempName = new VariableName("temperature", group);

    CoordinateVariableDefinition timedim =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {1640995200, 1640995201, 1640995202, 1640995203, 1640995204},
            "",
            "seconds since 01-01-1970",
            "");
    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(xName, new int[] {2, 4}, "my x axis", "cm", "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(yName, new int[] {3, 6, 9}, "my y axis", "cm", "");
    DimensionalVariableDefinition nadef =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {timeName, xName, yName},
            "a test dataset with temperatures in time and space",
            "C",
            "");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepareDimension(timedim);
      b.prepareDimension(xdim);
      b.prepareDimension(ydim);
      b.prepareArray(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(timedim);
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        Variable v = w.getVariable(nadef.getVariableName());
        int[] origin = new int[] {0, 0, 0};
        int[] yMeasurements = new int[3];
        int i = 0;
        for (int time = 0; time < 5; time++)
          for (int x = 0; x < 2; x++) {
            origin[0] = time;
            origin[1] = x;
            for (int y = 0; y < 3; y++) yMeasurements[y] = i++;
            Array data = ucar.ma2.Array.makeFromJavaArray(yMeasurements);
            w.writeArrayData(
                v, Array.makeArrayRankPlusOne(Array.makeArrayRankPlusOne(data)), origin);
          }
      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void sharedDimension() throws IOException, URISyntaxException {
    String filename = "sharedDimension";
    String resourceName = "/netcdf/sharedDimension.nc";

    String group_time = "time";
    String group_temp = group_time + "/temp";

    VariableName timeName = new VariableName("time", group_time);
    VariableName xName = new VariableName("X", group_temp);
    VariableName yName = new VariableName("Y", group_temp);
    VariableName tempName = new VariableName("temp", group_temp);

    CoordinateVariableDefinition time =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {12, 13, 14, 15, 16},
            "this is the time dimension that other arrays should link to",
            "seconds (since 01-01-1970)",
            "");

    CoordinateVariableDefinition dim_x =
        new CoordinateVariableDefinition(xName, new int[] {2, 4}, "my x axis", "cm", "");
    CoordinateVariableDefinition dim_y =
        new CoordinateVariableDefinition(yName, new int[] {3, 6, 9}, "my y axis", "cm", "");

    DimensionalVariableDefinition nadef_temp =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {timeName, xName, yName},
            "a test dataset with temperatures in time and space",
            "C",
            "surface temperature");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepareDimension(time);
      b.prepareDimension(dim_x);
      b.prepareDimension(dim_y);
      b.prepareArray(nadef_temp);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(time);
        w.writeDimensionVariable(dim_x);
        w.writeDimensionVariable(dim_y);
        Variable v = w.getVariable(nadef_temp.getVariableName());
        int[][] xyMeasurements = new int[2][3];
        int[] origin = new int[] {0, 0, 0};
        int i = 0;
        for (int t = 0; t < 5; t++) {
          origin[0] = t;
          for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) xyMeasurements[x][y] = i++;
          Array data = ucar.ma2.Array.makeFromJavaArray(xyMeasurements);
          w.writeArrayData(v, Array.makeArrayRankPlusOne(data), origin);
        }

      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void inRoot() throws IOException, URISyntaxException {
    String filename = "inRoot";
    String resourceName = "/netcdf/inRoot.nc";

    String root_group = "";

    VariableName timeName = new VariableName("time", root_group);
    VariableName tempName = new VariableName("temp", root_group);

    CoordinateVariableDefinition time =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {12, 13, 14, 15, 16},
            "this is the time dimension that other arrays should link to",
            "seconds (since 01-01-1970)",
            "");

    DimensionalVariableDefinition nadef_temp =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {timeName},
            "a test dataset with temperatures in time",
            "C",
            "");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepareDimension(time);
      b.prepareArray(nadef_temp);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(time);
        Variable v = w.getVariable(nadef_temp.getVariableName());
        int[] temp = new int[] {10, 14, 12, 19, 11};
        w.writeArrayData(v, new NumericalArrayImpl(temp));

      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }
}
