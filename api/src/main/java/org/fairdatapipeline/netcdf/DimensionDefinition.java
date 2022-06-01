package org.fairdatapipeline.netcdf;

public class DimensionDefinition {
    String name;
    NetcdfDataType dataType;
    String description;
    int size;
    String units;
    public static int UNLIMITED = 0;
    Object values;

    public DimensionDefinition(String name, NetcdfDataType dataType, String description, int size, String units) {
        this(name, dataType, description, size, units, null);
    }
    public DimensionDefinition(String name, NetcdfDataType dataType, String description, int size, String units, Object values) {
        this.name = name;
        this.dataType = dataType;
        this.description = description;
        this.size = size;
        this.units = units;
        this.values = values;
    }

    public boolean isUnlimited() {
        return this.size == UNLIMITED;
    }

    public String getName() {
        return name;
    }

    public NetcdfDataType getDataType() {
        return dataType;
    }

    public String getDescription() {
        return description;
    }

    public int getSize() {
        return size;
    }

    public String getUnits() {
        return units;
    }
    public Object getValues() {
        return values;
    }
}
