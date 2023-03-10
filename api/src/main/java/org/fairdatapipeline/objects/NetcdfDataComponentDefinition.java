package org.fairdatapipeline.objects;

import java.util.Map;
import javax.annotation.Nonnull;
import org.fairdatapipeline.netcdf.NetcdfNames;

/**
 * basic DataComponentDef - the only fields shared between all types of variables/dimensions/tables
 * are the attributes: description, long_name, and optional_attribs.
 */
abstract class NetcdfDataComponentDefinition {
  final @Nonnull String description;
  final @Nonnull String long_name;
  final @Nonnull Map<String, String[]> optional_attribs;

  NetcdfDataComponentDefinition(
      @Nonnull String description,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    this.description = description;
    this.long_name = long_name;
    optional_attribs.keySet().stream()
        .forEach(
            k -> {
              if (k.startsWith(NetcdfNames.FDP_PREFIX))
                throw new IllegalArgumentException(
                    "attribute names are not allowed to start with prefix "
                        + NetcdfNames.FDP_PREFIX);
            });
    this.optional_attribs = optional_attribs;
  }

  public @Nonnull String getDescription() {
    return description;
  }

  public @Nonnull String getLong_name() {
    return long_name;
  }

  public @Nonnull Map<String, String[]> getOptional_attribs() {
    return optional_attribs;
  }
}
