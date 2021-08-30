package org.fairdatapipeline.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.distribution.Distribution.DistributionType;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.distribution.ImmutableMinMax;
import org.fairdatapipeline.distribution.MinMax;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.samples.ImmutableSamples;
import org.fairdatapipeline.samples.Samples;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CoderunIntegrationTest2 {
  private final Table<Integer, String, Number> mockTable =
      ImmutableTable.<Integer, String, Number>builder()
          .put(0, "colA", 5)
          .put(1, "colA", 6)
          .put(2, "colA", 7)
          .put(0, "colB", 0)
          .put(1, "colB", 1)
          .put(2, "colB", 2)
          .build();

  private List<String[]> csv_data;
  private String chickenTestText =
      "This is a text file about chickens.\nPlease forgive the lack of interesting content in this file.";

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
  private RestClient restClient;

  @BeforeAll
  public void setUp() throws Exception {
    restClient = new RestClient("http://localhost:8000/api/");
    ori_configPath = Paths.get(getClass().getResource("/config-stdapi.yaml").toURI());
    ori_scriptPath = Paths.get(getClass().getResource("/script.sh").toURI());
    // System.out.println(configPath);
    // rng = mock(RandomGenerator.class);
    // when(rng.nextDouble()).thenReturn(0D);
    rng = new RandomDataGenerator().getRandomGenerator();
    samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    samples2 = ImmutableSamples.builder().addSamples(4, 5, 6).rng(rng).build();
    samples3 = ImmutableSamples.builder().addSamples(7, 8, 9).rng(rng).build();
    samples4 = ImmutableSamples.builder().addSamples(10, 11, 12).rng(rng).build();

    csv_data = new ArrayList<>();
    csv_data.add(new String[] {"apple", "12", "green"});
    csv_data.add(new String[] {"orange", "5", "orange"});
    csv_data.add(new String[] {"banana", "32", "yellow"});

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
    try (Coderun coderun = new Coderun(configPath, scriptPath)) {
      Data_product_write dp = coderun.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeEstimate(estimate);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    // to assert things are correct, we check the storageRoot:
    // we just make sure that 3 storagelocations were created: config, script, and output
    /*RegistryStorage_root sr =
        (RegistryStorage_root)
            restClient.getFirst(
                RegistryStorage_root.class, Collections.singletonMap("root", "D:\\datastore"));
    assertThat(sr.getLocations().size() == 3);
    if (sr.getLocations().size() == 3) {
      RegistryStorage_location sl =
          (RegistryStorage_location)
              restClient.get(RegistryStorage_location.class, sr.getLocations().get(2));
      assertThat(sl.getHash()).isEqualTo("a4f9d47dac45639e69a758a8b2d49bf11bbeb262");
      assertThat(sl.getPath())
          .isEqualTo(
              "standardAPItest\\human\\population\\0.0.1.toml"); // TODO: this is windows specific?
    }
    Registry_ObjectList<?> l =
        restClient.getList(RegistryObject.class, new HashMap<String, String>());
    assertThat(l.getCount()).isEqualTo(3);
    if (l.getCount() == 3) {
      RegistryObject o = (RegistryObject) l.getResults().get(0);
      assertThat(o.getFile_type()).isEqualTo("http://localhost:8000/api/file_type/3/");
      assertThat(o.getDescription()).isEqualTo("StandardApi Integration test");
    }*/
    System.out.println("\n\nend of testWriteEstimate\n\n");
  }

  @Test
  @Order(2)
  public void testReadEstimate() {
    String dataProduct = "human/population";
    String component = "estimate-component";
    System.out.println("\n\ntestReadEstimate\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent(component);
      assertThat(oc.readEstimate()).isEqualTo(estimate);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    /*RegistryStorage_root sr =
            (RegistryStorage_root)
                restClient.getFirst(
                    RegistryStorage_root.class, Collections.singletonMap("root", "D:\\datastore"));
        assertThat(sr.getLocations().size() == 3);
        Registry_ObjectList<?> l =
            restClient.getList(RegistryObject.class, new HashMap<String, String>());
        assertThat(l.getCount())
            .isEqualTo(5); // we have 2 x script, 2 x config, 1 x data_product = 5 objects
        l = restClient.getList(RegistryCode_run.class, new HashMap<String, String>());
        assertThat(l.getCount()).isEqualTo(2); // 2 code run's 1 write, 1 read
        if (l.getCount() == 2) {
          RegistryCode_run cr = (RegistryCode_run) l.getResults().get(1);
          assertThat(cr.getUrl()).isEqualTo("http://localhost:8000/api/code_run/1/");
          assertThat(cr.getOutputs().size()).isEqualTo(1);
          cr = (RegistryCode_run) l.getResults().get(0);
          assertThat(cr.getUrl()).isEqualTo("http://localhost:8000/api/code_run/2/");
          assertThat(cr.getOutputs().size()).isEqualTo(0);
          assertThat(cr.getInputs().size()).isEqualTo(1);
        }
    */
    System.out.println("\n\nend of testReadEstimate\n\n");
  }

  @Test
  @Order(3)
  public void testWriteDistribution() throws IOException {
    String dataProduct = "human/distribution";
    String component = "distribution-component";
    System.out.println("\n\ntestWriteDistribution\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent(component);
      oc.writeDistribution(distribution);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    // 3rd coderun
    // output 10th objcomponent
    // 8th object
    // 4th stolo (1 script, 1 config, 2 data)

    System.out.println("\n\nend of testWriteDistribution\n\n");
  }

  @Test
  @Order(4)
  public void testReadDistribution() {
    String dataProduct = "human/distribution";
    String component = "distribution-component";
    System.out.println("\n\ntestReadDistribution\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dc.getComponent(component);
      assertThat(oc.readDistribution()).isEqualTo(distribution);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    // 4th coderun
    // input 10th objcomp
    // we now have 10 objects

    System.out.println("\n\nend of testReadDistribution\n\n");
  }

  @Test
  @Order(5)
  public void testWriteCategoricalDistribution() throws IOException {
    System.out.println("\n\ntestWriteCategorialDistribution\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // 5th coderun
    // output obj component 16
    // which is part of object 13
    // we now have 13 objects
    // and 5 stolo's

    System.out.println("\n\nend of testCategorialDistribution\n\n");
  }

  @Test
  @Order(6)
  public void testReadCategoricalDistribution() {
    System.out.println("\n\ntestReadCategorialDistribution\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // we now have 6 code runs
    // this one inputs objcomp 16
    // we now have 15 objects

    System.out.println("\n\nend of testCategorialDistribution\n\n");
  }

  @Test
  @Order(7)
  public void testWriteSamples() throws IOException {
    System.out.println("\n\ntestWriteSamples\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // 7th code run
    // outputs 22nd obj comp
    // which belongs to obj 18
    // which uses stolo 6

    System.out.println("\n\nend of testWriteSamples\n\n");
    // assertEqualFileContents("actualSamples.toml", "expectedSamples.toml");
  }

  @Test
  @Order(8)
  public void testReadSamples() {
    System.out.println("\n\ntestReadSamples\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // code run 8
    // inputs obj comp 22
    // part of obj 18
    // stolo 6
    // we have 20 objects

    System.out.println("\n\nend of testReadSamples\n\n");
  }

  @Test
  @Order(9)
  public void testWriteSamplesMultipleComponents() throws IOException {
    System.out.println("\n\ntestWriteSamplesMultipleComponents\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // code run 9
    // outputs objcomp 28 and 29
    // both belong to obj 23 at stolo 7

    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(10)
  public void testReadSamplesMultipleComponents() {
    System.out.println("\n\ntestReadSamplesMultipleComponents\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // code run 10
    // inputs objcomp 28 and 29 (obj 23)
    // there are 25 objects
    System.out.println("\n\nend of testReadSamplesMultipleComponents\n\n");
  }

  @Test
  @Order(11)
  public void testWriteGlobDP() {
    System.out.println("\n\ntestWriteGlobDP\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // code run 11
    // outputs obj comp 35, obj 28, stolo 8

    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(12)
  public void testReadNoGlob() {
    System.out.println("\n\ntestReadNoGlob\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/dog";
      String component = "example-samples";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dc.getComponent(component);
      assertThat(oc.readSamples()).containsExactly(4, 5, 6);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // code run 12
    // inputs 35, obj 28 stolo 8

    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(13)
  public void testWriteGlobMultiDP() {
    System.out.println("\n\ntestWriteGlobDP\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
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
    // code run 13
    // outputs obj comp 41 (obj 33, stolo 9) 43 (obj 34, stolo 10)
    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(14)
  public void testWriteSamplesMultipleComponentsAndIssues() throws IOException {
    System.out.println("\n\ntestWriteSamplesMultipleComponentsAndIssues\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/dodo";
      String component1 = "example-samples-dodo1";
      String component2 = "example-samples-dodo2";
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
    // code run 14
    // outputs 47 (with issues 1, 2) and 48 (issue 3), obj 37, stolo 11

    System.out.println("end of testWriteSamplesMultipleComponents");
    // assertEqualFileContents("actualSamplesMultiple.toml", "expectedSamplesMultiple.toml");
  }

  @Test
  @Order(15)
  public void testReadSamplesMultipleComponentsAndIssues() {
    System.out.println("\n\ntestReadSamplesMultipleComponents\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/dodo";
      String component1 = "example-samples-dodo1";
      String component2 = "example-samples-dodo2";
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
    // code run 15
    // inputs 47 (now has issues 1, 2, 4, 5) and 48 (still only has issue 3)
    System.out.println("\n\nend of testReadSamplesMultipleComponents\n\n");
  }

  @Test
  @Order(16)
  public void testRead_oneIssueToMultipleComp() {
    System.out.println("\n\ntestRead_oneIssueToMultipleComp\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/dodo";
      String component1 = "example-samples-dodo1";
      String component2 = "example-samples-dodo2";
      Data_product_read dc = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc1 = dc.getComponent(component1);
      Issue i = fileApi.raise_issue("one issue for 2 comps", 2);
      i.add_components(oc1);
      Object_component_read oc2 = dc.getComponent(component2);
      i.add_components(oc2);
      assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
      assertThat(oc2.readSamples()).containsExactly(1, 2, 3);
      Issue i2 = fileApi.raise_issue("this issue attached itself to the comps", 3);
      i2.add_components(oc1, oc2);

      // assertThat(fileApi.readSamples(dataProduct, component)).containsExactly(1, 2, 3);
      // assertThat(fileApi.readSamples(dataProduct, component1)).isEqualTo(samples);
      // assertThat(fileApi.readSamples(dataProduct, component2)).isEqualTo(samples);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
      e.printStackTrace();
    }
    // code run 16
    // inputs 47 (now has issues 1, 2, 4, 5) and 48 (still only has issue 3)
    System.out.println("\n\nend of testReadSamplesMultipleComponents\n\n");
  }

  @Test
  @Order(17)
  public void testCSV_writeLink() throws IOException {
    System.out.println("\n\ntestCSV\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/ant";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "csv");
      Object_component_write oc = dp.getComponent();
      Path p = oc.writeLink();
      assertNotNull(p);
      if (p == null) return;
      try (PrintWriter pw = new PrintWriter(p.toFile())) {
        csv_data.stream()
            .map(s -> Stream.of(s).collect(Collectors.joining(",")))
            .forEach(pw::println);
      }
    }
  }

  private List<String> getRecordFromLine(String line) {
    List<String> values = new ArrayList<String>();
    try (Scanner rowScanner = new Scanner(line)) {
      rowScanner.useDelimiter(",");
      while (rowScanner.hasNext()) {
        values.add(rowScanner.next());
      }
    }
    return values;
  }

  @Test
  @Order(18)
  public void testCSV_readLink() throws IOException {
    System.out.println("\n\ntestCSV\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/ant";
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent();
      Path p = oc.readLink();
      Integer i = 0;
      try (Scanner scanner = new Scanner(p.toFile()); ) {
        while (scanner.hasNextLine()) {
          assertThat(getRecordFromLine(scanner.nextLine())).isEqualTo(List.of(csv_data.get(i)));
          i = i + 1;
        }
      }
    }
  }

  @Test
  @Order(19)
  public void testCSV_writeLink_withIssue() throws IOException {
    System.out.println("\n\ntestCSV\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/monkey";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "csv");
      Object_component_write oc = dp.getComponent();
      Path p = oc.writeLink();
      oc.raise_issue("this does not seem to contain anything monkey-related", 9);
      assertNotNull(p);
      if (p == null) return;
      try (PrintWriter pw = new PrintWriter(p.toFile())) {
        csv_data.stream()
            .map(s -> Stream.of(s).collect(Collectors.joining(",")))
            .forEach(pw::println);
      }
    }
  }

  @Test
  @Order(20)
  public void testCSV_readLink_withIssue() throws IOException {
    System.out.println("\n\ntestCSV\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/ant";
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent();
      oc.raise_issue("not enough orange", 10);
      Path p = oc.readLink();
      Integer i = 0;
      try (Scanner scanner = new Scanner(p.toFile()); ) {
        while (scanner.hasNextLine()) {
          assertThat(getRecordFromLine(scanner.nextLine())).isEqualTo(List.of(csv_data.get(i)));
          i = i + 1;
        }
      }
    }
  }

  @Test
  @Order(21)
  public void testRewriteDPname() {
    System.out.println("\n\ntestCSV\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/canine";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent("NumberOfLegs");
      oc.writeSamples(samples);
    }
  }

  @Test
  @Order(22)
  public void testReadRewrittenDPname() {
    System.out.println("\n\ntestReadRewrittenDPname\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/canine";
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent("NumberOfLegs");
      oc.readSamples();
    }
  }

  @Test
  @Order(23)
  public void testAltNS() {
    System.out.println("\n\ntestAltNS\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "test/altns";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct, "toml");
      Object_component_write oc = dp.getComponent("altNScompo");
      oc.writeSamples(samples);
    }
  }

  @Test
  @Order(24)
  public void testAltNSread() {
    System.out.println("\n\ntestAltNSread\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "test/altns";
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      Object_component_read oc = dp.getComponent("altNScompo");
      oc.readSamples();
    }
  }

  @Test
  @Order(25)
  public void testConfigFiletype() {
    System.out.println("\n\ntestConfigFiletype WRITE\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/chicken";
      Data_product_write dp = fileApi.get_dp_for_write(dataProduct);
      Object_component_write oc = dp.getComponent();
      try (CleanableFileChannel f = oc.getFileChannel()) {
        ByteBuffer bb = ByteBuffer.wrap(chickenTestText.getBytes(StandardCharsets.UTF_8));
        f.write(bb);
      } catch (IOException e) {
        System.out.println("failed to open FileChannel");
      }
    }

    System.out.println("\n\ntestConfigFiletype READ\n\n");

    try (var fileApi = new Coderun(configPath, scriptPath)) {
      String dataProduct = "animal/chicken";
      Data_product_read dp = fileApi.get_dp_for_read(dataProduct);
      assertThat(dp.extension).isEqualTo("txt");
      Object_component_read oc = dp.getComponent();
      try (CleanableFileChannel f = oc.getFileChannel()) {
        ByteBuffer bb = ByteBuffer.allocate(chickenTestText.length());
        f.read(bb);
        String r = new String(bb.array());
        assertThat(r).isEqualTo(chickenTestText);
      } catch (IOException e) {
        System.out.println("failed to open FileChannel");
      }
    }
  }

  @Test
  @Order(26)
  public void emptyCoderun() {
    System.out.println("\n\nemptyCoderun\n\n");
    try (var fileApi = new Coderun(configPath, scriptPath)) {
      // do nothing
    }
  }

  @Test
  @Disabled // Not implemented yet
  public void testReadArray() {
    var fileApi = new Coderun(configPath, scriptPath);
    String dataProduct = "object";
    String component = "grid1km/10year/females";

    // assertThat(fileApi.readArray(dataProduct, component)).isEqualTo(array);
  }

  @Test
  @Disabled // Not implemented yet
  public void testWriteArray() throws IOException {
    var fileApi = new Coderun(configPath, scriptPath);
    String dataProduct = "object";
    String component = "example-array-w";
    // fileApi.writeArray(dataProduct, component, array);

    // assertEqualFileContents("actualArray.h5", "expectedArray.h5");
  }

  @Test
  @Disabled // Not implemented yet
  public void testReadTable() {
    var fileApi = new Coderun(configPath, scriptPath);
    String dataProduct = "object";
    String component = "example-table";

    // assertThat(fileApi.readTable(dataProduct, component)).isEqualTo(mockTable);
  }

  @Test
  @Disabled // Not implemented yet
  public void testWriteTable() throws IOException {
    var fileApi = new Coderun(configPath, scriptPath);
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
