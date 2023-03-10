package org.fairdatapipeline.objects;

import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.NetcdfName;

/**
 * LocalVariableDefinition is a variable without a group - it is used as part of a Table: the group
 * is defined on the table, and the variables within the table are always sharing the tables group.
 */
public class LocalVariableDefinition extends VariableDefinition {
  NetcdfName localName;
  @Nullable Integer columnIndex;

  public LocalVariableDefinition(
      @Nonnull NetcdfName localName,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs,
      Object missingValue,
      @Nullable Integer columnIndex) {
    super(dataType, description, units, long_name, optional_attribs, missingValue);
    this.columnIndex = columnIndex;
    this.localName = localName;
  }

  public LocalVariableDefinition(
      @Nonnull NetcdfName localName,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs,
      Object missingValue) {
    super(dataType, description, units, long_name, optional_attribs, missingValue);
    this.localName = localName;
  }

  public LocalVariableDefinition(
      @Nonnull NetcdfName localName,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name,
      @Nonnull Map<String, String[]> optional_attribs) {
    super(dataType, description, units, long_name, optional_attribs);
    this.localName = localName;
  }

  public LocalVariableDefinition(
      @Nonnull NetcdfName localName,
      @Nonnull NetcdfDataType dataType,
      @Nonnull String description,
      @Nonnull String units,
      @Nonnull String long_name) {
    super(dataType, description, units, long_name, Collections.emptyMap());
    this.localName = localName;
  }

  public @Nonnull NetcdfName getLocalName() {
    return localName;
  }

  public @Nullable Integer getColumnIndex() {
    return columnIndex;
  }
}
