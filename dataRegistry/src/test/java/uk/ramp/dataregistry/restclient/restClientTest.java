
package uk.ramp.dataregistry.restclient;


import org.junit.jupiter.api.*;
import uk.ramp.dataregistry.content.File_type;
import uk.ramp.dataregistry.content.Namespace;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class restClientTest {
    String localReg = "http://localhost:8000/api/";
    String remoteReg = "https://data.scrc.uk/api/";
    RestClient lc, lcr;

    @BeforeAll
    public void setUp() throws Exception {
        lc = new RestClient(localReg);
        lcr = new RestClient(remoteReg);
    }



    @Test
    public void get_namespace() {
        Namespace n = (Namespace) lc.get(Namespace.class, 1);
        Assertions.assertEquals("BramTestNS", n.getName());
    }

    @Disabled
    @Test
    public void create_file_type() {
        lc.post(new File_type("Test filetype", ".tst"));
    }

    @Disabled
    @Test
    public void get_wrongClass()  {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {lc.get(Object.class, 1);});
    }

    @Disabled
    @Test
    public void get_file_type() {
        File_type f = (File_type) lc.get(File_type.class, 2);
        Assertions.assertEquals( "Test filetype", f.getName());
    }



}
