package org.fairdatapipeline.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BoolListTest {
  @Test
  void makeBoolList() {
    var boollist = ImmutableBoolList.builder().addBools(true, false, true).build();
    assertThat(boollist.bools()).contains(true, false);
  }
}
