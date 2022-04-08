package org.fairdatapipeline.toml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.StringReader;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.estimate.ImmutableEstimate;
import org.fairdatapipeline.parameters.Components;
import org.fairdatapipeline.parameters.ImmutableComponents;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TomlReaderPairwiseIntegrationTest {
  private final String toml =
      "[example-estimate]\n" + "type = \"point-estimate\"\n" + "value = 1.0";

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
}
