package org.fairdatapipeline.hash;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.*;

class Sha1HasherTest {

  @Test
  void testSha1Hash() {
    var input = "test";

    // Generated via http://www.sha1-online.com/
    var expectedOutput = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3";

    assertThat(new Sha1Hasher().hash(input)).isEqualTo(expectedOutput);
  }
}
