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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.Distribution.DistributionType;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.distribution.ImmutableMinMax;
import uk.ramp.distribution.MinMax;
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
  private Path configPath;
  private Path scriptPath;
  private RandomGenerator rng;

  @Before
  public void setUp() throws Exception {
    Path ori_configPath = Paths.get(getClass().getResource("/config.yaml").toURI());
    Path ori_scriptPath = Paths.get(getClass().getResource("/script.sh").toURI());
    Path datastore = Path.of("D:\\Datastore");
    String coderun_ts = "20210808T123456";
    Path coderun = datastore.resolve("coderun");
    FileUtils.deleteDirectory(coderun.toFile());
    coderun = coderun.resolve(coderun_ts);

    FileUtils.deleteDirectory(
        datastore
            .resolve("standardAPItest")
            .toFile()); // remove the whole namespace in the local datastore.

    Files.createDirectories(coderun);
    configPath = coderun.resolve("config.yaml");
    scriptPath = coderun.resolve("script.sh");
    Files.copy(ori_configPath, configPath);
    Files.copy(ori_scriptPath, scriptPath);

    System.out.println(configPath);
    // rng = mock(RandomGenerator.class);
    // when(rng.nextDouble()).thenReturn(0D);
    // samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    rng = new RandomDataGenerator().getRandomGenerator();

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
  public void testWriteEstimate() throws IOException {
    try (StandardApi stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/population";
      String component = "Estimate Component";
      stdApi.writeEstimate(dataProduct, component, estimate);
      // assertThat("bla").isEqualTo("bla");
      // assertEqualFileContents("actualEstimate.toml", "expectedEstimate.toml");
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
    }
  }

  @Test
  @Order(1)
  public void testReadEstimate() {
    try (var stdApi = new StandardApi(configPath, scriptPath, rng)) {
      String dataProduct = "human/population";
      String component = "Estimate Component";
      assertThat(stdApi.readEstimate(dataProduct, component)).isEqualTo(estimate);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
    }
  }

  @Ignore
  @Test
  public void testReadDistribution() {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "parameter";
    String component = "example-distribution";

    assertThat(stdApi.readDistribution(dataProduct, component)).isEqualTo(distribution);
  }

  @Ignore
  @Test
  public void testReadCategoricalDistribution() {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "parameter";
    String component = "example-distribution-categorical";

    assertThat(stdApi.readDistribution(dataProduct, component)).isEqualTo(categoricalDistribution);
  }

  @Ignore
  @Test
  public void testWriteDistribution() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "Initial Data_product";
    String component = "Distribution component";
    stdApi.writeDistribution(dataProduct, component, distribution);

    // assertEqualFileContents("actualDistribution.toml", "expectedDistribution.toml");
  }

  @Ignore
  @Test
  public void testWriteCategoricalDistribution() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "Initial Data_product";
    String component = "CDistribution component";
    stdApi.writeDistribution(dataProduct, component, categoricalDistribution);

    /*assertEqualFileContents(
    "actualDistributionCategorical.toml", "expectedDistributionCategorical.toml");*/
  }

  @Ignore
  @Test
  public void testReadSample() {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "parameter";
    String component = "example-samples";
    assertThat(stdApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
  }

  @Ignore
  @Test
  public void testWriteSamples() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "Initial Data_product";
    String component = "example-samples-w";
    stdApi.writeSamples(dataProduct, component, samples);

    // assertEqualFileContents("actualSamples.toml", "expectedSamples.toml");
  }

  @Ignore
  @Test
  public void testWriteSamplesMultipleComponents() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "parameter";
    String component1 = "example-samples-w1";
    String component2 = "example-samples-w2";
    stdApi.writeSamples(dataProduct, component1, samples);
    stdApi.writeSamples(dataProduct, component2, samples);

    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Ignore // Not implemented yet
  public void testReadArray() {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "grid1km/10year/females";

    assertThat(stdApi.readArray(dataProduct, component)).isEqualTo(array);
  }

  @Test
  @Ignore // Not implemented yet
  public void testWriteArray() throws IOException {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "example-array-w";
    stdApi.writeArray(dataProduct, component, array);

    // assertEqualFileContents("actualArray.h5", "expectedArray.h5");
  }

  @Test
  @Ignore // Not implemented yet
  public void testReadTable() {
    var stdApi = new StandardApi(configPath, scriptPath, rng);
    String dataProduct = "object";
    String component = "example-table";

    assertThat(stdApi.readTable(dataProduct, component)).isEqualTo(mockTable);
  }

  @Test
  @Ignore // Not implemented yet
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
