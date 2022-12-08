package org.fairdatapipeline.netcdf;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class NetcdfName {
  @Nonnull String name;
  // public static Pattern  THIS PATTERN IS SIMPLIFIED TO MATCH THE VERY RESTRICTED
  // FORMAT OF Datapipeline object_component
  // this was based on the definition of 'bare key' from TOML.
  public static final Pattern NAME_P = Pattern.compile("^\\p{Alnum}[\\p{Alnum}_-]*+$");

  public NetcdfName(@Nonnull String name) {
    if (!NAME_P.matcher(name).find())
      throw (new IllegalArgumentException("not a valid netCDF name: " + name));
    this.name = name;
  }

  public @Nonnull String getName() {
    return name;
  }

  public @Nonnull String toString() {
    return getName();
  }

  @Override
  public boolean equals(Object other) {
    return (other != null
        && other.getClass() == getClass()
        && ((NetcdfName) other).getName().equals(this.name));
  }
}
