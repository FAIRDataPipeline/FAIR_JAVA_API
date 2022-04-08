package org.fairdatapipeline.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.distribution.Distribution.DistributionType;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.estimate.ImmutableEstimate;
import org.fairdatapipeline.mapper.DataPipelineMapper;
import org.fairdatapipeline.samples.ImmutableSamples;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComponentsDeserializerTest {
  private final String json =
      "{\n"
          + "  \"example-distribution\": {\n"
          + "    \"distribution\": \"gamma\",\n"
          + "    \"scale\": 2,\n"
          + "    \"shape\": 1,\n"
          + "    \"type\": \"distribution\"\n"
          + "  },\n"
          + "  \"example-estimate\": {\n"
          + "    \"type\": \"point-estimate\",\n"
          + "    \"value\": 1.0\n"
          + "  },\n"
          + "  \"example-samples\": {\n"
          + "    \"samples\": [\n"
          + "      1,\n"
          + "      2,\n"
          + "      3\n"
          + "    ],\n"
          + "    \"type\": \"samples\"\n"
          + "  }\n"
          + "}";

  private ObjectMapper objectMapper;
  private RandomGenerator rng;

  @BeforeAll
  public void setUp() throws Exception {
    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0D);
    objectMapper = new DataPipelineMapper(rng);
  }

  @Test
  void deserialize() throws JsonProcessingException {
    Components actualComponents = objectMapper.readValue(json, Components.class);

    var estimate = ImmutableEstimate.builder().internalValue(1.0).rng(rng).build();
    var distribution =
        ImmutableDistribution.builder()
            .internalType(DistributionType.gamma)
            .internalShape(1)
            .internalScale(2)
            .rng(rng)
            .build();
    var samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    var expectedComponents =
        ImmutableComponents.builder()
            .components(
                Map.of(
                    "example-estimate",
                    estimate,
                    "example-distribution",
                    distribution,
                    "example-samples",
                    samples))
            .build();

    assertThat(actualComponents).isEqualTo(expectedComponents);
  }
}
