package org.fairdatapipeline.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.distribution.Distribution.DistributionType;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.mapper.DataPipelineMapper;
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
          + "  \"example-strings\": {\n"
          + "    \"strings\": [\"bram\",\"rosalie\"],\n"
          + "    \"type\": \"strings\"\n"
          + "  }\n"
          + "}";

  private ObjectMapper objectMapper;
  private RandomGenerator rng;

  @BeforeAll
  public void setUp() {
    rng = mock(RandomGenerator.class);
    when(rng.nextDouble()).thenReturn(0D);
    objectMapper = new DataPipelineMapper(rng);
  }

  @Test
  void deserialize() throws JsonProcessingException {
    Components actualComponents = objectMapper.readValue(json, Components.class);

    var distribution =
        ImmutableDistribution.builder()
            .internalType(DistributionType.gamma)
            .internalShape(1)
            .internalScale(2)
            .rng(rng)
            .build();
    var strings = ImmutableStringList.builder().addStrings("bram", "rosalie").build();
    Components components =
        ImmutableComponents.builder()
            .putComponents("example-distribution", distribution)
            .putComponents("example-strings", strings)
            .build();
    /* var expectedComponents =
                    ImmutableComponents.builder()
                            .components(
                                    Map.of(
                                            "example-distribution",
                                            distribution
    ))
                            .build();*/

    assertThat(actualComponents).isEqualTo(components);
  }
}
