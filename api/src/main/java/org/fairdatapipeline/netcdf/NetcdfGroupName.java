package org.fairdatapipeline.netcdf;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

public class NetcdfGroupName {
  @Nonnull String groupName;
  // public static Pattern netcdffullpath_pre_netCDF363 =
  // Pattern.compile("^\\p{Alnum}[\\p{Alnum}_@.+-]*(/\\p{Alnum}[\\p{Alnum}_@.+-])*$");
  // private static Pattern netcdffullpath =
  //        Pattern.compile("^\\p{Alnum}[^/\n]*+(?:/\\p{Alnum}[^/\n]*+)*+$");
  // public static Pattern  THIS PATTERN IS SIMPLIFIED TO MATCH THE VERY RESTRICTED
  // FORMAT OF Datapipeline object_component
  // this was based on the definition of 'bare key' from TOML.
  public static final Pattern groupnameP =
      Pattern.compile("^(?:\\p{Alnum}[\\p{Alnum}_-]*+(?:/\\p{Alnum}[\\p{Alnum}_-]*+)*+)?$");

  public NetcdfGroupName(@Nonnull String groupName) {
    if (groupName.startsWith("/")) groupName = groupName.substring(1);
    if (!groupnameP.matcher(groupName).find())
      throw (new IllegalArgumentException("not a valid netCDF group name: " + groupName));
    this.groupName = groupName;
  }

  public @Nonnull String getGroupName() {
    return groupName;
  }

  public @Nonnull String toString() {
    return getGroupName();
  }

  @Override
  public boolean equals(Object other) {
    return (other != null
        && other.getClass() == getClass()
        && ((NetcdfGroupName) other).getGroupName().equals(this.groupName));
  }
}
