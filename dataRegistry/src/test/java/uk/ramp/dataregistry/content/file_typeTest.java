package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;

import uk.ramp.dataregistry.restclient.RestClient;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class file_typeTest {
    String localReg = "http://localhost:8000/api/";
    String remoteReg = "https://data.scrc.uk/api/";
    RestClient rl;
    @BeforeAll
    public void setUp() throws Exception {
        rl = new RestClient(localReg);
    }

    @Test
    @Order(1)
    public void testFile_typeReader1() throws Exception {
        File_type o;
        ObjectMapper om = new ObjectMapper();
        System.out.println("TESTFILE_TYPEREADER1");
        om.registerModule(new JavaTimeModule());
        o = om.readValue("{\"url\":\"https://data.scrc.uk/api/file_type/5/?format=json\",\"last_updated\":\"2021-03-04T14:43:57.160401Z\",\"name\":\"YAML Ain't Markup Language\",\"extension\":\"yaml\",\"updated_by\":\"https://data.scrc.uk/api/users/13/?format=json\"}", File_type.class);
        Assertions.assertEquals(o.getUrl(), "https://data.scrc.uk/api/file_type/5/?format=json");
        Assertions.assertEquals(o.getLast_updated(), LocalDateTime.of(2021, 3, 4, 14, 43, 57, 160401000));
        Assertions.assertEquals(o.getUpdated_by(), "https://data.scrc.uk/api/users/13/?format=json");
        Assertions.assertEquals(o.getName(), "YAML Ain't Markup Language");
        Assertions.assertEquals(o.getExtension(), "yaml");
    }

    @Test
    @Order(2)
    void testFile_typeWriter() throws Exception {
        System.out.println("TESTFILE_TYPEWRITER");
        File_type o = new File_type();
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        System.out.println(om.writeValueAsString(o));
        o.setName("name");
        System.out.println(om.writeValueAsString(o));
        o.setUpdated_by("update by somone");
        System.out.println(om.writeValueAsString(o));
        o.setUrl("the url");
        System.out.println(om.writeValueAsString(o));
        o.setLast_updated(LocalDateTime.of(2021, 3, 2, 12, 0, 1, 1234567));
        System.out.println(om.writeValueAsString(o));
    }

    @Test
    @Order(3)
    void testFile_typeToLocalReg() throws Exception {
        System.out.println("TESTFILE_TYPETOLOCALREG");
        Response r = rl.post(new File_type("A test file type", "tst"));
        Assertions.assertEquals(201, r.getStatus());
    }

    @Test
    @Order(4)
    public void testObjectSearch() throws Exception{
        Map<String, String> m = new HashMap<String, String>();
        System.out.println("TESTOBJECTSEARCH");
        m.put("name", "A test file type");
        File_type o = (File_type) rl.getFirst(File_type.class, m);
        Assertions.assertEquals("tst", o.getExtension());
    }
}
