package uk.ramp.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FileApiIntegrationTest {
  private String configPath;
  private String parentPath;
  private String dataDirectoryPath;

  @Before
  public void setUp() throws IOException, URISyntaxException {
    configPath = Paths.get(getClass().getResource("/config.yaml").toURI()).toString();
    parentPath = Path.of(configPath).getParent().toString();
    dataDirectoryPath = Path.of(parentPath, "folder/data").toString();
    Files.deleteIfExists(Path.of(dataDirectoryPath, "exampleWrite.toml"));
    Files.deleteIfExists(Path.of("access-runId.yaml"));
  }

  @Test
  public void testClose() throws IOException {
    FileApi api = new FileApi(Path.of(configPath));
    api.close();
    assertThat(Files.readString(Path.of(parentPath, "access-runId.yaml")))
        .contains("open_timestamp")
        .contains("close_timestamp")
        .contains("run_id")
        .contains("io");
  }
}
