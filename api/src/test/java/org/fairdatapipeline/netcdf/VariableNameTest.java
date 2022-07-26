package org.fairdatapipeline.netcdf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VariableNameTest {
  /*
    @Test
    void testNetCdfNamePRE363() {
        // FALSE:
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("_bla").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher(".bla").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("/bla").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("bla/").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("bla\na").find());

        // TRUE:
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("b.@la").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("1bla").find());
        System.out.println(VariableName.netcdfname_pre_netCDF363.matcher("090").find());
    }

    @Test
    void testNetCdfName() {
        // FALSE:
        System.out.println(VariableName.netcdfname.matcher("_bla").find());
        System.out.println(VariableName.netcdfname.matcher(".bla").find());
        System.out.println(VariableName.netcdfname.matcher("/bla").find());
        System.out.println(VariableName.netcdfname.matcher("").find());
        System.out.println(VariableName.netcdfname.matcher("bla/").find());
        System.out.println(VariableName.netcdfname.matcher("*bla/").find());
        System.out.println(VariableName.netcdfname.matcher("bla\na").find());

        // TRUE:
        System.out.println(VariableName.netcdfname.matcher("b.@la").find());
        System.out.println(VariableName.netcdfname.matcher("1bla").find());
        System.out.println(VariableName.netcdfname.matcher("090").find());
        System.out.println(VariableName.netcdfname.matcher("b.@*&%$£la").find());
        System.out.println(VariableName.netcdfname.matcher("1\"bla").find());
        System.out.println(VariableName.netcdfname.matcher("0;:~&%€").find());

    }

    @Test
    void testNetCdfFullpath() {
        // FALSE:
        System.out.println(VariableName.netcdffullpath.matcher("_bla").find());
        System.out.println(VariableName.netcdffullpath.matcher(".bla").find());
        System.out.println(VariableName.netcdffullpath.matcher("/bla").find());
        System.out.println(VariableName.netcdffullpath.matcher("").find());
        System.out.println(VariableName.netcdffullpath.matcher("bla/").find());
        System.out.println(VariableName.netcdffullpath.matcher("*bla/").find());
        System.out.println(VariableName.netcdffullpath.matcher("bla\na").find());
        System.out.println(VariableName.netcdffullpath.matcher("1b//la").find());

        // TRUE:
        System.out.println(VariableName.netcdffullpath.matcher("b.@la").find());
        System.out.println(VariableName.netcdffullpath.matcher("1bla").find());
        System.out.println(VariableName.netcdffullpath.matcher("090").find());
        System.out.println(VariableName.netcdffullpath.matcher("b.@*&%$£la").find());
        System.out.println(VariableName.netcdffullpath.matcher("1\"bla").find());
        System.out.println(VariableName.netcdffullpath.matcher("0;:~&%€").find());
        System.out.println(VariableName.netcdffullpath.matcher("1bla/bla").find());
        System.out.println(VariableName.netcdffullpath.matcher("1bla/1bla/2bla").find());
    }
  */

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
        "1_*&/1_)(*&)",
        "bla/bla/bla",
        "/bla/bla",
        "bla192*&^"
      })
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_good_full_paths(String fullpath) {
    Assertions.assertNotNull(new VariableName(fullpath));
  }

  @ParameterizedTest
  @ValueSource(strings = {"bla192*&^", "bla"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_good_names(String name) {
    Assertions.assertNotNull(new VariableName(name, "bla"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "/bla"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_bad_names(String name) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          new VariableName(name, "bla");
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "bla", "/bla", "/bla/bla"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_good_groups(String group) {
    Assertions.assertNotNull(new VariableName("bla", group));
  }

  @ParameterizedTest
  @ValueSource(strings = {"bla/", "/bla/"})
  /** "" is not a valid fullPath. a valid full path contains at least a variable name. */
  void test_bad_groups(String group) {
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> {
          new VariableName("bla", group);
        });
  }

  @Test
  /** /bla/bla gets rid of the starting slash; group and name are both bla. */
  void test_a_specific_path1() {
    VariableName vn = new VariableName("/bla/bla");
    Assertions.assertEquals("bla", vn.getName());
    Assertions.assertEquals("bla", vn.getGroupName());
    Assertions.assertEquals("bla/bla", vn.getFullPath());
  }

  @Test
  /** /bla gets rid of the starting slash; group and name are both bla. */
  void test_a_specific_path2() {
    VariableName vn = new VariableName("/bla");
    Assertions.assertEquals("bla", vn.getName());
    Assertions.assertEquals("", vn.getGroupName());
    Assertions.assertEquals("bla", vn.getFullPath());
  }
}
