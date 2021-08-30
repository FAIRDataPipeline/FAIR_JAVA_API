package org.fairdatapipeline.dataregistry.restclient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;
import org.fairdatapipeline.dataregistry.content.*;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_location;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class populateLocal {
  RestClient lc;
  String localReg = "http://localhost:8000/api/";

  @BeforeAll
  public void setUp() throws Exception {
    lc = new RestClient(localReg);
    System.out.println("Created the RestClient in setup()");
  }

  @ParameterizedTest
  @MethodSource("objectsToBeCreated")
  @DisabledIf("keyvalueExists")
  public void createObjects(Registry_Updateable o) {
    Registry_Updateable r = lc.post(o);
    System.out.println("createObj.. statusinfo: " + r);
    Assertions.assertNotNull(r);
  }

  private Stream<Registry_Updateable> objectsToBeCreated() {
    ArrayList<Registry_Updateable> al = new ArrayList<Registry_Updateable>();
    al.add(new RegistryNamespace("test namespace"));
    al.add(new RegistryFile_type("Test filetype", ".tst"));
    RegistryIssue i = new RegistryIssue();
    i.setDescription("the big issue");
    i.setSeverity(1);
    al.add(i);
    RegistryStorage_root sr = new RegistryStorage_root();
    sr.setRoot("D:\\Datastore");
    sr.setLocal(true);
    sr.setLocations(new ArrayList<>() {});
    al.add(sr);
    RegistryStorage_location sl = new RegistryStorage_location();
    sl.setPath("StorageLocation");
    sl.setStorage_root(localReg + "storage_root/1/");
    sl.setHash("myHash");
    al.add(sl);
    /*Source s = new Source();
    s.setName("Test source");
    s.setAbbreviation("tstsrc");
    s.setWebsite("http://github.com/testsource/");
    al.add(s);*/
    RegistryObject o = new RegistryObject();
    o.setDescription("my new object description");
    o.setStorage_location(localReg + "storage_location/1/");
    al.add(o);
    RegistryData_product dp = new RegistryData_product();
    dp.setName("Initial Data_product");
    dp.setObject(localReg + "object/1/");
    dp.setVersion("1.0.0");
    dp.setNamespace(localReg + "namespace/1/");
    al.add(dp);
    RegistryExternal_object eo = new RegistryExternal_object();
    eo.setDescription("My test external object");
    eo.setIdentifier("http://www.bbc.co.uk/");
    eo.setRelease_date(LocalDateTime.of(2021, 4, 4, 4, 4, 4, 4));
    eo.setTitle("Initial External Object");
    eo.setData_product(localReg + "data_product/1/");
    eo.setPrimary_not_supplement(true);
    al.add(eo);
    RegistryObject_component oc = new RegistryObject_component();
    oc.setName("Initial object component");
    oc.setObject(localReg + "object/1/");
    oc.setDescription("My test object component");
    al.add(oc);
    RegistryCode_run cr = new RegistryCode_run();
    cr.setDescription("My test codeRun");
    cr.setRun_date(LocalDateTime.of(2021, 3, 3, 3, 3, 3, 3));
    cr.setCode_repo(localReg + "object/1/");
    cr.setSubmission_script(localReg + "object/1/");
    al.add(cr);
    RegistryAuthor a = new RegistryAuthor();
    a.setName("Bram Boskamp");
    al.add(a);
    RegistryKeyword k = new RegistryKeyword();
    k.setObject(localReg + "object/1/");
    k.setKeyphrase("huh");
    al.add(k);
    RegistryCode_repo_release crr = new RegistryCode_repo_release();
    crr.setName("Initial code repo release");
    crr.setObject(localReg + "object/1/");
    crr.setVersion("1.0.0");
    crr.setWebsite("http://github.com/blabla");
    al.add(crr);
    RegistryKey_value kv = new RegistryKey_value();
    kv.setObject(localReg + "object/1/");
    kv.setKey("the key");
    kv.setValue("the value");
    al.add(kv);
    // al.add(new Text_file("this is the contents of the text file."));

    System.out.println(
        "Created the ArrayList with FDP_Objects to be created.. number of elements: " + al.size());
    return al.stream();
  }

  private boolean keyvalueExists() {
    Registry_ObjectList<?> sr =
        (Registry_ObjectList<?>) lc.getList(RegistryKey_value.class, new HashMap<String, String>());
    System.out.println("keyvalueExists? " + (sr.getCount() != 0));
    return sr.getCount() != 0;
  }
}
