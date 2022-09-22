package org.fairdatapipeline.objects;

import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.fairdatapipeline.netcdf.NetcdfName;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class LocalVariableDefinition extends VariableDefinition{
    NetcdfName localName;

    public LocalVariableDefinition(
            @Nonnull NetcdfName localName,
            @Nonnull NetcdfDataType dataType,
            @Nonnull String description,
            @Nonnull String units,
            @Nonnull String long_name,
            @Nonnull Map<String, String> optional_attribs,
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
            @Nonnull Map<String, String> optional_attribs) {
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
}
