package org.fairdatapipeline.samples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SamplerTest {
  private Random random;

  @BeforeAll
  public void setUp() {
    this.random = mock(Random.class);
  }

  @Test
  void sample() {
    when(random.nextInt(3)).thenReturn(1);
    var sampler = new Sampler(random);
    assertThat(sampler.sample(List.of(1, 2, 3))).isEqualTo(2);
  }
}
