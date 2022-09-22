package org.fairdatapipeline.netcdf;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * VariableName stores the object_component (e.g. 'human/mortality_data') as a name
 * ('mortality_data') and a group ('human')
 */
public class VariableName implements DimensionName {
  @Nonnull NetcdfName name;
  @Nonnull NetcdfGroupName groupName;
  private static Pattern netcdffullpath =
      Pattern.compile("^\\p{Alnum}[\\p{Alnum}_-]*+(?:/\\p{Alnum}[\\p{Alnum}_-]*+)*+$");

  /**
   * create the VariableName from a full path with variable name (e.g. 'human/mortality_data') will
   * be stored as a name ('mortality_data') and a group ('human') if the full path starts with a '/'
   * we remove the slash.
   *
   * @param fullPath
   */
  public VariableName(@Nonnull String fullPath) {
    if (fullPath.startsWith("/")) fullPath = fullPath.substring(1);
    if (!netcdffullpath.matcher(fullPath).find())
      throw (new IllegalArgumentException("not a valid netCDF full path: " + fullPath));
    int last_slash = fullPath.lastIndexOf("/");
    if (last_slash == -1) {
      this.name = new NetcdfName(fullPath);
      this.groupName = new NetcdfGroupName("");
    } else {
      this.name = new NetcdfName(fullPath.substring(last_slash + 1));
      this.groupName = new NetcdfGroupName(fullPath.substring(0, last_slash));
    }
  }

  public VariableName(@Nonnull String name, @Nonnull String groupName) {
    this(new NetcdfName(name), new NetcdfGroupName(groupName));
  }

  public VariableName(@Nonnull NetcdfName name, @Nonnull NetcdfGroupName groupName) {
    this.name = name;
    this.groupName = groupName;
  }


  public @Nonnull NetcdfGroupName getGroupName() {
    return groupName;
  }

  public @Nonnull NetcdfName getName() {
    return name;
  }

  public @Nonnull String getFullPath() {
    if (groupName.toString().length() == 0) return name.toString();
    return groupName.toString() + '/' + name.toString();
  }

  public @Nonnull String toString() {
    return getFullPath();
  }

  @Override
  public boolean equals(Object other) {
    return (other != null
        && other.getClass() == getClass()
        && ((VariableName) other).getGroupName().equals(this.groupName)
        && ((VariableName) other).getName().equals(this.name));
  }
}
