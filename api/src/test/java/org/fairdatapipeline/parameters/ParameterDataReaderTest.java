package org.fairdatapipeline.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Reader;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.estimate.Estimate;
import org.fairdatapipeline.file.CleanableFileChannel;
import org.fairdatapipeline.samples.Samples;
import org.fairdatapipeline.toml.TomlReader;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ParameterDataReaderTest {
  private CleanableFileChannel fileChannel;
  private TomlReader tomlReader;
  private Estimate mockEstimate;

  @BeforeAll
  public void setUp() {
    this.fileChannel = mock(CleanableFileChannel.class);
    this.tomlReader = mock(TomlReader.class);
    this.mockEstimate = mock(Estimate.class);
  }

  @Test
  void read() {
    Components expectedComponents =
        ImmutableComponents.builder()
            .putComponents("example-estimate", mockEstimate)
            .putComponents("example-distribution", mock(Distribution.class))
            .putComponents("example-samples", mock(Samples.class))
            .build();

    when(tomlReader.read(any(Reader.class), any())).thenReturn(expectedComponents);
    var dataReader = new ParameterDataReaderImpl(tomlReader);

    assertThat(dataReader.read(fileChannel, "example-estimate")).isEqualTo(mockEstimate);
  }
}
