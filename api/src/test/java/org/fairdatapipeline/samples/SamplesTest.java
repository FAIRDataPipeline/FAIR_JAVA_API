package org.fairdatapipeline.samples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.fairdatapipeline.distribution.Distribution.DistributionType.empirical;

import java.util.stream.IntStream;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well512a;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SamplesTest {
  private RandomGenerator rng;

  @BeforeAll
  public void setUp() {
    rng = new Well512a();
  }

  @Test
  void derivedEstimateFromSamples() {
    var samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    assertThat(samples.getEstimate().floatValue()).isCloseTo(2, offset(1e-7F));
  }

  @Test
  @Disabled("large numbers are currently unsupported.")
  void derivedEstimateLargeSamples() {
    var largeValue = 100_000_000_000_000_000L;
    var samples =
        ImmutableSamples.builder().addSamples(largeValue, largeValue + 1, largeValue + 2).build();
    assertThat(samples.getEstimate()).isEqualTo(largeValue + 1);
  }

  @Test
  void derivedSamplesFromSamples() {
    var samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    assertThat(samples.getSamples()).containsExactly(1, 2, 3);
  }

  @Test
  void heterogenousToDoubles() {
    var samples = ImmutableSamples.builder().addSamples(1.4, 2.5, 3).rng(rng).build();
    assertThat(samples.getSamples()).containsExactly(1.4, 2.5, 3.0);
  }

  @Test
  void derivedDistributionFromSamples() {
    var samples = ImmutableSamples.builder().addSamples(1, 2, 3).rng(rng).build();
    var distribution = samples.getDistribution();
    assertThat(distribution.internalType()).isEqualTo(empirical);
    assertThat(distribution.getEstimate().floatValue()).isCloseTo(2, offset(1e-7F));
    var distSampleAvg =
        IntStream.range(0, 10000)
            .parallel()
            .mapToDouble(i -> distribution.getSample().doubleValue())
            .average()
            .orElseThrow();
    assertThat(distSampleAvg).isBetween(1.95, 2.05);
  }
}
