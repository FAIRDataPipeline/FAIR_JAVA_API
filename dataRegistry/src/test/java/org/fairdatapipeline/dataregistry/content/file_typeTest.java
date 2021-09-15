package org.fairdatapipeline.dataregistry.content;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class file_typeTest {
  String jsonstring =
      "{\"url\":\"https://data.scrc.uk/api/file_type/5/?format=json\",\"last_updated\":\"2021-03-04T14:43:57.160401Z\",\"name\":\"YAML Ain't Markup Language\",\"extension\":\"yaml\",\"updated_by\":\"https://data.scrc.uk/api/users/13/?format=json\"}";
  String url = "https://data.scrc.uk/api/file_type/5/?format=json";
  String updated_by = "https://data.scrc.uk/api/users/13/?format=json";
  LocalDateTime d = LocalDateTime.of(2021, 3, 4, 14, 43, 57, 160401000);
  String name = "YAML Ain't Markup Language";
  String extension = "yaml";
  RegistryFile_type expected;

  @BeforeAll
  public void setUp() throws Exception {
    expected = new RegistryFile_type();
    expected.setUrl(url);
    expected.setLast_updated(d);
    expected.setUpdated_by(updated_by);
    expected.setName(name);
    expected.setExtension(extension);
  }

  @Test
  public void testFile_typeReader() throws Exception {
    RegistryFile_type from_json;
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    from_json = om.readValue(jsonstring, RegistryFile_type.class);
    assertThat(from_json, samePropertyValuesAs(expected));
  }

  @Test
  void testFile_typeTwoWayTest() throws Exception {
    ObjectMapper om = new ObjectMapper();
    JavaTimeModule jtm = new JavaTimeModule();
    jtm.addSerializer(
        LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
    om.registerModule(jtm);
    String s = om.writeValueAsString(expected);
    RegistryFile_type returned = om.readValue(s, RegistryFile_type.class);
    assertThat(returned, samePropertyValuesAs(expected));
  }
}
