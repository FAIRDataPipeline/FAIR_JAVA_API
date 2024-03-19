package org.fairdatapipeline.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringListTest {
  @Test
  void makeStringList() {
    var stringlist = ImmutableStringList.builder().addStrings("bla", "ble").build();
    assertThat(stringlist.strings()).contains("bla", "ble");
  }
}
