package org.fairdatapipeline.dataregistry.restclient;

import jakarta.ws.rs.core.MediaType;
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
    Assertions.assertNotNull(System.getenv("REGTOKEN"));
    lc = new RestClient(localReg, System.getenv("REGTOKEN"));
    create_author();
  }

  void create_author() {
    if (lc.getFirst(RegistryAuthor.class, Collections.emptyMap()) == null) {
      RegistryAuthor author = new RegistryAuthor();
      author.setName("An Anonymous Author");
      lc.post(author);
    }
  }

  @Disabled("enable this once registry has version checking")
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
        () -> lc.get(RegistryCode_run.class, lc.makeAPIURL(RegistryUsers.class, 1)));
  }

  @Test
  public void wrongReg() {
    RestClient lc2 = new RestClient(badReg, "any token");
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
}
