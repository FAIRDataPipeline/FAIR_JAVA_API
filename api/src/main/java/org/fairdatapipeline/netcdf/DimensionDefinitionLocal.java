package org.fairdatapipeline.netcdf;

import org.fairdatapipeline.api.IllegalActionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;

public class DimensionDefinitionLocal extends DimensionDefinition {
    NetcdfDataType dataType;
    String description;
    int size;
    String units;
    public static int UNLIMITED = 0;
    Object values;

    /** if we don't supply values, we need to set length and type. length can be DimensionDefinitionLocal.UNLIMITED
     *
     * @param name
     * @param description
     * @param size
     * @param units
     * @param dataType
     */

    public DimensionDefinitionLocal(@Nonnull String name, @Nonnull String description, int size, @Nonnull String units, @Nonnull NetcdfDataType dataType) {
        this(name, description, size, units, dataType, null);
    }

    /** if we give 'values', we don't need to give type and length as these can be taken from the 'values' array.
     *
     * @param name
     * @param description
     * @param units
     * @param values
     */
    public DimensionDefinitionLocal(@Nonnull String name, @Nonnull String description, @Nonnull String units, @Nonnull Object values) {
        this(name, description, Array.getLength(values), units, NetcdfDataType.translate_datatype(values), values);
    }


    private DimensionDefinitionLocal(@Nonnull String name, String description, int size, String units, @Nonnull NetcdfDataType dataType, @Nullable Object values) {
        if(name.startsWith("/")) throw(new IllegalActionException("name must not start with /"));
        this.name = name;
        this.dataType = dataType;
        this.description = description;
        this.size = size;
        this.units = units;
        this.values = values;
    }

    @Override
    boolean isLocal() {
        return true;
    }


    public boolean isUnlimited() {
        return this.size == UNLIMITED;
    }

    public @Nonnull String getName() {
        return name;
    }

    public @Nonnull NetcdfDataType getDataType() {
        return dataType;
    }

    public @Nonnull String getDescription() {
        return description;
    }

    public int getSize() {
        return size;
    }

    public @Nonnull String getUnits() {
        return units;
    }

    public @Nullable Object getValues() { return values; }
}
