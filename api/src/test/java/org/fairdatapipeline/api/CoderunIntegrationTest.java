package org.fairdatapipeline.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.distribution.Distribution.DistributionType;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.distribution.ImmutableMinMax;
import org.fairdatapipeline.distribution.MinMax;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.VariableName;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.NumericalArray;
import org.fairdatapipeline.objects.NumericalArrayImpl;
import org.fairdatapipeline.samples.ImmutableSamples;
import org.fairdatapipeline.samples.Samples;
import org.javatuples.Triplet;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "LOCALREG", matches = "FRESHASADAISY")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoderunIntegrationTest {
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
  private final String chickenTestText =
      "This is a text file about chickens.\nPlease forgive the lack of interesting content in this file.";
  private Samples samples, samples2, samples3, samples4;
  private Distribution distribution;
  private Distribution categoricalDistribution;
  private final Number estimate = 1.0;
  private RandomGenerator rng;
  private RestClient restClient;

  private Path ori_configPath;
  private Path ori_scriptPath;
  private Path datastorePath;
  private String coderun_ts;
  private Path coderunPath;
  private Path coderunTSPath;
  private String ns;
  private String altNamespace;
  private Path nsPath;
  private Path altNamespacePath;
  private Path configPath;
  private Path scriptPath;
  private Path coderuns_txt;
  private String token;
  private String repo_desc = "Analysis / processing script location";
  private String script_desc = "Submission script location in local datastore";
  private String config_desc = "Working config.yaml file location in local datastore";
  private String CSV_hash;

  void setup_paths() throws URISyntaxException {
    ori_configPath =
        Paths.get(Objects.requireNonNull(getClass().getResource("/config-stdapi.yaml")).toURI());
    ori_scriptPath =
        Paths.get(Objects.requireNonNull(getClass().getResource("/script.sh")).toURI());
    datastorePath = ori_configPath.getParent().resolve("datastore");
    coderun_ts = "20210807T123456";
    coderunPath = datastorePath.resolve("coderun");
    coderunTSPath = coderunPath.resolve(coderun_ts);
    ns = "CoderunTest";
    altNamespace = "alternativeNS";
    nsPath = datastorePath.resolve(ns);
    altNamespacePath = datastorePath.resolve(altNamespace);
    configPath = coderunTSPath.resolve("config.yaml");
    scriptPath = coderunTSPath.resolve("script.sh");
    coderuns_txt = coderunTSPath.resolve("coderuns.txt");
  }

  void setup_data() {
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

  @BeforeAll
  public void setUp() throws Exception {
    this.token = System.getenv("REGTOKEN");
    restClient = new RestClient("http://localhost:8000/api/", token);
    create_author();

    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0D);
    // rng = new RandomDataGenerator().getRandomGenerator();
    setup_paths();
    setup_data();
    create_namespace(this.ns);
    create_namespace(this.altNamespace);
    this.CSV_hash = "eb7e7a49816c8a6a784260e4596e88bf7a96a6a5";
    if (System.getProperty("os.name").contains("Windows"))
      this.CSV_hash = "d1713dcc0c6b28337d14f3693882aebca3e96f17";
    cleanup_datastore();
  }

  void create_namespace(String ns) {
    restClient.post(new RegistryNamespace(ns));
  }

  void create_author() {
    RegistryAuthor author =
        (RegistryAuthor) restClient.getFirst(RegistryAuthor.class, Collections.emptyMap());
    if (author == null) {
      author = new RegistryAuthor();
      author.setName("An Anonymous Author");
      author = (RegistryAuthor) restClient.post(author);
    }
    if (restClient.getFirst(RegistryUser_author.class, Collections.emptyMap()) == null) {
      RegistryUser_author ua = new RegistryUser_author();
      ua.setAuthor(author.getUrl());
      RegistryUsers u =
          (RegistryUsers) restClient.getFirst(RegistryUsers.class, Collections.emptyMap());
      ua.setUser(u.getUrl());
      restClient.post(ua);
    }
  }

  @AfterAll
  public void final_cleanup() throws IOException {
    // delete_directories();
  }

  public void cleanup_datastore() throws IOException {
    delete_directories();
    Files.createDirectories(coderunTSPath);
    Files.copy(ori_configPath, configPath);
    Files.copy(ori_scriptPath, scriptPath);
  }

  void delete_directories() throws IOException {
    FileUtils.deleteDirectory(coderunTSPath.toFile());
    FileUtils.deleteDirectory(
        nsPath.toFile()); // remove the whole namespace in the local datastore.
    FileUtils.deleteDirectory(
        altNamespacePath
            .toFile()); // remove the whole alternative namespace in the local datastore.
  }

  void check_namespace(String dataProduct, String namespace) {
    RegistryData_product dp =
        (RegistryData_product)
            restClient.getFirst(
                RegistryData_product.class, Collections.singletonMap("name", dataProduct));
    RegistryNamespace ns =
        (RegistryNamespace) restClient.get(RegistryNamespace.class, dp.getNamespace());
    assertThat(ns.getName()).isEqualTo(namespace);
  }

  void check_issue(String issue, String... components) {
    RegistryIssue ri =
        (RegistryIssue)
            restClient.getFirst(
                RegistryIssue.class, Collections.singletonMap("description", issue));
    ri.getComponent_issues()
        .forEach(
            i -> {
              RegistryObject_component oc =
                  (RegistryObject_component) restClient.get(RegistryObject_component.class, i);
              assertThat(oc).isNotNull();
              if (oc.isWhole_object()) {
                RegistryObject o =
                    (RegistryObject) restClient.get(RegistryObject.class, oc.getObject());
                if (o.getData_products().isEmpty()) {
                  // for script, config, repo, we just check the description
                  assertThat(o.getDescription()).isIn((Object[]) components);
                } else {
                  RegistryData_product dp =
                      (RegistryData_product)
                          restClient.get(RegistryData_product.class, o.getData_products().get(0));
                  assertThat(dp.getName()).isIn((Object[]) components);
                }
              } else {
                assertThat(oc.getName()).isIn((Object[]) components);
              }
            });
  }

  void check_last_coderun(
      List<Triplet<String, String, String>> inputs, List<Triplet<String, String, String>> outputs) {
    // quite a random set of checks to see if a coderun did what I expect it to do.
    List<String> coderuns;
    try {
      coderuns = Files.readAllLines(coderuns_txt);
    } catch (IOException e) {
      Assertions.fail("Failed to read coderuns_txt: {} ", e);
      return;
    }
    String last_coderun = coderuns.get(coderuns.size() - 1);
    RegistryCode_run cr =
        (RegistryCode_run)
            restClient.getFirst(
                RegistryCode_run.class, Collections.singletonMap("uuid", last_coderun));
    RegistryObject script =
        (RegistryObject) restClient.get(RegistryObject.class, cr.getSubmission_script());
    assertNotNull(script);
    RegistryObject config =
        (RegistryObject) restClient.get(RegistryObject.class, cr.getModel_config());
    assertNotNull(config);
    RegistryObject code_repo =
        (RegistryObject) restClient.get(RegistryObject.class, cr.getCode_repo());
    assertNotNull(code_repo);
    assertThat(code_repo.getAuthors())
        .containsExactly(restClient.makeAPIURL(RegistryAuthor.class, 1));

    assertThat(cr.getDescription()).isEqualTo("Coderun Integration test");
    if (inputs == null) assertThat(cr.getInputs()).isEmpty();
    else {
      assertThat(cr.getInputs()).hasSameSizeAs(inputs);
      cr.getInputs().stream()
          .map(
              input ->
                  (RegistryObject_component) restClient.get(RegistryObject_component.class, input))
          .forEach(
              oc -> {
                RegistryObject o =
                    (RegistryObject) restClient.get(RegistryObject.class, oc.getObject());
                RegistryStorage_location sl =
                    (RegistryStorage_location)
                        restClient.get(RegistryStorage_location.class, o.getStorage_location());
                RegistryData_product dp =
                    (RegistryData_product)
                        restClient.get(RegistryData_product.class, o.getData_products().get(0));
                String ocName = "";
                if (!oc.isWhole_object()) ocName = oc.getName();
                assertThat(inputs).contains(new Triplet<>(dp.getName(), ocName, sl.getHash()));
              });
    }

    if (outputs == null) assertThat(cr.getOutputs()).isEmpty();
    else {
      assertThat(cr.getOutputs()).hasSameSizeAs(outputs);
      cr.getOutputs()
          .forEach(
              output -> {
                RegistryObject_component oc =
                    (RegistryObject_component)
                        restClient.get(RegistryObject_component.class, output);
                RegistryObject o =
                    (RegistryObject) restClient.get(RegistryObject.class, oc.getObject());
                RegistryStorage_location sl =
                    (RegistryStorage_location)
                        restClient.get(RegistryStorage_location.class, o.getStorage_location());
                RegistryData_product dp =
                    (RegistryData_product)
                        restClient.get(RegistryData_product.class, o.getData_products().get(0));
                String ocname = "";
                if (!oc.isWhole_object()) ocname = oc.getName();
                assertThat(outputs).contains(new Triplet<>(dp.getName(), ocname, sl.getHash()));
              });
    }
  }

  @Test
  @Order(1)
  void testWriteEstimate() {
    String dataProduct = "human/population";
    String component = "estimate-component";
    try (Coderun coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeEstimate(estimate);
    }
    String hash = "a4f9d47dac45639e69a758a8b2d49bf11bbeb262";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, component, hash)));
  }

  @Test
  @Order(2)
  void testReadEstimate() {
    String dataProduct = "human/population";
    String component = "estimate-component";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dp = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dp.getComponent(component);
      assertThat(oc.readEstimate()).isEqualTo(estimate);
    }
    String hash = "a4f9d47dac45639e69a758a8b2d49bf11bbeb262";
    check_last_coderun(List.of(new Triplet<>(dataProduct, component, hash)), null);
  }

  @Test
  @Order(3)
  void testWriteDistribution() {
    String dataProduct = "human/distribution";
    String component = "distribution-component";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeDistribution(distribution);
    }
    String hash = "8e767aea46ac67c4546dacc23d302729c461fddd";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, component, hash)));
  }

  @Test
  @Order(4)
  void testReadDistribution() {
    String dataProduct = "human/distribution";
    String component = "distribution-component";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dc.getComponent(component);
      assertThat(oc.readDistribution()).isEqualTo(distribution);
    }
    String hash = "8e767aea46ac67c4546dacc23d302729c461fddd";
    check_last_coderun(List.of(new Triplet<>(dataProduct, component, hash)), null);
  }

  @Test
  @Order(5)
  void testWriteCategoricalDistribution() {
    String dataProduct = "human/cdistribution";
    String component = "cdistribution-component";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeDistribution(categoricalDistribution);
    }
    String hash = "a30b735ba6ce0340dbf264518d8a3ca1918397b8";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, component, hash)));
  }

  @Test
  @Order(6)
  void testReadCategoricalDistribution() {
    String dataProduct = "human/cdistribution";
    String component = "cdistribution-component";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dc.getComponent(component);
      assertThat(oc.readDistribution()).isEqualTo(categoricalDistribution);
    }
    String hash = "a30b735ba6ce0340dbf264518d8a3ca1918397b8";
    check_last_coderun(List.of(new Triplet<>(dataProduct, component, hash)), null);
  }

  @Test
  @Order(7)
  void testWriteSamples() {
    String dataProduct = "human/samples";
    String component = "example-samples-w";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeSamples(samples);
    }
    String hash = "83d034652197abd2f456f286c9ee7ac04500309b";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, component, hash)));
  }

  @Test
  @Order(8)
  void testReadSamples() {
    String dataProduct = "human/samples";
    String component = "example-samples-w";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dc.getComponent(component);
      assertThat(oc.readSamples()).containsExactly(1, 2, 3);
    }
    String hash = "83d034652197abd2f456f286c9ee7ac04500309b";
    check_last_coderun(List.of(new Triplet<>(dataProduct, component, hash)), null);
  }

  @Test
  @Order(9)
  void testWriteSamplesMultipleComponents() {
    String dataProduct = "human/multicomp";
    String component1 = "example-samples-w1";
    String component2 = "example-samples-w2";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);

      Object_component_write_filechannel_toml oc1 = dp.getComponent(component1);
      oc1.writeSamples(samples);

      Object_component_write_filechannel_toml oc2 = dp.getComponent(component2);
      oc2.writeSamples(samples);
    }
    String hash = "d9501d26df34a851591b1ef718564ab9f8f44c5d";
    check_last_coderun(
        null,
        Arrays.asList(
            new Triplet<>(dataProduct, component1, hash),
            new Triplet<>(dataProduct, component2, hash)));
  }

  @Test
  @Order(10)
  void testReadSamplesMultipleComponents() {
    String dataProduct = "human/multicomp";
    String component1 = "example-samples-w1";
    String component2 = "example-samples-w2";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc1 = dc.getComponent(component1);
      Object_component_read_filechannel_toml oc2 = dc.getComponent(component2);
      assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
      assertThat(oc2.readSamples()).containsExactly(1, 2, 3);
    }
    String hash = "d9501d26df34a851591b1ef718564ab9f8f44c5d";
    check_last_coderun(
        Arrays.asList(
            new Triplet<>(dataProduct, component1, hash),
            new Triplet<>(dataProduct, component2, hash)),
        null);
  }

  @Test
  @Order(11)
  void testWriteGlobDP() {
    String dataProduct = "animal/dog";
    String component = "example-samples";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeSamples(samples2);
    }
    String hash = "a12e5337b4c6dd4c8ff119da2ae47996561b5a32";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, component, hash)));
  }

  @Test
  @Order(12)
  void testReadNoGlob() {
    String dataProduct = "animal/dog";
    String component = "example-samples";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dc.getComponent(component);
      assertThat(oc.readSamples()).containsExactly(4, 5, 6);
    }
    String hash = "a12e5337b4c6dd4c8ff119da2ae47996561b5a32";
    check_last_coderun(List.of(new Triplet<>(dataProduct, component, hash)), null);
  }

  @Test
  @Order(13)
  void testWriteGlobMultiDP() {
    String dataProduct1 = "animal/horse";
    String dataProduct2 = "animal/mouse";
    String component = "example-samples";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp1 = coderun.get_dp_for_write_toml(dataProduct1);
      Data_product_write_toml dp2 = coderun.get_dp_for_write_toml(dataProduct2);
      Object_component_write_filechannel_toml oc = dp1.getComponent(component);
      oc.writeSamples(samples3);
      oc = dp2.getComponent(component);
      oc.writeSamples(samples4);
    }
    String hash1 = "0e4840da685f9d6f4777a6f4281f6a0303c720e6";
    String hash2 = "2902dcb50813de1b9db38cd21938658d53969dac";
    check_last_coderun(
        null,
        Arrays.asList(
            new Triplet<>(dataProduct1, component, hash1),
            new Triplet<>(dataProduct2, component, hash2)));
  }

  @Test
  void testIssueWithScript() {
    String issue = "I don't like this script";
    try (Coderun coderun = new Coderun(configPath, scriptPath, token)) {
      coderun.getScript().raise_issue(issue, 10);
    }
    check_issue(issue, script_desc);
  }

  @Test
  void testIssueWithConfig() {
    String issue = "I don't like this config";
    try (Coderun coderun = new Coderun(configPath, scriptPath, token)) {
      coderun.getConfig().raise_issue(issue, 10);
    }
    check_issue(issue, config_desc);
  }

  @Test
  void testIssueWithRepo() {
    String issue = "I don't like this repo";
    try (Coderun coderun = new Coderun(configPath, scriptPath, token)) {
      coderun.getCode_repo().raise_issue(issue, 10);
    }
    check_issue(issue, repo_desc);
  }

  @Test
  void testIssueWithFileObjects() {
    String issue = "I don't like any of these FileObjects";
    try (Coderun coderun = new Coderun(configPath, scriptPath, token)) {
      Issue i = coderun.raise_issue(issue, 10);
      i.add_fileObjects(coderun.getCode_repo(), coderun.getScript(), coderun.getCode_repo());
    }
    check_issue(issue, repo_desc, config_desc, script_desc);
  }

  @Test
  @Order(14)
  void testWriteSamplesMultipleComponentsAndIssues() {
    String dataProduct = "animal/dodo";
    String component1 = "example-samples-dodo1";
    String component2 = "example-samples-dodo2";
    String issue1 = "something is terribly wrong with this component";
    String issue2 = "very bad component";
    String issue3 = "this one is not so bad";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);

      Object_component_write_filechannel_toml oc1 = dp.getComponent(component1);
      oc1.raise_issue(issue1, 10);
      oc1.writeSamples(samples);
      oc1.raise_issue(issue2, 1);

      Object_component_write_filechannel_toml oc2 = dp.getComponent(component2);
      oc2.writeSamples(samples);
      oc2.raise_issue(issue3, 1);
    }
    String hash = "b1f907976527b9b3f54667505a4141ecb1942dd2";
    check_last_coderun(
        null,
        Arrays.asList(
            new Triplet<>(dataProduct, component1, hash),
            new Triplet<>(dataProduct, component2, hash)));
    check_issue(issue1, component1);
    check_issue(issue2, component1);
    check_issue(issue3, component2);
  }

  @Test
  @Order(15)
  void testReadSamplesMultipleComponentsAndIssues() {
    String dataProduct = "animal/dodo";
    String component1 = "example-samples-dodo1";
    String component2 = "example-samples-dodo2";
    String issue1 = "upon re-reading this component we found even more problems";
    String issue2 = "upon re-reading this component we found it's actually OK";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc1 = dc.getComponent(component1);
      oc1.raise_issue(issue1, 10);
      Object_component_read_filechannel_toml oc2 = dc.getComponent(component2);
      oc1.raise_issue(issue2, 0);
      assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
      assertThat(oc2.readSamples()).containsExactly(1, 2, 3);
    }
    String hash = "b1f907976527b9b3f54667505a4141ecb1942dd2";
    check_last_coderun(
        Arrays.asList(
            new Triplet<>(dataProduct, component1, hash),
            new Triplet<>(dataProduct, component2, hash)),
        null);
    check_issue(issue1, component1);
    check_issue(issue2, component1);
  }

  @Test
  @Order(16)
  void testRead_oneIssueToMultipleComp_and_script() {
    String dataProduct = "animal/dodo";
    String component1 = "example-samples-dodo1";
    String component2 = "example-samples-dodo2";
    String issue = "one issue for 2 comps";
    String issue2 = "this issue attached itself to the comps";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dc = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc1 = dc.getComponent(component1);
      Issue i = coderun.raise_issue(issue, 2);
      i.add_components(oc1);
      Object_component_read_filechannel_toml oc2 = dc.getComponent(component2);
      i.add_components(oc2);
      assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
      assertThat(oc2.readSamples()).containsExactly(1, 2, 3);
      Issue i2 = coderun.raise_issue(issue2, 3);
      i2.add_components(oc1, oc2);
      i2.add_fileObjects(coderun.getScript());
    }
    String hash = "b1f907976527b9b3f54667505a4141ecb1942dd2";
    check_last_coderun(
        Arrays.asList(
            new Triplet<>(dataProduct, component1, hash),
            new Triplet<>(dataProduct, component2, hash)),
        null);
    check_issue(issue, component1, component2);
    check_issue(issue2, component1, component2, script_desc);
  }

  @Test
  @Order(17)
  void testCSV_writeLink() throws IOException {
    String dataProduct = "animal/ant";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_link dp = coderun.get_dp_for_write_link(dataProduct, "csv");
      Object_component_write_filechannel_link oc = dp.getComponent();
      Path p = oc.writeLink();
      assertNotNull(p);
      try (PrintWriter pw = new PrintWriter(p.toFile())) {
        for (String[] s : csv_data) {
          String join = String.join(",", s);
          pw.println(join);
        }
      }
    }
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, "", CSV_hash)));
  }

  private List<String> getRecordFromLine(String line) {
    List<String> values = new ArrayList<>();
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
  void testCSV_readLink() throws IOException {
    String dataProduct = "animal/ant";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_link dp = coderun.get_dp_for_read_link(dataProduct);
      Object_component_read_filechannel_link oc = dp.getComponent();
      Path p = oc.readLink();
      int i = 0;
      try (Scanner scanner = new Scanner(p.toFile())) {
        while (scanner.hasNextLine()) {
          assertThat(getRecordFromLine(scanner.nextLine())).isEqualTo(List.of(csv_data.get(i)));
          i = i + 1;
        }
      }
    }
    check_last_coderun(List.of(new Triplet<>(dataProduct, "", CSV_hash)), null);
  }

  @Test
  @Order(19)
  void testCSV_writeLink_withIssue() throws IOException {
    String dataProduct = "animal/monkey";
    String issue = "this does not seem to contain anything monkey-related";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_link dp = coderun.get_dp_for_write_link(dataProduct, "csv");
      Object_component_write_filechannel_link oc = dp.getComponent();
      Path p = oc.writeLink();
      oc.raise_issue(issue, 9);
      assertNotNull(p);
      try (PrintWriter pw = new PrintWriter(p.toFile())) {
        csv_data.stream().map(s -> String.join(",", s)).forEach(pw::println);
      }
    }
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, "", CSV_hash)));
    check_issue(issue, dataProduct);
  }

  @Test
  @Order(20)
  void testCSV_readLink_withIssue() throws IOException {
    String dataProduct = "animal/ant";
    String issue = "not enough orange";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_link dp = coderun.get_dp_for_read_link(dataProduct);
      Object_component_read_filechannel_link oc = dp.getComponent();
      oc.raise_issue(issue, 10);
      Path p = oc.readLink();
      int i = 0;
      try (Scanner scanner = new Scanner(p.toFile())) {
        while (scanner.hasNextLine()) {
          assertThat(getRecordFromLine(scanner.nextLine())).isEqualTo(List.of(csv_data.get(i)));
          i = i + 1;
        }
      }
    }
    check_last_coderun(List.of(new Triplet<>(dataProduct, "", CSV_hash)), null);
    check_issue(issue, dataProduct);
  }

  @Test
  @Order(21)
  void testRewriteDPname() {
    String dataProduct = "animal/canine";
    String component = "NumberOfLegs";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeSamples(samples);
    }
    String rewrittenDP = "animal/doggy";
    String hash = "1b87d38867355d56f80bccb04f96d542a5a3c8bc";
    check_last_coderun(null, List.of(new Triplet<>(rewrittenDP, component, hash)));
    check_namespace(rewrittenDP, "CoderunTest");
  }

  @Test
  @Order(22)
  void testReadRewrittenDPname() {
    String dataProduct = "animal/canine";
    String component = "NumberOfLegs";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dp = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dp.getComponent(component);
      oc.readSamples();
    }
    String rewrittenDP = "animal/doggy";
    String hash = "1b87d38867355d56f80bccb04f96d542a5a3c8bc";
    check_last_coderun(List.of(new Triplet<>(rewrittenDP, component, hash)), null);
  }

  @Test
  @Order(23)
  void testAltNS() {
    String dataProduct = "test/altns";
    String component = "altNScompo";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_toml dp = coderun.get_dp_for_write_toml(dataProduct);
      Object_component_write_filechannel_toml oc = dp.getComponent(component);
      oc.writeSamples(samples);
    }
    String hash = "b8e68425f66bfb033dbbfeac3b48b922a2121ca0";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, component, hash)));
    check_namespace(dataProduct, "alternativeNS");
  }

  @Test
  @Order(24)
  void testAltNSread() {
    String dataProduct = "test/altns";
    String component = "altNScompo";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_toml dp = coderun.get_dp_for_read_toml(dataProduct);
      Object_component_read_filechannel_toml oc = dp.getComponent(component);
      oc.readSamples();
    }
    String hash = "b8e68425f66bfb033dbbfeac3b48b922a2121ca0";
    check_last_coderun(List.of(new Triplet<>(dataProduct, component, hash)), null);
  }

  @Test
  @Order(25)
  void testConfigFiletype() {
    String dataProduct = "animal/chicken";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_link dp = coderun.get_dp_for_write_link(dataProduct);
      Object_component_write_filechannel_link oc = dp.getComponent();
      try (CleanableFileChannel f = oc.getFileChannel()) {
        ByteBuffer bb = ByteBuffer.wrap(chickenTestText.getBytes(StandardCharsets.UTF_8));
        f.write(bb);
      } catch (IOException e) {
        System.out.println("failed to open FileChannel");
      }
    }
    String hash = "e19e3f4697ddf60aa09552a5f4390aed11ffd35f";
    check_last_coderun(null, List.of(new Triplet<>(dataProduct, "", hash)));

    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_link dp = coderun.get_dp_for_read_link(dataProduct);
      RegistryFile_type ft =
          (RegistryFile_type)
              restClient.get(RegistryFile_type.class, dp.registryObject.getFile_type());
      assertThat(ft.getExtension()).isEqualTo("txt");
      Object_component_read_filechannel_link oc = dp.getComponent();
      try (CleanableFileChannel f = oc.getFileChannel()) {
        ByteBuffer bb = ByteBuffer.allocate(chickenTestText.length());
        f.read(bb);
        String r = new String(bb.array());
        assertThat(r).isEqualTo(chickenTestText);
      } catch (IOException e) {
        System.out.println("failed to open FileChannel");
      }
    }
    check_last_coderun(List.of(new Triplet<>(dataProduct, "", hash)), null);
  }

  @SuppressWarnings("EmptyTryBlock")
  @Test
  @Order(26)
  void emptyCoderun() {
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      // do nothing
    }
    check_last_coderun(null, null);
  }

  @Test
  @Order(27)
  void testWriteArray() throws IOException {
    String dataProduct = "test/array1";
    String component_path = "component1/with/a/path";
    VariableName latname = new VariableName("lat", component_path);
    VariableName lonname = new VariableName("lon", component_path);
    VariableName nadefname = new VariableName("array1", component_path);

    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_write_nc dp = coderun.get_dp_for_write_nc(dataProduct);

      CoordinateVariableDefinition latdim =
          new CoordinateVariableDefinition(
              latname,
              new double[] {-75, -60, -45, -30, -15, 0, 15, 30, 45, 60, 75},
              "",
              "degrees north",
              "latitude");

      CoordinateVariableDefinition londim =
          new CoordinateVariableDefinition(
              lonname,
              new double[] {-180, -150, -120, -90, -60, -30, 0, 30, 60, 90, 120, 150},
              "",
              "degrees east",
              "longitude");
      VariableName[] dims = new VariableName[] {latname, lonname};

      DimensionalVariableDefinition nadef =
          new DimensionalVariableDefinition(
              nadefname,
              NetcdfDataType.DOUBLE,
              dims,
              "a test dataset of temperatures in space",
              "C",
              "surface temperature");
      Object_component_write_dimension oc_lon = dp.getComponent(londim);
      Object_component_write_dimension oc_lat = dp.getComponent(latdim);
      Object_component_write_array oc1 = dp.getComponent(nadef);

      double[][] temperatures = new double[11][12];
      for (int lati = 0; lati < 11; lati++)
        for (int loni = 0; loni < 12; loni++)
          temperatures[lati][loni] = lati + (double) loni / 12.0;
      NumericalArray nadat = new NumericalArrayImpl(temperatures);
      try {
        oc1.writeArrayData(nadat);
      } catch (EOFException e) {
        //
      }
    }
    String hash = "";

    check_last_coderun(
        null,
        Arrays.asList(
            new Triplet<>(dataProduct, lonname.getFullPath(), hash),
            new Triplet<>(dataProduct, latname.getFullPath(), hash),
            new Triplet<>(dataProduct, nadefname.getFullPath(), hash)));
  }

  @Test
  @Order(28)
  void testReadArray() throws IOException {
    String dataProduct = "test/array1";
    String component1 = "component1/with/a/path/array1";
    try (var coderun = new Coderun(configPath, scriptPath, token)) {
      Data_product_read_nc dc = coderun.get_dp_for_read_nc(dataProduct);
      Object_component_read_nc oc1 = dc.getComponent(component1);
      oc1.readArray();
      // assertThat(oc1.readSamples()).containsExactly(1, 2, 3);
    }
    String hash = "";
    check_last_coderun(Arrays.asList(new Triplet<>(dataProduct, component1, hash)), null);
  }
}
