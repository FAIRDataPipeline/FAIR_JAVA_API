package org.fairdatapipeline.toml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.distribution.Distribution.DistributionType;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.estimate.ImmutableEstimate;
import org.fairdatapipeline.parameters.Components;
import org.fairdatapipeline.parameters.ImmutableComponents;
import org.fairdatapipeline.parameters.ImmutableStringList;
import org.fairdatapipeline.samples.ImmutableSamples;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TomlWriterPairwiseIntegrationTest {

  private final String expectedToml =
      "[example-estimate]\n"
          + "value = 1.0\n"
          + "type = \"point-estimate\"\n"
          + "[example-distribution]\n"
          + "distribution = \"gamma\"\n"
          + "shape = 1.0\n"
          + "scale = 2.0\n"
          + "type = \"distribution\"\n"
          + "[example-samples]\n"
          + "samples = [1, 2, 3]\n"
          + "type = \"samples\"\n"
          + "[example-strings]\n"
          + "strings = [\"bla\", \"blo\"]\n"
          + "type = \"strings\"\n";

  private TOMLMapper tomlMapper;
  private RandomGenerator rng;

  @BeforeAll
  public void setUp() {
    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0D);
    tomlMapper = new TOMLMapper(rng);
  }

  @Test
  void write() {
    var estimate = ImmutableEstimate.builder().internalValue(1.0).rng(rng).build();
    var distribution =
        ImmutableDistribution.builder()
            .internalType(DistributionType.gamma)
            .internalShape(1)
            .internalScale(2)
            .rng(rng)
            .build();
    var samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    var strings = ImmutableStringList.builder().addStrings("bla", "blo").build();

    Components components =
        ImmutableComponents.builder()
            .putComponents("example-estimate", estimate)
            .putComponents("example-distribution", distribution)
            .putComponents("example-samples", samples)
            .putComponents("example-strings", strings)
            .build();

    var writer = new StringWriter();
    var tomlWriter = new TomlWriter(tomlMapper);

    tomlWriter.write(writer, components);

    var actualToml = writer.toString();
    assertThat(actualToml).isEqualTo(expectedToml);
  }
}
