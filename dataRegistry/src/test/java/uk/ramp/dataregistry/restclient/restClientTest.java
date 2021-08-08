package uk.ramp.dataregistry.restclient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.*;
import uk.ramp.dataregistry.content.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class restClientTest {
  String localReg = "http://localhost:8000/api/";
  String remoteReg = "https://data.scrc.uk/api/";
  RestClient lc, lcr;

  @BeforeAll
  public void setUp() throws Exception {
    lc = new RestClient(localReg);
    lcr = new RestClient(remoteReg);
  }

  @Disabled
  @Test
  @Order(0)
  public void get_namespace() {
    Namespace n = (Namespace) lc.get(Namespace.class, 98765);
    Assertions.assertNull(n); // i'm expecting NULL cause namespace 1 doesn't exist.
    // Assertions.assertEquals("BramTestNS", n.getName());
  }

  @Test
  @Order(1)
  public void create_namespace() {
    String name = "test namespace8";
    Namespace n = new Namespace(name);
    Namespace r = (Namespace) lc.post(n);
    System.out.println("create_namespace: " + r);
    Assertions.assertEquals(name, r.getName());
  }

  @Disabled
  @Test
  @Order(2)
  public void create_file_type() {
    String name = "Test filetype";
    String extension = ".tst";
    File_type r = (File_type) lc.post(new File_type(name, extension));
    System.out.println("create_file_type: " + r);
    Assertions.assertEquals(name, r.getName());
  }

  @Disabled
  @Test
  @Order(3)
  public void create_issue() {
    String desc = "The big issue";
    Integer severity = 1;
    Issue i = new Issue();
    i.setDescription(desc);
    i.setSeverity(severity);
    Issue r = (Issue) lc.post(i);
    System.out.println("create_issue: " + r);
    Assertions.assertEquals(desc, r.getDescription());
    Assertions.assertEquals(severity, r.getSeverity());
  }

  @Disabled
  @Test
  @Order(4)
  public void create_storageroot() {
    String root = "StorageRoot";
    Storage_root sr = new Storage_root();
    sr.setLocal(true);
    sr.setRoot(root);
    Storage_root r = (Storage_root) lc.post(sr);
    System.out.println("create_storageroot Response: " + r);
    Assertions.assertEquals(root, r.getRoot());
  }

  @Disabled
  @Test
  @Order(5)
  public void create_storagelocation() {
    Storage_location sl = new Storage_location();
    String stroot = localReg + "storage_root/1/";
    String path = "StorageLocation";
    String hash = "myhash";
    sl.setStorage_root(stroot);
    sl.setPath(path);
    sl.setHash(hash); // hash is not allowed to be empty!
    Storage_location r = (Storage_location) lc.post(sl);
    System.out.println("create_storageLocation: " + r);
    Assertions.assertEquals(stroot, r.getStorage_root());
  }

  @Disabled
  @Test
  @Order(7)
  public void create_object() {
    FDPObject o = new FDPObject();
    o.setDescription("my new object description");
    o.setStorage_location(localReg + "storage_location/1/");
    // Response r = lc.post(o);
    // System.out.println("create_object: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(8)
  public void create_data_product() {
    Data_product dp = new Data_product();
    dp.setName("Initial Data_product");
    dp.setObject(localReg + "object/4/");
    dp.setVersion("1.bla.piep");
    dp.setNamespace(localReg + "namespace/1/");
    // Response r = lc.post(dp);
    // System.out.println("create_data_product: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(9)
  public void create_externalObject() {
    // version must be a valid version [removed in this version?]
    // object,source,original store must be valid URLs
    // obj & source must be supplied. original_store is optional
    External_object eo = new External_object();
    eo.setDescription("My test external object");
    eo.setDoi_or_unique_name("My very unique name");
    eo.setRelease_date(LocalDateTime.of(2021, 4, 4, 4, 4, 4, 4));
    eo.setTitle("Initial External Object");
    eo.setData_product(localReg + "data_product/1/");
    eo.setPrimary_not_supplement(true);
    // Response r = lc.post(eo);
    // System.out.println("create_externalObject: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(10)
  public void create_objectComponent() {
    Object_component oc = new Object_component();
    oc.setName("Initial object component");
    oc.setObject(localReg + "object/1/");
    oc.setDescription("My test object component");
    // Response r = lc.post(oc);
    // System.out.println("create_objectComponent: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(11)
  public void create_codeRun() {
    Code_run cr = new Code_run();
    cr.setDescription("My test codeRun");
    cr.setRun_date(LocalDateTime.of(2021, 3, 3, 3, 3, 3, 3));
    cr.setCode_repo(localReg + "object/1/");
    cr.setSubmission_script(localReg + "object/1/");
    // Response r = lc.post(cr);
    // System.out.println("create_codeRun: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(12)
  public void create_author() {
    Author a = new Author();
    a.setFamily_name("Boskamp");
    a.setGiven_name("Bram");
    // Response r = lc.post(a);
    // System.out.println("create_author: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(13)
  public void create_keyword() {
    Keyword k = new Keyword();
    k.setObject(localReg + "object/1/");
    k.setKeyphrase("huh");
    // Response r = lc.post(k);
    // System.out.println("create_keyword: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(14)
  public void create_code_repo_release() {
    Code_repo_release crr = new Code_repo_release();
    crr.setName("Initial code repo release");
    crr.setObject(localReg + "object/1/");
    crr.setVersion("1.0.0");
    crr.setWebsite("http://github.com/blabla");
    // Response r = lc.post(crr);
    // System.out.println("create_code_repo_release: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(15)
  public void create_key_value() {
    Key_value kv = new Key_value();
    kv.setObject(localReg + "object/1/");
    kv.setKey("the key");
    kv.setValue("the value");
    // Response r = lc.post(kv);
    // System.out.println("create_key_value: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(17)
  public void get_wrongClass() {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          lc.get(Object.class, 1);
        });
  }

  @Disabled
  @Test
  @Order(18)
  public void get_file_type() {
    File_type f = (File_type) lc.get(File_type.class, 1);
    Assertions.assertEquals("Test filetype", f.getName());
  }

  @Disabled
  @Test
  @Order(19)
  public void get_first() {
    File_type f = (File_type) lc.getFirst(File_type.class, new HashMap<String, String>() {});
    Assertions.assertEquals(".tst", f.getExtension());
  }

  @Disabled
  @Test
  @Order(20)
  public void get_first_of_none() {
    Map<String, String> m = new HashMap<String, String>() {};
    m.put("extension", "blow");
    FDP_ObjectList<?> ol = lc.getList(File_type.class, m);
    System.out.println("get_first_of_none ol.count: " + ol.getCount());

    File_type f = (File_type) lc.getFirst(File_type.class, m);
    if (f != null) {
      System.out.println(f.getUrl());
    }
    Assertions.assertNull(f);
  }
}
