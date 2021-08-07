package uk.ramp.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ramp.file.CleanableFileChannel;


public class FileApiIntegrationTest {
  private Path ori_configPath, configPath, ori_scriptPath, scriptPath;
  private String datastore = "D:\\datastore";
  private String coderun = "\\coderun\\20210806T123456";
  private FileApi api;

  @Before
  public void setUp() throws IOException, URISyntaxException {
    ori_configPath = Paths.get(getClass().getResource("/config.yaml").toURI());
    ori_scriptPath = Paths.get(getClass().getResource("/script.sh").toURI());
    String folder = datastore + coderun;
    configPath = Paths.get(folder + "\\config.yaml");
    scriptPath = Paths.get(folder + "\\script.sh");
    Files.deleteIfExists(configPath);
    Files.deleteIfExists(scriptPath);
    if(!Paths.get(folder).toFile().exists())  Files.createDirectories(Paths.get(folder));
    Files.copy(ori_configPath, configPath);
    Files.copy(ori_scriptPath, scriptPath);
  }

  @Ignore
  @Test
  public void testClose() throws IOException {
    //api = new FileApi(Path.of(configPath));
    api.close();
    /*assertThat(Files.readString(Path.of(parentPath, "access-runId.yaml")))
        .contains("open_timestamp")
        .contains("close_timestamp")
        .contains("run_id")
        .contains("io");*/
  }

  @Test
  /*
   * this just writes 'bla die bla' to a new data product specified in the config.
   * it creates the DP, StoLo, Obj, and the 'whole_object' ObjComponent gets created
   * automatically by the registry. StoLo will have the hash.
   */
  public void testWrite() {
    try (FileApi api = new FileApi(configPath, scriptPath)){
      CleanableFileChannel f = api.openForWrite("human/example");
      String a = "bla die bla";
      ByteBuffer bb = ByteBuffer.wrap(a.getBytes(StandardCharsets.UTF_8));
      f.write(bb);
    }catch(Exception e) {
      System.out.println("Exception");
      System.out.println(e);
    }
  }
}
