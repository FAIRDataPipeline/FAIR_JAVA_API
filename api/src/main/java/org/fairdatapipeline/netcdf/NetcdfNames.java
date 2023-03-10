package org.fairdatapipeline.netcdf;

public class NetcdfNames {
  public static final String[] attrib_names =
      new String[] {"description", "units", "long_name", "standard_name"};

  public static String FDP_PREFIX = "__fdp_";
  public static final String ATTRIB_DESC = attrib_names[0];
  public static final String ATTRIB_UNITS = attrib_names[1];
  public static final String ATTRIB_LNAME = attrib_names[2];
  public static final String ATTRIB_FILLVALUE = "_FillValue";
  public static final String ATTRIB_GROUP_TYPE = FDP_PREFIX + "group_type";
  public static final String ATTRIB_GROUP_TYPE_TABLE = "table";
  public static final String ATTRIB_COLUMN_INDEX = FDP_PREFIX + "column_index";

  public static String generatedDimName(String vName, int i) {
    return FDP_PREFIX + vName + "_dim_" + i;
  }
}
