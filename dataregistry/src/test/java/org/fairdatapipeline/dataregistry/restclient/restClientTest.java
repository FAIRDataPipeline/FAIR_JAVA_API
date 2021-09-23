package org.fairdatapipeline.dataregistry.restclient;

import java.net.URL;
import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "LOCALREG", matches = "FRESHASADAISY")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class restClientTest {
  String localReg = "http://localhost:8000/api/";
  String badReg = "http://localhost:1234/nothere/";
  RestClient lc;

  @BeforeAll
  public void setUp() {
    lc = new RestClient(localReg);
  }

  @Test
  public void wrongToken() {
    RestClient lc2 = new RestClient(localReg, "bad token");
    Assertions.assertThrows(
        org.fairdatapipeline.dataregistry.restclient.ForbiddenException.class,
        () -> lc2.getFirst(RegistryUsers.class, Collections.emptyMap()));
  }

  @Test
  void wrongRegistry() {
    // i'm faking a JSON error by interpreting a user as a code_run..
    Assertions.assertThrows(
        RegistryJSONException.class,
        () -> lc.get(RegistryCode_run.class, new URL(localReg + "users/1/")));
  }

  @Test
  public void wrongReg() {
    RestClient lc2 = new RestClient(badReg);
    Assertions.assertThrows(
        org.fairdatapipeline.dataregistry.restclient.ConnectException.class,
        () -> lc2.getFirst(RegistryUsers.class, Collections.emptyMap()));
  }

  @Test
  public void wisnaeThereAtAw_get_by_id() {
    RegistryNamespace n = (RegistryNamespace) lc.get(RegistryNamespace.class, 98765);
    Assertions.assertNull(n); // i'm expecting NULL cause namespace 98765 doesn't exist.
  }

  @Test
  public void wisnaeThereAtAw_getList() {
    Registry_ObjectList<?> ol =
        lc.getList(
            RegistryFile_type.class,
            Collections.singletonMap("extension", "AVeryUnlikelyExtension"));
    Assertions.assertEquals(ol.getCount(), 0);
  }

  @Test
  public void wisnaeThereAtAw_getFirst() {
    RegistryFile_type f =
        (RegistryFile_type)
            lc.getFirst(
                RegistryFile_type.class,
                Collections.singletonMap("extension", "AVeryUnlikelyExtension"));
    Assertions.assertNull(f);
  }

  @Test
  public void breakUniqueConstraint() {
    String name = "test namespace";
    if (lc.getFirst(RegistryNamespace.class, Collections.singletonMap("name", name)) == null) {
      lc.post(new RegistryNamespace(name));
    }
    Assertions.assertNull(lc.post(new RegistryNamespace(name)));
  }

  @Test
  public void get_wrongClass() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> lc.get(Object.class, 1));
  }
}
