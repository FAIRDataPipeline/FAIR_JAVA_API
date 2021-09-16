package org.fairdatapipeline.dataregistry.restclient;

import java.util.Collections;
import java.util.Map;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class test_read_from_populated_registry {
  String registry = "http://localhost:8000/api/";
  RestClient lc;
  Map<String, String> m;

  @BeforeAll
  public void setUp() {
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
}
