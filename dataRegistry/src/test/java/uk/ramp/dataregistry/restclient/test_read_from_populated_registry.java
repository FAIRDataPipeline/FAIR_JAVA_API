package uk.ramp.dataregistry.restclient;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.ramp.dataregistry.content.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class test_read_from_populated_registry {
  String registry = "http://localhost:8000/api/";
  RestClient lc;
  Map<String, String> m;

  @BeforeAll
  public void setUp() throws Exception {
    this.lc = new RestClient(registry);
    this.m = Collections.emptyMap();
  }

  @ParameterizedTest
  @ValueSource(
      classes = {
        RegistryAuthor.class,
        RegistryCode_repo_release.class,
        RegistryCode_run.class,
        RegistryData_product.class,
        RegistryExternal_object.class,
        RegistryObject.class,
        RegistryFile_type.class,
        RegistryIssue.class,
        RegistryKey_value.class,
        RegistryKeyword.class,
        RegistryNamespace.class,
        RegistryObject_component.class,
        RegistryStorage_location.class,
        RegistryStorage_root.class,
        RegistryUsers.class
      })
  public void get_object(Class c) {
    Registry_RootObject n = lc.getFirst(c, m);
    System.out.println(c.getName() + " id: " + n.get_id());
    Assertions.assertNotNull(n.getUrl());
  }
}
