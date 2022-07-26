package org.fairdatapipeline.netcdf;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * VariableName stores the object_component (e.g. 'human/mortality_data') as a name
 * ('mortality_data') and a group ('human')
 */
public class VariableName {
  @Nonnull String name;
  @Nonnull String groupName;
  // public static Pattern netcdfname_pre_netCDF363 =
  // Pattern.compile("^\\p{Alnum}[\\p{Alnum}_@.+-]*$");
  // TODO: prevent trailing spaces in these regex?
  private static Pattern netcdfname = Pattern.compile("^\\p{Alnum}[^/\n]*$");
  // public static Pattern netcdffullpath_pre_netCDF363 =
  // Pattern.compile("^\\p{Alnum}[\\p{Alnum}_@.+-]*(/\\p{Alnum}[\\p{Alnum}_@.+-])*$");
  private static Pattern netcdffullpath =
      Pattern.compile("^\\p{Alnum}[^/\n]*+(?:/\\p{Alnum}[^/\n]*+)*+$");

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
      this.name = fullPath;
      this.groupName = "";
    } else {
      this.name = fullPath.substring(last_slash + 1);
      this.groupName = fullPath.substring(0, last_slash);
    }
  }

  public VariableName(@Nonnull String name, @Nonnull String groupName) {
    if (groupName.startsWith("/")) groupName = groupName.substring(1);
    if (groupName.length() > 0 && !netcdffullpath.matcher(groupName).find())
      throw (new IllegalArgumentException("not a valid netCDF path name: " + groupName));
    if (name.length() == 0)
      throw (new IllegalArgumentException("variable name can not be empty: " + name));
    if (!netcdfname.matcher(name).find())
      throw (new IllegalArgumentException("not a valid variable name: " + name));
    this.name = name;
    this.groupName = groupName;
  }

  public @Nonnull String getGroupName() {
    return groupName;
  }

  public String getName() {
    return name;
  }

  public @Nonnull String getFullPath() {
    if (groupName.length() == 0) return name;
    return groupName + '/' + name;
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
