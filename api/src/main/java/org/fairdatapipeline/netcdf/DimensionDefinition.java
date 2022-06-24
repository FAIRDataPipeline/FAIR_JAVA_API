package org.fairdatapipeline.netcdf;

import javax.annotation.Nonnull;

public abstract class DimensionDefinition {
    @Nonnull String name;

    @Nonnull String getName() {
        return this.name;
    }

    abstract boolean isLocal();
}
