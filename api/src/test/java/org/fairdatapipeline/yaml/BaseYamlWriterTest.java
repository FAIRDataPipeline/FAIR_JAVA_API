package org.fairdatapipeline.yaml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseYamlWriterTest {
  private Writer underlyingWriter;

  @BeforeAll
  public void setUp() throws Exception {
    underlyingWriter = new StringWriter();
  }

  @Test
  void write() {
    var writer = new BaseYamlWriter();
    var data = "test";
    writer.write(underlyingWriter, data);

    assertThat(underlyingWriter).hasToString("\"test\"\n");
  }
}
