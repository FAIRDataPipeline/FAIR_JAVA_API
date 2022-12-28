package org.fairdatapipeline.objects;

import org.fairdatapipeline.netcdf.NetcdfName;
import javax.annotation.Nullable;

public class Dimension {
    @Nullable NetcdfName name;
    @Nullable Integer size;
    boolean is_size = false;

    public Dimension(NetcdfName name) {
        this.name = name;
    }

    public Dimension(String name) {
        this.name = new NetcdfName(name);
    }

    public Dimension(int size) {
        this.size= size;
        is_size = true;
    }

    public boolean is_size() {
        return is_size;
    }

    public Integer size() {
        return size;
    }

    public NetcdfName name(){
        return name;
    }
}
