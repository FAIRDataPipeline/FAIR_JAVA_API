package org.fairdatapipeline.objects;

import org.fairdatapipeline.api.IllegalActionException;
import org.fairdatapipeline.netcdf.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class NumericalArrayDefinition {
    private final String name;
    private final NetcdfDataType dataType;
    private final String description;
    private final String units;
    private final DimensionDefinition[] dimensions;

    /**
     *
     * @param description this gets stored in the ':short' attribute
     * @param units
     * @param dimensions - a list of dimensions
     */
    public NumericalArrayDefinition(@Nonnull String name, @Nonnull NetcdfDataType dataType, @Nonnull String description, @Nonnull String units, @Nonnull DimensionDefinition[] dimensions) {
        // check that:
        // dimension_names are unique
        // not more than 1 unlimited dimensions (we can't check external dimensions)

        int num_dims = dimensions.length;
        ArrayList<String> dimension_names = new ArrayList<>();
        int unlimited_dimensions = 0;

        for(int i=0;i< num_dims;i++){
            if(dimensions[i].getClass() == DimensionDefinitionLocal.class){
                DimensionDefinitionLocal d = (DimensionDefinitionLocal)  dimensions[i];
                if(dimension_names.contains(d.getName())) throw(new IllegalActionException("dimension names must be unique; duplicate: " + d.getName()));
                if(d.isUnlimited()) unlimited_dimensions += 1;
                if(unlimited_dimensions > 1) throw(new IllegalArgumentException("you can only have 1 unlimited dimension."));
                dimension_names.add(d.getName());
            }
        }
        this.name = name;
        this.dataType = dataType;
        this.description = description;
        this.units = units;
        this.dimensions = dimensions;
    }

    public String getName() { return name; }
    public NetcdfDataType getDataType() { return dataType; }
    public String getDescription() { return description;}
    public DimensionDefinition[] getDimensions() { return this.dimensions.clone();}
    public String getUnits() { return this.units; }
}
