package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FDPObjectTest {
  String localReg = "http://localhost:8000/api/";
  String remoteReg = "https://data.scrc.uk/api/";

  @BeforeAll
  public void setUp() throws Exception {}

  @Test
  @Disabled("FDPObject has changed structure.")
  public void testObjectReader1() throws Exception {
    FDPObject o;
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    o =
        om.readValue(
            "{\"url\":\"https://data.scrc.uk/api/object/73895/?format=json\",\"last_updated\":\"2021-03-31T13:20:51.356604Z\",\"description\":\"test file 2\",\"updated_by\":\"https://data.scrc.uk/api/users/13/?format=json\",\"storage_location\":\"https://data.scrc.uk/api/storage_location/14376/?format=json\",\"file_type\":null,\"issues\":[],\"components\":[],\"data_product\":null,\"code_repo_release\":null,\"external_object\":null,\"quality_control\":null,\"authors\":[],\"licences\":[],\"keywords\":[]}",
            FDPObject.class);
    Assertions.assertEquals(o.getUrl(), "https://data.scrc.uk/api/object/73895/?format=json");
    Assertions.assertEquals(
        o.getLast_updated(), LocalDateTime.of(2021, 3, 31, 13, 20, 51, 356604000));
    Assertions.assertEquals(o.getDescription(), "test file 2");
    Assertions.assertEquals(o.getUpdated_by(), "https://data.scrc.uk/api/users/13/?format=json");
    Assertions.assertEquals(
        o.getStorage_location(), "https://data.scrc.uk/api/storage_location/14376/?format=json");
    // Assertions.assertNull(o.getFile_type());
    Assertions.assertEquals(o.getComponents(), new ArrayList<String>() {});
    Assertions.assertNull(o.getData_product());
    Assertions.assertNull(o.getCode_repo_release());
    Assertions.assertNull(o.getQuality_control());
    Assertions.assertEquals(o.getAuthors(), new ArrayList<String>() {});
    Assertions.assertEquals(o.getLicences(), new ArrayList<String>() {});
    Assertions.assertEquals(o.getKeywords(), new ArrayList<String>() {});
  }

  @Test
  @Disabled("FDPObject has changed.")
  public void testObjectReader2() throws Exception {
    FDPObject o;
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    o =
        om.readValue(
            "{\"url\":\"https://data.scrc.uk/api/object/73884/?format=json\",\"last_updated\":\"2021-03-17T15:00:11.408021Z\",\"description\":\"\",\"updated_by\":\"https://data.scrc.uk/api/users/3/?format=json\",\"storage_location\":\"https://data.scrc.uk/api/storage_location/14350/?format=json\",\"file_type\":null,\"issues\":[],\"components\":[\"https://data.scrc.uk/api/object_component/14201/?format=json\",\"https://data.scrc.uk/api/object_component/14200/?format=json\",\"https://data.scrc.uk/api/object_component/14199/?format=json\",\"https://data.scrc.uk/api/object_component/14198/?format=json\",\"https://data.scrc.uk/api/object_component/14197/?format=json\",\"https://data.scrc.uk/api/object_component/14196/?format=json\",\"https://data.scrc.uk/api/object_component/14195/?format=json\",\"https://data.scrc.uk/api/object_component/14194/?format=json\",\"https://data.scrc.uk/api/object_component/14193/?format=json\",\"https://data.scrc.uk/api/object_component/14192/?format=json\",\"https://data.scrc.uk/api/object_component/14191/?format=json\",\"https://data.scrc.uk/api/object_component/14190/?format=json\",\"https://data.scrc.uk/api/object_component/14189/?format=json\",\"https://data.scrc.uk/api/object_component/14188/?format=json\",\"https://data.scrc.uk/api/object_component/14187/?format=json\"],\"data_product\":\"https://data.scrc.uk/api/data_product/3341/?format=json\",\"code_repo_release\":null,\"external_object\":null,\"quality_control\":null,\"authors\":[],\"licences\":[],\"keywords\":[]}",
            FDPObject.class);
    Assertions.assertEquals(o.getUrl(), "https://data.scrc.uk/api/object/73884/?format=json");
    Assertions.assertEquals(
        o.getLast_updated(), LocalDateTime.of(2021, 3, 17, 15, 0, 11, 408021000));
    Assertions.assertEquals(o.getDescription(), "");
    Assertions.assertEquals(o.getUpdated_by(), "https://data.scrc.uk/api/users/3/?format=json");
    Assertions.assertEquals(
        o.getStorage_location(), "https://data.scrc.uk/api/storage_location/14350/?format=json");
    // Assertions.assertNull(o.getFile_type());
    Assertions.assertEquals(o.getComponents().size(), 15);
    Assertions.assertEquals(
        o.getData_product(), "https://data.scrc.uk/api/data_product/3341/?format=json");
    Assertions.assertNull(o.getCode_repo_release());
    Assertions.assertNull(o.getQuality_control());
    Assertions.assertEquals(o.getAuthors(), new ArrayList<String>() {});
    Assertions.assertEquals(o.getLicences(), new ArrayList<String>() {});
    Assertions.assertEquals(o.getKeywords(), new ArrayList<String>() {});
  }
}
