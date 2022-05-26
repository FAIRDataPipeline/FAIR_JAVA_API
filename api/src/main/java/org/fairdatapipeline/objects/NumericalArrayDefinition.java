package org.fairdatapipeline.objects;

import org.fairdatapipeline.netcdf.NetcdfBuilder;
import org.fairdatapipeline.netcdf.NetcdfDataType;
import org.jetbrains.annotations.NotNull;

public class NumericalArrayDefinition {
    private final String name;
    private final NetcdfDataType dataType;
    private final String description;
    private final String[] dimension_names;
    private final int[] dimension_sizes;
    private final Object[] dimension_values;
    private final String[] dimension_units;
    private final String units;
    static String[] DEFAULT_DIM_NAMES = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    /**
     *
     * @param description this gets stored in the ':short' attribute
     * @param dimension_names these get used to name the dimensions
     * @param dimension_sizes state the size of each dimension.
     * @param dimension_values these hold the values for each of the dimension-variables.
     * @param dimension_units these hold the units for each of the dimension-variables.
     * @param units
     */
    public NumericalArrayDefinition(String name, NetcdfDataType dataType, String description, String[] dimension_names, int @NotNull [] dimension_sizes, Object[] dimension_values, String[] dimension_units, String units) {
        // check that:
        // dimension_names (if given) should match dimension_sizes length, otherwise create default 'a', 'b', 'c'..
        // dimension_values (if given) should match dimension_sizes in length, and each array in dimension_values should have the correct length given dimension_sizes, and should have an acceptable datatype.
        // dimension_units (if given) should match dimension_sizes in length
        int num_dims = dimension_sizes.length;

        if(dimension_names == null) {
            this.dimension_names = new String[num_dims];
            for(int i=0; i<num_dims;i++) this.dimension_names[i] = this.DEFAULT_DIM_NAMES[i];
        }else{
            if(dimension_names.length != num_dims) throw(new IllegalArgumentException("all dimension_* arguments to create a NumbericalArrayDefinition must be the same length"));
            this.dimension_names = dimension_names;
        }
        if(dimension_values == null) {
            this.dimension_values = new Object[num_dims];
            for(int i=0; i<num_dims;i++) {
                this.dimension_values[i] = new int[dimension_sizes[i]];
                for(int j=0;j<dimension_sizes[i];j++) ((int[])this.dimension_values[i])[j] = j+1;
            }
        }else{
            if(dimension_names.length != num_dims) throw(new IllegalArgumentException("all dimension_* arguments to create a NumbericalArrayDefinition must be the same length"));
            for(int i=0;i<num_dims;i++) {
                // translate will throw if java class of dimension_values[i] is untranslatable to netcdf type.
                NetcdfBuilder.translate_datatype(dimension_values[i]);
            }
            this.dimension_values = dimension_values;
        }
        if (dimension_units != null) {
            if(dimension_units.length != num_dims) throw(new IllegalArgumentException("all dimension_* arguments to create a NumbericalArrayDefinition must be the same length"));
        }
        this.name = name;
        this.dataType = dataType;
        this.description = description;
        this.dimension_sizes = dimension_sizes;
        this.dimension_units = dimension_units;
        this.units = units;
    }



    public String getName() { return name; }
    public NetcdfDataType getDataType() { return dataType; }
    public String getDescription() { return description;}
    public String[] getDimension_names() { return this.dimension_names.clone();}
    public int[] getDimension_sizes() { return this.dimension_sizes; }
    public Object[] getDimension_values() { return this.dimension_values.clone();}
    public String[] getDimension_units() { return this.dimension_units.clone();}
    public String getUnits() { return this.units; }
}
