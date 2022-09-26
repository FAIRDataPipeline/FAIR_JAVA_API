package org.fairdatapipeline.netcdf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VariableNameTest {

  @ParameterizedTest
  @ValueSource(strings = {"", "/", " ", "_bla", "bla//bla", "bla/", "*bla", "bla\nbla"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_bad_full_paths(String fullpath) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          new VariableName(fullpath);
        });
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "bla/bla",
        "bla/191",
        "191/191",
        "bla/bla/bla",
        "/bla/bla",
      })
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_good_full_paths(String fullpath) {
    Assertions.assertDoesNotThrow(() -> new VariableName(fullpath));
  }

  @ParameterizedTest
  @ValueSource(strings = {"bla192", "bla", "123"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_good_names(String name) {
    Assertions.assertDoesNotThrow(() -> new NetcdfName(name));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "/bla"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_bad_names(String name) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          new NetcdfName(name);
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "bla", "/bla", "/bla/bla"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_good_groups(String group) {
    Assertions.assertDoesNotThrow(() -> new NetcdfGroupName(group));
  }

  @ParameterizedTest
  @ValueSource(strings = {"bla/", "/bla/"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_bad_groups(String group) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          new NetcdfGroupName(group);
        });
  }

  @Test
  /** /bla/bla gets rid of the starting slash; group and name are both bla. */
  void test_a_specific_path1() {
    VariableName vn = new VariableName("/bla/bla");
    Assertions.assertEquals("bla", vn.getName().toString());
    Assertions.assertEquals("bla", vn.getGroupName().toString());
    Assertions.assertEquals("bla/bla", vn.getFullPath());
  }

  @Test
  /** /bla gets rid of the starting slash; group and name are both bla. */
  void test_a_specific_path2() {
    VariableName vn = new VariableName("/bla");
    Assertions.assertEquals("bla", vn.getName().toString());
    Assertions.assertEquals("", vn.getGroupName().toString());
    Assertions.assertEquals("bla", vn.getFullPath());
  }
}
