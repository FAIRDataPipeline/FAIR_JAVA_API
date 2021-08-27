package org.fairdatapipeline.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.*;

public class BaseYamlWriterTest {
  private Writer underlyingWriter;

  @BeforeAll
  public void setUp() throws Exception {
    underlyingWriter = new StringWriter();
  }

  @Test
  public void write() {
    var writer = new BaseYamlWriter();
    var data = "test";
    writer.write(underlyingWriter, data);

    assertThat(underlyingWriter.toString()).isEqualTo("\"test\"\n");
  }
}
