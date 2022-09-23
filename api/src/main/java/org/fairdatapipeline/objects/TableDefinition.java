package org.fairdatapipeline.objects;

import org.fairdatapipeline.netcdf.NetcdfGroupName;
import org.fairdatapipeline.netcdf.VariableName;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * table is a bit of an odd one: it behaves like any other data component: it has a name (group & name),
 * description and other attributes. but instead of directly containing a variable (array with dimensions)
 * or a 'CoordinateVariable', it contains an array of VariableDefinitions.
 * These VariableDefinitions must all only have local names (no group). they will share a single
 * dimension for its length.
 */
public class TableDefinition extends NetcdfDataComponentDefinition {
    @Nonnull LocalVariableDefinition[] columns;
    @Nonnull NetcdfGroupName groupName;

    int size;

    public TableDefinition(
            @Nonnull NetcdfGroupName groupName,
            int size,
            @Nonnull String description,
            @Nonnull String long_name,
            @Nonnull Map<String, String[]> optional_attribs,
            @Nonnull LocalVariableDefinition[] columns) {
        super(description, long_name, optional_attribs);
        this.groupName = groupName;
        this.columns = columns;
        this.size = size;
    }

    public @Nonnull NetcdfGroupName getGroupName() {
        return groupName;
    }

    public boolean isUnlimited() {
        return this.size == CoordinateVariableDefinition.UNLIMITED;
    }

    public VariableName getVariableName(int i) {
        if(i >= this.columns.length) throw(new IllegalArgumentException("this table doesn't have that many columns " + i + " >= " + this.columns.length));
        return new VariableName(this.columns[i].getLocalName(), this.getGroupName());
    }

    public LocalVariableDefinition[] getColumns() {
        return this.columns;// TODO make a copy
    }

    public int getSize() { return this.size;}

}
