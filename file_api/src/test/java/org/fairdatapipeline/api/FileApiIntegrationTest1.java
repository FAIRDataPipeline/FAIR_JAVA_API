package org.fairdatapipeline.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.*;

public class FileApiIntegrationTest1 {
  private Path ori_configPath, configPath, ori_scriptPath, scriptPath;
  private String datastore = "D:\\datastore";
  private String coderun = "\\coderun\\20210806T123456";
  private FileApi api;

  @BeforeAll
  public void setUp() throws IOException, URISyntaxException {
    ori_configPath = Paths.get(getClass().getResource("/config.yaml").toURI());
    ori_scriptPath = Paths.get(getClass().getResource("/script.sh").toURI());
    String folder = datastore + coderun;
    configPath = Paths.get(folder + "\\config.yaml");
    scriptPath = Paths.get(folder + "\\script.sh");
    Files.deleteIfExists(configPath);
    Files.deleteIfExists(scriptPath);
    if (!Paths.get(folder).toFile().exists()) Files.createDirectories(Paths.get(folder));
    Files.copy(ori_configPath, configPath);
    Files.copy(ori_scriptPath, scriptPath);
  }


  // @Test
  /*
   * this just writes 'bla die bla' to a new data product specified in the config.
   * it creates the DP, StoLo, Obj, and the 'whole_object' ObjComponent gets created
   * automatically by the registry. StoLo will have the hash.
   * Not sure what checks to run on this test. probably should test that the output
   * file has been created, and that all the objects have been created in the registry.
   */
  /*  public void testWrite1() {
      try (FileApi api = new FileApi(configPath, scriptPath)) {
        CleanableFileChannel f = api.openForWrite("human/example");
        String a = "bla die bla";
        ByteBuffer bb = ByteBuffer.wrap(a.getBytes(StandardCharsets.UTF_8));
        f.write(bb);
      } catch (Exception e) {
        System.out.println("Exception");
        System.out.println(e);
      }
    }
  */
  // @Test
  /*
   * this does the same as testWrite1; writing 'bla die bla' to DP 'human/example' but
   * it only gets the path from api, instead of retrieving a fileChannel. this changes the
   * way that the closing/hashing works.
   * Not sure what checks to run on this test. probably should test that the output
   * file has been created, and that all the objects have been created in the registry.
   */
  /*  public void testWrite2() throws IOException {
      try (FileApi api = new FileApi(configPath, scriptPath)) {
        Path p = api.getFilePathForWrite("human/example");
        BufferedWriter w;
        String a = "bla die bla";
        w = Files.newBufferedWriter(p);
        w.write(a, 0, a.length());
        w.close();
      } catch (Exception e) {
        System.out.println("Exception");
        System.out.println(e);
      }
    }
  */
  // @Test
  /*  public void testRead1() {
      try (FileApi api = new FileApi(configPath, scriptPath)) {
        System.out.println("start");
        CleanableFileChannel f = api.openForRead("human/example");
        if (f == null) {
          throw (new IllegalArgumentException("failed to open the DataProduct for read"));
        }
        String a = "bla die bla";
        System.out.println("size: " + f.size());
        ByteBuffer bb = ByteBuffer.allocate((int) f.size());
        f.read(bb);
        System.out.println("read into bb");
        String result = new String(bb.array());
        System.out.println("made string result: " + result);
        Assert.assertEquals(a, result);
      } catch (Exception e) {
        System.out.println("Exception");
        System.out.println(e);
      }
    }
  */
  // @Test
  /*  public void testRead2() {
    try (FileApi api = new FileApi(configPath, scriptPath)) {
      System.out.println("start");
      Path p = api.getFilePathForRead("human/example");
      if (p == null) {
        throw (new IllegalArgumentException("failed to get the dataProduct Path for read"));
      }
      FileChannel f = FileChannel.open(p, READ);
      String a = "bla die bla";
      System.out.println("size: " + f.size());
      ByteBuffer bb = ByteBuffer.allocate((int) f.size());
      f.read(bb);
      System.out.println("read into bb");
      String result = new String(bb.array());
      System.out.println("made string result: " + result);
      Assert.assertEquals(a, result);
    } catch (Exception e) {
      System.out.println("Exception");
      System.out.println(e);
    }
  }*/
}
