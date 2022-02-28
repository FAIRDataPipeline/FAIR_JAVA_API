package org.fairdatapipeline.dataregistry.restclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;

import jakarta.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@EnabledIfEnvironmentVariable(named = "LOCALREG", matches = "FRESHASADAISY")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class restClientTest {
  String localReg = "http://localhost:8000/api/";
  String badReg = "http://localhost:1234/nothere/";
  RestClient lc;
  Map<String, String> m;

  @BeforeAll
  public void setUp() {
    Assertions.assertNotNull(System.getenv("REGTOKEN"));
    lc = new RestClient(localReg, System.getenv("REGTOKEN"));
    create_author();
    this.m = Collections.emptyMap();
  }

  @Order(1)
  @ParameterizedTest
  @MethodSource("objectsToBeCreated")
  @DisabledIf("keyvalueExists")
  public void createObjects(Registry_Updateable o) {
    Registry_Updateable r = lc.post(o);
    Assertions.assertNotNull(r);
    switch (o.getClass().getSimpleName()) {
      case ("RegistryCode_run"):
        assertThat(
            r,
            samePropertyValuesAs(
                o,
                "uuid",
                "url",
                "updated_by",
                "last_updated",
                "_id",
                "inputs",
                "outputs",
                "prov_report",
                "run_date"));
        break;
      case ("RegistryIssue"):
        assertThat(
            r,
            samePropertyValuesAs(
                o, "uuid", "url", "updated_by", "last_updated", "_id", "component_issues"));
        break;
      case ("RegistryObject"):
        assertThat(
            r,
            samePropertyValuesAs(
                o,
                "uuid",
                "url",
                "updated_by",
                "last_updated",
                "_id",
                "authors",
                "components",
                "data_products",
                "licenses",
                "keywords"));
        break;
      case ("RegistryObject_component"):
        assertThat(
            r,
            samePropertyValuesAs(
                o,
                "uuid",
                "url",
                "updated_by",
                "last_updated",
                "_id",
                "issues",
                "inputs_of",
                "outputs_of"));
        break;
      case ("RegistryStorage_root"):
        assertThat(
            r,
            samePropertyValuesAs(
                o, "uuid", "url", "updated_by", "last_updated", "_id", "locations"));
        break;
      case ("RegistryUsers"):
        assertThat(
            r, samePropertyValuesAs(o, "uuid", "url", "updated_by", "last_updated", "_id", "orgs"));
        break;
      case ("RegistryExternal_object"):
        assertThat(
            r,
            samePropertyValuesAs(
                o, "uuid", "url", "updated_by", "last_updated", "_id", "release_date", "version"));
        break;
      case ("RegistryData_product"):
        assertThat(
            r,
            samePropertyValuesAs(
                o, "uuid", "url", "updated_by", "last_updated", "_id", "prov_report"));
        break;
      default:
        assertThat(r, samePropertyValuesAs(o, "uuid", "url", "updated_by", "last_updated", "_id"));
        break;
    }
  }

  @Order(2)
  @ParameterizedTest
  @ValueSource(
      classes = {
        RegistryAuthor.class,
        RegistryCode_repo_release.class,
        RegistryCode_run.class,
        RegistryData_product.class,
        RegistryExternal_object.class,
        RegistryFile_type.class,
        RegistryObject.class,
        RegistryFile_type.class,
        RegistryIssue.class,
        RegistryKey_value.class,
        RegistryKeyword.class,
        RegistryNamespace.class,
        RegistryObject.class,
        RegistryObject_component.class,
        RegistryStorage_location.class,
        RegistryStorage_root.class,
        RegistryUsers.class
      })
  public void get_object(Class<Registry_RootObject> c) {
    Registry_RootObject n = lc.getFirst(c, m);
    Assertions.assertNotNull(n.getUrl());
  }

  private Stream<Registry_Updateable> objectsToBeCreated() throws MalformedURLException {
    ArrayList<Registry_Updateable> al = new ArrayList<>();
    al.add(new RegistryNamespace("test namespace"));
    al.add(new RegistryFile_type("Test filetype", ".tst"));
    RegistryIssue i = new RegistryIssue();
    i.setDescription("the big issue");
    i.setSeverity(1);
    al.add(i);
    RegistryStorage_root sr = new RegistryStorage_root();
    sr.setRoot(Path.of("D:\\Datastore").toUri());
    sr.setLocal(true);
    al.add(sr);
    RegistryStorage_location sl = new RegistryStorage_location();
    sl.setPath("StorageLocation");
    sl.setStorage_root(lc.makeAPIURL(RegistryStorage_root.class, 1));
    sl.setHash("myHash");
    al.add(sl);
    RegistryObject o = new RegistryObject();
    o.setDescription("my new object description");
    o.setStorage_location(lc.makeAPIURL(RegistryStorage_location.class, 1));
    al.add(o);
    RegistryData_product dp = new RegistryData_product();
    dp.setName("Initial Data_product");
    dp.setObject(lc.makeAPIURL(RegistryObject.class, 1));
    dp.setVersion("1.0.0");
    dp.setNamespace(lc.makeAPIURL(RegistryNamespace.class, 1));
    al.add(dp);
    RegistryExternal_object eo = new RegistryExternal_object();
    eo.setDescription("My test external object");
    eo.setIdentifier(new URL("http://www.bbc.co.uk/"));
    eo.setRelease_date(LocalDateTime.of(2021, 4, 4, 4, 4, 4, 4));
    eo.setTitle("Initial External Object");
    eo.setData_product(lc.makeAPIURL(RegistryData_product.class, 1));
    eo.setPrimary_not_supplement(true);
    al.add(eo);
    RegistryObject_component oc = new RegistryObject_component();
    oc.setName("Initial object component");
    oc.setObject(lc.makeAPIURL(RegistryObject.class, 1));
    oc.setDescription("My test object component");
    al.add(oc);
    RegistryCode_run cr = new RegistryCode_run();
    cr.setDescription("My test codeRun");
    cr.setRun_date(LocalDateTime.of(2021, 3, 3, 3, 3, 3, 3));
    cr.setCode_repo(lc.makeAPIURL(RegistryObject.class, 1));
    cr.setSubmission_script(lc.makeAPIURL(RegistryObject.class, 1));
    al.add(cr);
    RegistryAuthor a = new RegistryAuthor();
    a.setName("Bram Boskamp");
    al.add(a);
    RegistryKeyword k = new RegistryKeyword();
    k.setObject(lc.makeAPIURL(RegistryObject.class, 1));
    k.setKeyphrase("huh");
    al.add(k);
    RegistryCode_repo_release crr = new RegistryCode_repo_release();
    crr.setName("Initial code repo release");
    crr.setObject(lc.makeAPIURL(RegistryObject.class, 1));
    crr.setVersion("1.0.0");
    crr.setWebsite(new URL("http://github.com/blabla"));
    al.add(crr);
    RegistryKey_value kv = new RegistryKey_value();
    kv.setObject(lc.makeAPIURL(RegistryObject.class, 1));
    kv.setKey("the key");
    kv.setValue("the value");
    al.add(kv);

    return al.stream();
  }

  private boolean keyvalueExists() {
    Registry_ObjectList<?> sr = lc.getList(RegistryKey_value.class, new HashMap<>());
    return sr.getCount() != 0;
  }

  void create_author() {
    if (lc.getFirst(RegistryAuthor.class, Collections.emptyMap()) == null) {
      RegistryAuthor author = new RegistryAuthor();
      author.setName("An Anonymous Author");
      lc.post(author);
    }
  }

  @Order(3)
  @Test
  public void wrongVersion() {
    class RestClient_wv extends RestClient {
      private final MediaType jsonWithVersion =
          new MediaType("application", "json", Collections.singletonMap("version", "0.0.0"));

      public RestClient_wv(String registry_url, String token) {
        super(registry_url, token);
      }

      @Override
      MediaType getJsonMediaType() {
        return this.jsonWithVersion;
      }
    }
    RestClient_wv restClient_wv = new RestClient_wv(localReg, System.getenv("REGTOKEN"));
    Assertions.assertThrows(
        RegistryVersionException.class,
        () -> restClient_wv.getFirst(RegistryUsers.class, Collections.emptyMap()));
  }

  @Order(4)
  @Test
  public void wrongToken() {
    RestClient lc2 = new RestClient(localReg, "bad token");
    Assertions.assertThrows(
        org.fairdatapipeline.dataregistry.restclient.ForbiddenException.class,
        () -> lc2.getFirst(RegistryUsers.class, Collections.emptyMap()));
  }

  @Order(5)
  @Test
  void wrongRegistry() {
    // i'm faking a JSON error by interpreting a user as a code_run..
    Assertions.assertThrows(
        RegistryJSONException.class,
        () -> lc.get(RegistryCode_run.class, lc.makeAPIURL(RegistryUsers.class, 1)));
  }

  @Order(6)
  @Test
  public void wrongReg() {
    RestClient lc2 = new RestClient(badReg, "any token");
    Assertions.assertThrows(
        org.fairdatapipeline.dataregistry.restclient.ConnectException.class,
        () -> lc2.getFirst(RegistryUsers.class, Collections.emptyMap()));
  }

  @Order(7)
  @Test
  public void wisnaeThereAtAw_get_by_id() {
    RegistryNamespace n = (RegistryNamespace) lc.get(RegistryNamespace.class, 98765);
    Assertions.assertNull(n); // i'm expecting NULL cause namespace 98765 doesn't exist.
  }

  @Order(8)
  @Test
  public void wisnaeThereAtAw_getList() {
    Registry_ObjectList<?> ol =
        lc.getList(
            RegistryFile_type.class,
            Collections.singletonMap("extension", "AVeryUnlikelyExtension"));
    Assertions.assertEquals(ol.getCount(), 0);
  }

  @Order(9)
  @Test
  public void wisnaeThereAtAw_getFirst() {
    RegistryFile_type f =
        (RegistryFile_type)
            lc.getFirst(
                RegistryFile_type.class,
                Collections.singletonMap("extension", "AVeryUnlikelyExtension"));
    Assertions.assertNull(f);
  }

  @Order(10)
  @Test
  public void breakUniqueConstraint() {
    String name = "test namespace";
    if (lc.getFirst(RegistryNamespace.class, Collections.singletonMap("name", name)) == null) {
      lc.post(new RegistryNamespace(name));
    }
    Assertions.assertNull(lc.post(new RegistryNamespace(name)));
  }
}
