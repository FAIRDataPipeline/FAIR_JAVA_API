package org.fairdatapipeline.samples;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SamplerTest {
  private Random random;

  @BeforeAll
  public void setUp() throws Exception {
    this.random = mock(Random.class);
  }

  @Test
  public void sample() {
    when(random.nextInt(eq(3))).thenReturn(1);
    var sampler = new Sampler(random);
    assertThat(sampler.sample(List.of(1, 2, 3))).isEqualTo(2);
  }
}
