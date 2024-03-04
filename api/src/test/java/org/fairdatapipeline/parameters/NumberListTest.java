package org.fairdatapipeline.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NumberListTest {

  /**
   *  test that a mix of ints and doubles will end up as all doubles. (necessary for TOML - it can't read a 'heterogenous array' of mixed int & double numbers)
   *
   */
  @Test
  void makeNumberList() {
    var numberlist = ImmutableNumberList.builder().addNumbers(1, 1.5, 12345.67).build();
    assertThat(numberlist.numbers()).contains(1.0, 1.5, 12345.67);
  }

  /**
   * given just ints, they should NOT be converted to doubles.
   */
  @Test
  void makeNumberList2() {
    var numberlist = ImmutableNumberList.builder().addNumbers(1, 2, 12).build();
    assertThat(numberlist.numbers()).contains(1, 2, 12);
  }
}
