package org.fairdatapipeline.objects;

import javax.annotation.Nullable;
import org.fairdatapipeline.netcdf.NetcdfName;

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
    this.size = size;
    is_size = true;
  }

  public boolean is_size() {
    return is_size;
  }

  public Integer size() {
    return size;
  }

  public NetcdfName name() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof Dimension)) return false;
    Dimension d = (Dimension) o;
    return ((this.is_size() && d.is_size() && d.size == this.size)
        || (!this.is_size() && !d.is_size() && d.name().getName().equals(this.name.getName())));
  }
}
