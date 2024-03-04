package org.fairdatapipeline.toml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.StringReader;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.estimate.ImmutableEstimate;
import org.fairdatapipeline.parameters.Components;
import org.fairdatapipeline.parameters.ImmutableComponents;
import org.fairdatapipeline.parameters.ImmutableStringList;
import org.fairdatapipeline.samples.ImmutableSamples;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TomlReaderPairwiseIntegrationTest {
  private final String toml =
      "[example-estimate]\n" + "type = \"point-estimate\"\n" + "value = 1.0";

  private final String toml2 =
      "[example-estimate]\n"
          + "value = 1.0\n"
          + "type = \"point-estimate\"\n"
          + "[example-distribution]\n"
          + "distribution = \"gamma\"\n"
          + "shape = 1.0\n"
          + "scale = 2.0\n"
          + "type = \"distribution\"\n"
          + "[example-samples]\n"
          + "samples = [1.5, 2.0, 3.0]\n"
          + "type = \"samples\"\n"
          + "[example-strings]\n"
          + "strings = [\"bla\", \"blo\"]\n"
          + "type = \"strings\"\n";

  private RandomGenerator rng;

  @BeforeAll
  public void setUp() {
    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0D);
  }

  @Test
  void read() {
    TomlReader tomlReader = new TomlReader(new TOMLMapper(rng));
    var reader = new StringReader(toml);
    var estimate = ImmutableEstimate.builder().internalValue(1.0).rng(rng).build();
    Components components =
        ImmutableComponents.builder().putComponents("example-estimate", estimate).build();
    Components components_read = tomlReader.read(reader, new TypeReference<>() {});

    assertThat(components_read).isEqualTo(components);
  }

  @Test
  void read2() {
    TomlReader tomlReader = new TomlReader(new TOMLMapper(rng));
    var reader = new StringReader(toml2);
    var estimate = ImmutableEstimate.builder().internalValue(1.0).rng(rng).build();
    Components components =
        ImmutableComponents.builder()
            .putComponents("example-estimate", estimate)
            .putComponents(
                "example-distribution",
                ImmutableDistribution.builder()
                    .internalShape(1)
                    .internalScale(2)
                    .internalType(Distribution.DistributionType.gamma)
                    .rng(rng)
                    .build())
            .putComponents(
                "example-samples",
                ImmutableSamples.builder().addSamples(1.5, 2, 3).rng(rng).build())
            .putComponents(
                "example-strings", ImmutableStringList.builder().addStrings("bla", "blo").build())
            .build();
    Components components_read = tomlReader.read(reader, new TypeReference<>() {});

    assertThat(components_read.components()).containsAllEntriesOf(components.components());
  }
}
