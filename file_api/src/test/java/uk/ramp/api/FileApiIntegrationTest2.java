package uk.ramp.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.*;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.Distribution.DistributionType;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.ImmutableMinMax;
import uk.ramp.distribution.MinMax;
import uk.ramp.samples.ImmutableSamples;
import uk.ramp.samples.Samples;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FileApiIntegrationTest2 {
  private final Table<Integer, String, Number> mockTable =
      ImmutableTable.<Integer, String, Number>builder()
          .put(0, "colA", 5)
          .put(1, "colA", 6)
          .put(2, "colA", 7)
          .put(0, "colB", 0)
          .put(1, "colB", 1)
          .put(2, "colB", 2)
          .build();

  private final Number[] array = new Number[] {5, 6, 3.4};
  private Samples samples, samples2, samples3, samples4;
  private Distribution distribution;
  private Distribution categoricalDistribution;
  private Number estimate = 1.0;
  private Path datastorePath = Path.of("D:\\Datastore");
  private String coderun_ts = "20210808T123456";
  private Path coderunPath = datastorePath.resolve("coderun");
  private Path coderunTSpath = coderunPath.resolve(coderun_ts);
  private String ns = "standardAPItest";
  private Path nsPath = datastorePath.resolve(ns);
  private Path ori_configPath;
  private Path ori_scriptPath;
  private RandomGenerator rng;
  private Path configPath = coderunTSpath.resolve("config.yaml");
  private Path scriptPath = coderunTSpath.resolve("script.sh");

  @BeforeAll
  public void setUp() throws Exception {
    ori_configPath = Paths.get(getClass().getResource("/config-stdapi.yaml").toURI());
    ori_scriptPath = Paths.get(getClass().getResource("/script.sh").toURI());
    // System.out.println(configPath);
    // rng = mock(RandomGenerator.class);
    // when(rng.nextDouble()).thenReturn(0D);
    rng = new RandomDataGenerator().getRandomGenerator();
    samples  = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    samples2 = ImmutableSamples.builder().addSamples(4, 5, 6).rng(rng).build();
    samples3 = ImmutableSamples.builder().addSamples(7, 8, 9).rng(rng).build();
    samples4 = ImmutableSamples.builder().addSamples(10, 11, 12).rng(rng).build();

    distribution =
        ImmutableDistribution.builder()
            .internalShape(1)
            .internalScale(2)
            .internalType(DistributionType.gamma)
            .rng(rng)
            .build();

    MinMax firstMinMax =
        ImmutableMinMax.builder()
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .lowerBoundary(0)
            .upperBoundary(4)
            .build();

    MinMax secondMinMax =
        ImmutableMinMax.builder()
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .lowerBoundary(5)
            .upperBoundary(9)
            .build();

    MinMax thirdMinMax =
        ImmutableMinMax.builder()
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .lowerBoundary(10)
            .upperBoundary(14)
            .build();

    MinMax fourthMinMax =
        ImmutableMinMax.builder()
            .isLowerInclusive(true)
            .isUpperInclusive(true)
            .lowerBoundary(15)
            .upperBoundary(20)
            .build();

    categoricalDistribution =
        ImmutableDistribution.builder()
            .internalType(DistributionType.categorical)
            .bins(List.of(firstMinMax, secondMinMax, thirdMinMax, fourthMinMax))
            .weights(List.of(0.4, 0.1, 0.1, 0.4))
            .rng(rng)
            .build();
  }

  @Test
  @Order(0)
  public void cleanup_datastore() throws IOException {
    System.out.println(
        "test 0 - cleanup_datastore: removing all outputs from datastore and copying the config & script files to coderun area");
    FileUtils.deleteDirectory(coderunTSpath.toFile());
    FileUtils.deleteDirectory(
        nsPath.toFile()); // remove the whole namespace in the local datastore.
    Files.createDirectories(coderunTSpath);
    Files.copy(ori_configPath, configPath);
    Files.copy(ori_scriptPath, scriptPath);
  }

  @Test
  @Order(1)
  public void testWriteEstimate() throws IOException {
    String dataProduct = "human/population";
    String component = "estimate-component";
    System.out.println("\n\ntestWriteEstimate\n\n");
    try (FileApi fileApi = new FileApi(configPath, scriptPath)) {
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeEstimate(estimate);
      // assertThat("bla").isEqualTo("bla");
      // assertEqualFileContents("actualEstimate.toml", "expectedEstimate.toml");
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testWriteEstimate\n\n");
  }

  @Test
  @Order(2)
  public void testReadEstimate() {
    String dataProduct = "human/population";
    String component = "estimate-component";
    System.out.println("\n\ntestReadEstimate\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent(component);
      assertThat(oc.readEstimate()).isEqualTo(estimate);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testReadEstimate\n\n");
  }

  @Test
  @Order(3)
  public void testWriteDistribution() throws IOException {
    String dataProduct = "human/distribution";
    String component = "distribution-component";
    System.out.println("\n\ntestWriteDistribution\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeDistribution(distribution);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testWriteDistribution\n\n");
  }

  @Test
  @Order(4)
  public void testReadDistribution() {
    String dataProduct = "human/distribution";
    String component = "distribution-component";
    System.out.println("\n\ntestReadDistribution\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dc.getComponent(component);
      assertThat(oc.readDistribution()).isEqualTo(distribution);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testReadDistribution\n\n");
  }

  @Test
  @Order(5)
  public void testWriteCategoricalDistribution() throws IOException {
    System.out.println("\n\ntestWriteCategorialDistribution\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/cdistribution";
      String component = "cdistribution-component";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeDistribution(categoricalDistribution);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testCategorialDistribution\n\n");
  }

  @Test
  @Order(6)
  public void testReadCategoricalDistribution() {
    System.out.println("\n\ntestReadCategorialDistribution\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/cdistribution";
      String component = "cdistribution-component";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dc.getComponent(component);
      assertThat(oc.readDistribution()).isEqualTo(categoricalDistribution);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testCategorialDistribution\n\n");
  }

  @Test
  @Order(7)
  public void testWriteSamples() throws IOException {
    System.out.println("\n\ntestWriteSamples\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/samples";
      String component = "example-samples-w";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeSamples(samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testWriteSamples\n\n");
    // assertEqualFileContents("actualSamples.toml", "expectedSamples.toml");
  }

  @Test
  @Order(8)
  public void testReadSamples() {
    System.out.println("\n\ntestReadSamples\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/samples";
      String component = "example-samples-w";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dc.getComponent(component);
      assertThat(oc.readSamples()).containsExactly(1, 2, 3);
      // assertThat(fileApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
      // assertThat(fileApi.readSamples(dataProduct, component)).isEqualTo(samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testReadSamples\n\n");
  }

  @Test
  @Order(9)
  public void testWriteSamplesMultipleComponents() throws IOException {
    System.out.println("\n\ntestWriteSamplesMultipleComponents\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/multicomp";
      String component1 = "example-samples-w1";
      String component2 = "example-samples-w2";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");

      Object_component_write oc1 = dp.getComponent(component1);
      oc1.writeSamples(samples);

      Object_component_write oc2 = dp.getComponent(component2);
      oc2.writeSamples(samples);

      // fileApi.writeSamples(dataProduct, component1, samples);
      // fileApi.writeSamples(dataProduct, component2, samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(10)
  public void testReadSamplesMultipleComponents() {
    System.out.println("\n\ntestReadSamplesMultipleComponents\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/multicomp";
      String component1 = "example-samples-w1";
      String component2 = "example-samples-w2";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc1 = dc.getComponent(component1);
      Object_component_read oc2 = dc.getComponent(component2);
      assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
      assertThat(oc2.readSamples()).containsExactly(1, 2, 3);

      // assertThat(fileApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
      // assertThat(fileApi.readSamples(dataProduct, component1)).isEqualTo(samples);
      // assertThat(fileApi.readSamples(dataProduct, component2)).isEqualTo(samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testReadSamplesMultipleComponents\n\n");
  }

  @Test
  @Order(11)
  public void testWriteGlobDP() {
    System.out.println("\n\ntestWriteGlobDP\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "animal/dog";
      String component = "example-samples";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeSamples(samples2);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(12)
  public void testReadNoGlob() {
    System.out.println("\n\ntestReadNoGlob\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "animal/dog";
      String component = "example-samples";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dc.getComponent(component);
      assertThat(oc.readSamples()).containsExactly(4, 5, 6);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(13)
  public void testWriteGlobMultiDP() {
    System.out.println("\n\ntestWriteGlobDP\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct1 = "animal/horse";
      String dataProduct2 = "animal/mouse";
      String component = "example-samples";
      Data_product_write dp1 = fileApi.get_dp_for_write(dataProduct1, "toml");
      Data_product_write dp2 = fileApi.get_dp_for_write(dataProduct2, "toml");
      Object_component_write oc = dp1.getComponent(component);
      oc.writeSamples(samples3);
      oc = dp2.getComponent(component);
      oc.writeSamples(samples4);

    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(14)
  public void testWriteSamplesMultipleComponentsAndIssues() throws IOException {
    System.out.println("\n\ntestWriteSamplesMultipleComponentsAndIssues\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/multicomp";
      String component1 = "example-samples-w1";
      String component2 = "example-samples-w2";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");

      Object_component_write oc1 = dp.getComponent(component1);
      oc1.raise_issue("something is terribly wrong with this component", 10);
      oc1.writeSamples(samples);
      oc1.raise_issue("very bad component", 1);

      Object_component_write oc2 = dp.getComponent(component2);
      oc2.writeSamples(samples);
      oc2.raise_issue("this one is not so bad", 1);

      // fileApi.writeSamples(dataProduct, component1, samples);
      // fileApi.writeSamples(dataProduct, component2, samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(15)
  public void testReadSamplesMultipleComponentsAndIssues() {
    System.out.println("\n\ntestReadSamplesMultipleComponents\n\n");
    try (var fileApi = new FileApi(configPath, scriptPath)) {
      String dataProduct = "human/multicomp";
      String component1 = "example-samples-w1";
      String component2 = "example-samples-w2";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc1 = dc.getComponent(component1);
      oc1.raise_issue("upon re-reading this component we found even more problems", 10);
      Object_component_read oc2 = dc.getComponent(component2);
      oc1.raise_issue("upon re-reading this component we found it's actually OK", 0);
      assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
      assertThat(oc2.readSamples()).containsExactly(1, 2, 3);

      // assertThat(fileApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
      // assertThat(fileApi.readSamples(dataProduct, component1)).isEqualTo(samples);
      // assertThat(fileApi.readSamples(dataProduct, component2)).isEqualTo(samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    System.out.println("\n\nend of testReadSamplesMultipleComponents\n\n");
  }


  @Test
  @Disabled // Not implemented yet
  public void testReadArray() {
    var fileApi = new FileApi(configPath, scriptPath);
    String dataProduct = "object";
    String component = "grid1km/10year/females";

    // assertThat(fileApi.readArray(dataProduct, component)).isEqualTo(array);
  }

  @Test
  @Disabled // Not implemented yet
  public void testWriteArray() throws IOException {
    var fileApi = new FileApi(configPath, scriptPath);
    String dataProduct = "object";
    String component = "example-array-w";
    // fileApi.writeArray(dataProduct, component, array);

    // assertEqualFileContents("actualArray.h5", "expectedArray.h5");
  }

  @Test
  @Disabled // Not implemented yet
  public void testReadTable() {
    var fileApi = new FileApi(configPath, scriptPath);
    String dataProduct = "object";
    String component = "example-table";

    // assertThat(fileApi.readTable(dataProduct, component)).isEqualTo(mockTable);
  }

  @Test
  @Disabled // Not implemented yet
  public void testWriteTable() throws IOException {
    var fileApi = new FileApi(configPath, scriptPath);
    String dataProduct = "object";
    String component = "example-table-w";
    // fileApi.writeTable(dataProduct, component, mockTable);
    // assertEqualFileContents("actualTable.h5", "expectedTable.h5");
  }

  /*private void assertEqualFileContents(String file1, String file2) throws IOException {
    assertThat(Files.readString(Path.of(dataDirectoryPath, file1)))
        .isEqualTo(Files.readString(Path.of(dataDirectoryPath, file2)));
  }*/
}
