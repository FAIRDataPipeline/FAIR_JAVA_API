package uk.ramp.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;

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
public class StandardApiIntegrationTest {
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
  private Samples samples;

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
    ori_configPath = Paths.get(getClass().getResource("/config.yaml").toURI());
    ori_scriptPath = Paths.get(getClass().getResource("/script.sh").toURI());
    // System.out.println(configPath);
    // rng = mock(RandomGenerator.class);
    // when(rng.nextDouble()).thenReturn(0D);
    rng = new RandomDataGenerator().getRandomGenerator();
    samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();

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
    System.out.println("\n\ntestWriteEstimate\n\n");
    try (StandardApi stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/population";
      String component = "estimate-component";
      stdApi.writeEstimate(dataProduct, component, estimate);
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
    System.out.println("\n\ntestReadEstimate\n\n");
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/population";
      String component = "estimate-component";
      assertThat(stdApi.readEstimate(dataProduct, component)).isEqualTo(estimate);
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
    System.out.println("\n\ntestWriteDistribution\n\n");
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/distribution";
      String component = "distribution-component";
      stdApi.writeDistribution(dataProduct, component, distribution);
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
    System.out.println("\n\ntestReadDistribution\n\n");
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/distribution";
      String component = "distribution-component";
      assertThat(stdApi.readDistribution(dataProduct, component)).isEqualTo(distribution);
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
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/cdistribution";
      String component = "cdistribution-component";
      stdApi.writeDistribution(dataProduct, component, categoricalDistribution);
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
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/cdistribution";
      String component = "cdistribution-component";

      assertThat(stdApi.readDistribution(dataProduct, component))
          .isEqualTo(categoricalDistribution);
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
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/samples";
      String component = "example-samples-w";
      stdApi.writeSamples(dataProduct, component, samples);
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
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/samples";
      String component = "example-samples-w";
      assertThat(stdApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
      // assertThat(stdApi.readSamples(dataProduct, component)).isEqualTo(samples);
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
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/multicomp";
      String component1 = "example-samples-w1";
      String component2 = "example-samples-w2";
      stdApi.writeSamples(dataProduct, component1, samples);
      stdApi.writeSamples(dataProduct, component2, samples);
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
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/multicomp";
      String component1 = "example-samples-w1";
      String component2 = "example-samples-w2";

      // assertThat(stdApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
      assertThat(stdApi.readSamples(dataProduct, component1)).isEqualTo(samples);
      assertThat(stdApi.readSamples(dataProduct, component2)).isEqualTo(samples);
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
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "grid1km/10year/females";

    assertThat(stdApi.readArray(dataProduct, component)).isEqualTo(array);
  }

  @Test
  @Disabled // Not implemented yet
  public void testWriteArray() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "example-array-w";
    stdApi.writeArray(dataProduct, component, array);

    // assertEqualFileContents("actualArray.h5", "expectedArray.h5");
  }

  @Test
  @Disabled // Not implemented yet
  public void testReadTable() {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "example-table";

    assertThat(stdApi.readTable(dataProduct, component)).isEqualTo(mockTable);
  }

  @Test
  @Disabled // Not implemented yet
  public void testWriteTable() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "example-table-w";

    stdApi.writeTable(dataProduct, component, mockTable);

    // assertEqualFileContents("actualTable.h5", "expectedTable.h5");
  }

  /*private void assertEqualFileContents(String file1, String file2) throws IOException {
    assertThat(Files.readString(Path.of(dataDirectoryPath, file1)))
        .isEqualTo(Files.readString(Path.of(dataDirectoryPath, file2)));
  }*/
}
