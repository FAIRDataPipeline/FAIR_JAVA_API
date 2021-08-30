package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class file_typeTest {
  String localReg = "http://localhost:8000/api/";
  String remoteReg = "https://data.scrc.uk/api/";
  RestClient rl;
  String jsonstring =
      "{\"url\":\"https://data.scrc.uk/api/file_type/5/?format=json\",\"last_updated\":\"2021-03-04T14:43:57.160401Z\",\"name\":\"YAML Ain't Markup Language\",\"extension\":\"yaml\",\"updated_by\":\"https://data.scrc.uk/api/users/13/?format=json\"}";
  String url = "https://data.scrc.uk/api/file_type/5/?format=json";
  String updated_by = "https://data.scrc.uk/api/users/13/?format=json";
  LocalDateTime d = LocalDateTime.of(2021, 3, 4, 14, 43, 57, 160401000);
  String name = "YAML Ain't Markup Language";
  String extension = "yaml";

  @BeforeAll
  public void setUp() throws Exception {
    rl = new RestClient(localReg);
  }

  @Test
  @Order(1)
  public void testFile_typeReader() throws Exception {
    RegistryFile_type o;
    ObjectMapper om = new ObjectMapper();
    System.out.println("TESTFILE_TYPEREADER1");
    om.registerModule(new JavaTimeModule());
    o = om.readValue(jsonstring, RegistryFile_type.class);
    Assertions.assertEquals(o.getUrl(), url);
    Assertions.assertEquals(o.getLast_updated(), d);
    Assertions.assertEquals(o.getUpdated_by(), updated_by);
    Assertions.assertEquals(o.getName(), name);
    Assertions.assertEquals(o.getExtension(), extension);
  }

  @Test
  @Order(2)
  void testFile_typeTwoWayTest() throws Exception {
    System.out.println("TESTFILE_TYPEWRITER");
    RegistryFile_type o = new RegistryFile_type();
    ObjectMapper om = new ObjectMapper();
    JavaTimeModule jtm = new JavaTimeModule();
    jtm.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
    om.registerModule(jtm);
    o.setName(name);
    o.setUpdated_by(updated_by);
    o.setUrl(url);
    o.setLast_updated(d);
    o.setExtension(extension);
    String s = om.writeValueAsString(o);
    RegistryFile_type o2 = om.readValue(s, RegistryFile_type.class);
    Assertions.assertEquals(name, o2.getName());
    Assertions.assertEquals(url, o2.getUrl());
    Assertions.assertEquals(updated_by, o2.getUpdated_by());
    Assertions.assertEquals(d, o2.getLast_updated());
    Assertions.assertEquals(extension, o2.getExtension());
  }
}
