package org.fairdatapipeline.netcdf;

import org.apache.commons.io.FileUtils;
import org.fairdatapipeline.hash.Hasher;
import org.fairdatapipeline.hash.Sha1Hasher;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayDefinition;
import org.fairdatapipeline.objects.NumericalArrayImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.apache.commons.codec.digest.DigestUtils.sha1Hex;
import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFileFormat;
import ucar.nc2.write.NetcdfFormatWriter;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NetcdfBuilderTest {
    final Runnable onClose = this::myClose;
    final Sha1Hasher shahasher = new Sha1Hasher();
    final Hasher hasher = new Hasher();


    private void myClose() {
        // do nothing
    }


    /**
     * testing creation of a single group in netcdf using NetcdfBuilder.getGroup()
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test_create_group_with_dim() throws IOException, URISyntaxException {
        String filename = "test_create_group_with_dim";
        String resourceName = "/netcdf/test_create_group_with_dim.nc";
        Path filePath = Files.createTempFile(filename, ".nc");
        try(NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), this.onClose)) {
            Group.Builder g = b.getGroup(null, "/aap/noot/mies");
            g.addDimension(new Dimension("bla", 3));
        }
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }

    /**
     * testing creation of a group and its subgroup (each containing a dimension), using NetcdfBuilder.getGroup()
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test_create_2groups_with_dims() throws IOException, URISyntaxException {
        String filename = "test_create_2groups_with_dims";
        String resourceName = "/netcdf/test_create_2groups_with_dims.nc";
        Path filePath = Files.createTempFile(filename, ".nc");
        try(NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), this.onClose)) {
            Group.Builder g1 = b.getGroup(null, "/aap/noot/mies");
            Group.Builder g2 = b.getGroup(null, "/aap/noot/mies/pis");
            g1.addDimension(new Dimension("miesdim", 3));
            g2.addDimension(new Dimension("pisdim", 2));
        }
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }

    /**
     * can't use Array.makeFromJavaArray() on a non-primitives array, such as String[]
     */
    @Test
    void makeFromJavaNonPrimitive() {
        Object o = new String[] {"bram"};
        Assertions.assertThrows(NullPointerException.class, () -> {ucar.ma2.Array.makeFromJavaArray(o);});
    }

    /**
     * test that we can store a String[] in a STRING variable.
     * @throws IOException
     * @throws URISyntaxException
     * @throws InvalidRangeException
     */
    @Test
    void test_write_string() throws IOException, URISyntaxException, InvalidRangeException {
        String filename = "test_write_string";
        String resourceName = "/netcdf/test_write_string.nc";
        String varname = "blavar";
        Path filePath = Files.createTempFile(filename, ".nc");
        Nc4Chunking chunker =
                Nc4ChunkingStrategy.factory(Nc4Chunking.Strategy.none, 0, false);
        NetcdfFormatWriter.Builder builder = NetcdfFormatWriter.createNewNetcdf4(NetcdfFileFormat.NETCDF4, filePath.toString(), chunker);
        Variable.Builder vb = Variable.builder().setName(varname).setDataType(DataType.STRING).addAttribute(new Attribute("test", "testing a string var"));
        builder.getRootGroup().addVariable(vb);
        NetcdfFormatWriter writer = builder.build();
        Variable v = writer.findVariable(varname);
        if(v == null) throw(new UnsupportedOperationException("variable " + varname + " not found"));
        Object o = new String[] {"bram"};
        ucar.ma2.Array values = NetcdfDataType.translate_array(o);
        writer.write(v, values);
        writer.close();
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }

    /**
     * test a simple INT array stored using NetcdfBuilder.prepareArray and .writeDimensionVariables and .writeArrayData
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test_build_prepare_write_INT() throws IOException, URISyntaxException {
        String filename = "test_build_prepare_write_INT";
        String resourceName = "/netcdf/test_build_prepare_write_INT.nc";
        NumericalArrayDefinition nadef = new NumericalArrayDefinition("temperature", NetcdfDataType.INT, "a test dataset with temperatures in 2d space", new String[] {"X", "Y"}, new int[] {2, 3}, new int[][] {{2, 4}, {3, 6, 9}}, new String[] {"cm", "cm"}, "C");
        String group = "/aap/noot/mies";
        NumericalArray nadat = new NumericalArrayImpl(new int[][] {{1,2,3}, {11, 12, 13}});
        Path filePath = Files.createTempFile(filename, ".nc");
        try(NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), this.onClose)) {
            b.prepareArray(group, nadef);
            try(NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
                w.writeDimensionVariables(group, nadef);
                w.writeArrayData(group, nadef, nadat);
            }
        }
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }

    /**
     * testing the prepare/write sequence with 2 arrays.
     */
    @Test
    void test_build_write_two_arrays() throws IOException, URISyntaxException{
        String filename = "test_build_write_two_arrays";
        String resourceName = "/netcdf/test_build_write_two_arrays.nc";
        NumericalArrayDefinition temperature = new NumericalArrayDefinition("temperature", NetcdfDataType.INT,
                "a test dataset with int temperatures in 2d space, measure in a 2cm grid",
                new String[] {"X", "Y"},
                new int[] {2, 3},
                new int[][] {{2, 4}, {3, 6, 9}},
                new String[] {"cm", "cm"},
                "C");
        Object[] height_dim_values = new Object[2];
        height_dim_values[0] = new String[]{"Bram Boskamp", "Rosalie Boskamp"};
        height_dim_values[1] = new int[]{1640995200, 1643673600, 1646092800}; // i haven't bothered to figure out if I could use actual Dates in my data.. these are seconds since 1970.

        NumericalArrayDefinition heights = new NumericalArrayDefinition("personheight", NetcdfDataType.DOUBLE,
                "a test dataset with real height in 2d space, with measurements for each person on a number of dates",
                new String[] {"person", "date"},
                new int[] {2, 3},
                height_dim_values,
                new String[] {"name", "seconds since 1970"}, "m");
        String group1 = "/my/group/temps";
        String group2 = "/my/othergroup/heights";
        NumericalArray temp_data = new NumericalArrayImpl(new int[][] {{1,2,3}, {11, 12, 13}});
        NumericalArray height_data = new NumericalArrayImpl(new double[][] {{1.832, 1.828, 1.823}, {1.229, 1.232, 1.239}});
        Path filePath = Files.createTempFile(filename, ".nc");
        try(NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), this.onClose)) {
            b.prepareArray(group1, temperature);
            b.prepareArray(group2, heights);
            try(NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
                w.writeDimensionVariables(group1, temperature);
                w.writeDimensionVariables(group2, heights);
                w.writeArrayData(group1, temperature, temp_data);
                w.writeArrayData(group2, heights, height_data);
            }
        }
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }


    /**
     * test using NetcdfWritehandle to write a 3d array in timeslices: an XY 2d set for 1 write each t
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test_build_write_in_parts1() throws IOException, URISyntaxException{
        String filename = "test_build_write_in_parts1";
        String resourceName = "/netcdf/test_build_write_in_parts.nc";

        NumericalArrayDefinition nadef = new NumericalArrayDefinition("temperature",
                NetcdfDataType.INT,
                "a test dataset with temperatures in time", new String[] {"time", "X", "Y"},
                new int[] {5, 2, 3}, new int[][] {{1640995200, 1640995201, 1640995202, 1640995203, 1640995204}, {2, 4}, {3, 6, 9}},
                new String[] {"seconds since 01-01-1970", "cm", "cm"},
                "C");
        String group = "/three/d/intime";

        Path filePath = Files.createTempFile(filename, ".nc");
        try(NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), this.onClose)) {
            b.prepareArray(group, nadef);
            try(NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
                w.writeDimensionVariables(group, nadef);
                NetcdfWriteHandle h = w.get_write_handle(group, nadef);
                int[][] xyMeasurements = new int[2][3];
                int i = 0;
                for(int time=0;time<5;time++) {
                    for(int x=0;x<2;x++) for(int y=0;y<3;y++) xyMeasurements[x][y] = i++;
                    h.write_data(new NumericalArrayImpl(xyMeasurements));
                }
            }
        }
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }


    /**
     * test using NetcdfWritehandle to write a TXY 3d array in 1d vectors: write a Y 1d vector set for 1 write each TX
     * @throws IOException
     * @throws URISyntaxException
     */
    @Test
    void test_build_write_in_parts2() throws IOException, URISyntaxException{
        String filename = "test_build_write_in_parts2";
        String resourceName = "/netcdf/test_build_write_in_parts.nc";

        NumericalArrayDefinition nadef = new NumericalArrayDefinition("temperature",
                NetcdfDataType.INT,
                "a test dataset with temperatures in time", new String[] {"time", "X", "Y"},
                new int[] {5, 2, 3}, new int[][] {{1640995200, 1640995201, 1640995202, 1640995203, 1640995204}, {2, 4}, {3, 6, 9}},
                new String[] {"seconds since 01-01-1970", "cm", "cm"},
                "C");
        String group = "/three/d/intime";

        Path filePath = Files.createTempFile(filename, ".nc");
        try(NetcdfBuilder b = new NetcdfBuilder(filePath.toString(), this.onClose)) {
            b.prepareArray(group, nadef);
            try(NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
                w.writeDimensionVariables(group, nadef);
                NetcdfWriteHandle h = w.get_write_handle(group, nadef);
                int[] yMeasurements = new int[3];
                int i = 0;
                for(int time=0;time<5;time++) for(int x=0;x<2;x++) {
                    for(int y=0;y<3;y++) yMeasurements[y] = i++;
                    h.write_data(new NumericalArrayImpl(yMeasurements));
                }
            }
        }
        Assertions.assertTrue(FileUtils.contentEquals(filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
        FileUtils.delete(filePath.toFile());
    }



    @Test
    void test_make_from_javaarray() {
        ucar.ma2.Array a = ucar.ma2.Array.makeFromJavaArray(new int[][] {{1, 2, 3}, {11, 12, 13}});
        Assertions.assertEquals(13, a.getInt(Index.factory(a.getShape()).set0(1).set1(2)));
    }

    @Test
    void test_object_array() {
        Object[] o = new Object[5];
        o[0] = new Integer[] {1, 2, 3, 4};
        o[1] = new String[] {"aap", "noot", "mies"};
        o[2] = new Double[] {1.1, 2.2, 3.3, 4.4};
        o[3] = new int[] {1, 2, 3, 4};
        o[4] = new double[] {1.1, 2.2, 3.3, 4.4};

        for(int i=0; i<5;i++) {
            System.out.println(i + ": " + o[i].getClass().getSimpleName() + " - type: " +  o[i].getClass().getTypeName());
        }
        System.out.println("0 is Integer[]? - " + (o[0].getClass() == Array.newInstance(Integer.class, 0).getClass()));
        System.out.println("1 is String[]? - " + (o[1].getClass() == Array.newInstance(String.class, 0).getClass()));
        System.out.println("2 is Double[]? - " + (o[2].getClass() == Array.newInstance(Double.class, 0).getClass()));
        System.out.println("3 is int[]? - " + (o[3].getClass() == Array.newInstance(int.class, 0).getClass()));

    }
}
