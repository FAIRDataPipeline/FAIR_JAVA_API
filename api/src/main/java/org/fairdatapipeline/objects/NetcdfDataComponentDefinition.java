package org.fairdatapipeline.objects;

import java.util.Map;
import javax.annotation.Nonnull;

public abstract class NetcdfDataComponentDefinition {
  final @Nonnull String description;
  final @Nonnull String long_name;
  final @Nonnull Map<String, String[]> optional_attribs;

  NetcdfDataComponentDefinition(
      @Nonnull String description,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    this.description = description;
    this.long_name = long_name;
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
