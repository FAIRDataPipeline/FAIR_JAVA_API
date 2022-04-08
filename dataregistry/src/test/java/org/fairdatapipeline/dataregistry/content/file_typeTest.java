package org.fairdatapipeline.dataregistry.content;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.fairdatapipeline.dataregistry.restclient.APIURL;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class file_typeTest {
  String json_url = "https://data.scrc.uk/api/file_type/5/?format=json";
  String json_lastupdated = "2021-03-04T14:43:57.160401Z";
  String json_name = "YAML Ain't Markup Language";
  String json_updated_by = "https://data.scrc.uk/api/users/13/?format=json";
  String json_extension = "yaml";
  String JSONString =
      "{\"url\":\""
          + json_url
          + "\",\"last_updated\":\""
          + json_lastupdated
          + "\",\"name\":\""
          + json_name
          + "\",\"extension\":\""
          + json_extension
          + "\",\"updated_by\":\""
          + json_updated_by
          + "\"}";
  APIURL url;
  APIURL updated_by;
  LocalDateTime d = LocalDateTime.of(2021, 3, 4, 14, 43, 57, 160401000);
  RegistryFile_type expected;

  @BeforeAll
  public void setUp() throws URISyntaxException {
    url = mock(APIURL.class);
    when(url.getUri()).thenReturn(new URI(json_url));
    when(url.toString()).thenReturn(json_url);
    when(url.getPath()).thenReturn(new URI(json_url).getPath());
    updated_by = mock(APIURL.class);
    when(updated_by.getUri()).thenReturn(new URI(json_updated_by));
    when(updated_by.getPath()).thenReturn(new URI(json_updated_by).getPath());
    when(updated_by.toString()).thenReturn(json_updated_by);
    expected = new RegistryFile_type();
    expected.setUrl(url);
    expected.setLast_updated(d);
    expected.setUpdated_by(updated_by);
    expected.setName(json_name);
    expected.setExtension(json_extension);
  }

  @Test
  void testFile_typeReader() throws Exception {
    RegistryFile_type from_json;
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    from_json = om.readValue(JSONString, RegistryFile_type.class);
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
