package org.fairdatapipeline.dataregistry.restclient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.*;

@Disabled
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
    RegistryNamespace n = (RegistryNamespace) lc.get(RegistryNamespace.class, 98765);
    Assertions.assertNull(n); // i'm expecting NULL cause namespace 1 doesn't exist.
    // Assertions.assertEquals("BramTestNS", n.getName());
  }

  @Disabled
  @Test
  @Order(1)
  public void create_namespace() {
    String name = "test namespace8x";
    RegistryNamespace n = new RegistryNamespace(name);
    RegistryNamespace r = (RegistryNamespace) lc.post(n);
    System.out.println("create_namespace: " + r);
    Assertions.assertEquals(name, r.getName());
  }

  @Disabled
  @Test
  @Order(2)
  public void create_file_type() {
    String name = "Test filetype";
    String extension = ".tst";
    RegistryFile_type r = (RegistryFile_type) lc.post(new RegistryFile_type(name, extension));
    System.out.println("create_file_type: " + r);
    Assertions.assertEquals(name, r.getName());
  }

  @Disabled
  @Test
  @Order(3)
  public void create_issue() {
    String desc = "The big issue";
    Integer severity = 1;
    RegistryIssue i = new RegistryIssue();
    i.setDescription(desc);
    i.setSeverity(severity);
    RegistryIssue r = (RegistryIssue) lc.post(i);
    System.out.println("create_issue: " + r);
    Assertions.assertEquals(desc, r.getDescription());
    Assertions.assertEquals(severity, r.getSeverity());
  }

  @Disabled
  @Test
  @Order(4)
  public void create_storageroot() {
    String root = "StorageRoot";
    RegistryStorage_root sr = new RegistryStorage_root();
    sr.setLocal(true);
    sr.setRoot(root);
    RegistryStorage_root r = (RegistryStorage_root) lc.post(sr);
    System.out.println("create_storageroot Response: " + r);
    Assertions.assertEquals(root, r.getRoot());
  }

  @Disabled
  @Test
  @Order(5)
  public void create_storagelocation() {
    RegistryStorage_location sl = new RegistryStorage_location();
    String stroot = localReg + "storage_root/1/";
    String path = "StorageLocation";
    String hash = "myhash";
    sl.setStorage_root(stroot);
    sl.setPath(path);
    sl.setHash(hash); // hash is not allowed to be empty!
    RegistryStorage_location r = (RegistryStorage_location) lc.post(sl);
    System.out.println("create_storageLocation: " + r);
    Assertions.assertEquals(stroot, r.getStorage_root());
  }

  @Disabled
  @Test
  @Order(7)
  public void create_object() {
    RegistryObject o = new RegistryObject();
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
    RegistryData_product dp = new RegistryData_product();
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
    RegistryExternal_object eo = new RegistryExternal_object();
    eo.setDescription("My test external object");
    eo.setIdentifier("http://www.xs4all.nl/");
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
    RegistryObject_component oc = new RegistryObject_component();
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
    RegistryCode_run cr = new RegistryCode_run();
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
    RegistryAuthor a = new RegistryAuthor();
    a.setName("Bram Boskamp");
    // Response r = lc.post(a);
    // System.out.println("create_author: " + r.getStatusInfo());
    // Assertions.assertEquals(201, r.getStatus());
  }

  @Disabled
  @Test
  @Order(13)
  public void create_keyword() {
    RegistryKeyword k = new RegistryKeyword();
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
    RegistryCode_repo_release crr = new RegistryCode_repo_release();
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
    RegistryKey_value kv = new RegistryKey_value();
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
    RegistryFile_type f = (RegistryFile_type) lc.get(RegistryFile_type.class, 1);
    Assertions.assertEquals("Test filetype", f.getName());
  }

  @Disabled
  @Test
  @Order(19)
  public void get_first() {
    RegistryFile_type f =
        (RegistryFile_type) lc.getFirst(RegistryFile_type.class, new HashMap<String, String>() {});
    Assertions.assertEquals(".tst", f.getExtension());
  }

  @Disabled
  @Test
  @Order(20)
  public void get_first_of_none() {
    Map<String, String> m = new HashMap<String, String>() {};
    m.put("extension", "blow");
    Registry_ObjectList<?> ol = lc.getList(RegistryFile_type.class, m);
    System.out.println("get_first_of_none ol.count: " + ol.getCount());

    RegistryFile_type f = (RegistryFile_type) lc.getFirst(RegistryFile_type.class, m);
    if (f != null) {
      System.out.println(f.getUrl());
    }
    Assertions.assertNull(f);
  }
}
