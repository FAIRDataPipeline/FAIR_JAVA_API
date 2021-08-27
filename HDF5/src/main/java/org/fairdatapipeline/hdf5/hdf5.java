package org.fairdatapipeline.hdf5;

import hdf.object.*;
import hdf.object.h5.*;
import java.nio.file.Path;
import java.util.ArrayList;

public class hdf5 {
  H5File h5file;

  hdf5(Path filepath) {
    System.loadLibrary("hdf5");
    h5file = new H5File(filepath.toString()); // MODE?
  }

  public HObject read_ho(String component) {
    HObject ho = null;
    try {
      ho = h5file.get(component);
    } catch (Exception e) {
      throw (new IllegalArgumentException(
          "can't find component " + component + " in the HDF5 file." + e));
    }
    return ho;
  }

  @SuppressWarnings("unchecked")
  public ArrayList<Object> read_arraylist(String component) {
    HObject ho = read_ho(component);
    if (!(ho instanceof Dataset)) {
      throw (new IllegalArgumentException("component " + component + " is not a dataset"));
    }
    try {
      Object o = ((Dataset) ho).getData();
      if (o instanceof ArrayList) {
        return (ArrayList<Object>) o;
      } else {
        throw (new IllegalArgumentException("getData didn't give me an arraylist.."));
      }
    } catch (Exception e) {
      throw (new IllegalArgumentException("failed to get data from dataset " + e));
    }
  }

  @SuppressWarnings("unchecked")
  public Number[] read_array_1dn(String component) {

    HObject ho = read_ho(component);
    if (!(ho instanceof Dataset)) {
      throw (new IllegalArgumentException("component " + component + " is not a dataset"));
    }
    if (ho instanceof CompoundDS) {
      CompoundDS cds = (CompoundDS) ho;
      if (cds.getMemberCount() != 1) {
        throw (new IllegalArgumentException(
            "1D number array can't be read from CompoundDS with "
                + cds.getMemberCount()
                + " members."));
      }
      Datatype[] types = cds.getMemberTypes();
      if (types[1].getDatatypeClass() > 2) {
        // only 0, 1, 2 are numbers.
        throw (new IllegalArgumentException(
            "number array can't be read from data type " + types[1].getDescription()));
      }
      int[] orders = cds.getMemberOrders();
      if (orders[1] != 1) {
        throw (new IllegalArgumentException(
            "1D number array can't be read from CompoundDS with member with order != 1."));
      }
      ArrayList<Object> ao;
      try {
        Object o = cds.getData();
        if (o instanceof ArrayList) {
          ao = (ArrayList<Object>) o;
        } else {
          throw (new IllegalArgumentException("getData didn't give us an ArrayList"));
        }
      } catch (Exception e) {
        throw (new IllegalArgumentException("failed to get data from dataset " + e));
      }
      return (Number[]) ao.toArray();
    }
    if (ho instanceof ScalarDS) {
      ScalarDS sds = (ScalarDS) ho;
      if (sds.getDatatype().getDatatypeClass() > 2) {
        // only datatype class 0, 1, 2 are Number.
        throw (new IllegalArgumentException(
            "number array can't be read from data type " + sds.getDatatype().getDescription()));
      }
    }
    return null;
  }

  public void write(String component, Number[] arr) {}
}
