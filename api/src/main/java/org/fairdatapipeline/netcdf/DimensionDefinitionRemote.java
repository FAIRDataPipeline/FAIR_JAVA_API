package org.fairdatapipeline.netcdf;

import org.fairdatapipeline.api.IllegalActionException;

import javax.annotation.Nonnull;

public class DimensionDefinitionRemote extends DimensionDefinition{

    String groupName;

    public DimensionDefinitionRemote(@Nonnull String name, @Nonnull String groupName) {
        this.name = name;
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    boolean isLocal() {
        return false;
    }
}
