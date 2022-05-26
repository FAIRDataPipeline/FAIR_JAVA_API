package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fairdatapipeline.objects.NumericalArrayDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFormatWriter;
import ucar.nc2.write.NetcdfFileFormat;

public class NetcdfBuilder implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(NetcdfBuilder.class);
  private static final Cleaner cleaner = Cleaner.create();
  private final Cleanable cleanable;
  private final NetcdfBuilderWrapper netcdfBuilderWrapper;

  public NetcdfBuilder(String filePath, Runnable onClose) {
    logger.trace("NetcdfBuilder({}) ", filePath);
    this.netcdfBuilderWrapper = new NetcdfBuilderWrapper(filePath, onClose);
    this.cleanable = cleaner.register(this, this.netcdfBuilderWrapper);
  }

  // Defining a resource that requires cleaning
  private static class NetcdfBuilderWrapper implements Runnable {
    private final NetcdfFormatWriter.Builder builder;
    private boolean has_been_built = false;

    private final Runnable runOnClose;

    NetcdfBuilderWrapper(String filePath, Runnable runOnClose) {
      Nc4Chunking chunker =
              Nc4ChunkingStrategy.factory(Nc4Chunking.Strategy.none, 0, false);

      this.builder = NetcdfFormatWriter.createNewNetcdf4(NetcdfFileFormat.NETCDF4, filePath, chunker);

      this.runOnClose = runOnClose;
    }

    public NetcdfFormatWriter build() throws Exception {
      try {
        NetcdfFormatWriter w = builder.build();
        has_been_built = true;
        return w;
      } catch(IOException e) {
        throw(new Exception("failed to create the netCDF file"));
      }
    }

    // Invoked by close method or cleaner
    @Override
    public void run() {
      logger.trace("run() invoked by cleaner");
      runOnClose.run();
      if(!has_been_built) {
        // write the file:
        try(NetcdfFormatWriter w = builder.build()) {
          // do nothing
        }catch(IOException e) {
          logger.error("failed to write netcdf file", e);
        }
      }else {
        // it has already been built. do nothing.
      }
    }
  }

  NetcdfFormatWriter build() throws Exception {
    return this.netcdfBuilderWrapper.build();
  }

  public void prepareArray(String group_name, NumericalArrayDefinition nadef) {
    List<Dimension> dims = new ArrayList<Dimension>();
    Group.Builder gb = getGroup(netcdfBuilderWrapper.builder.getRootGroup(), group_name);
    for(int i = 0; i < nadef.getDimension_values().length; i++) {
      Dimension d = new Dimension(nadef.getDimension_names()[i] + "_dim", nadef.getDimension_sizes()[i]);
      gb.addDimension(d);
      Variable.Builder this_dims_var = Variable.builder().setName(nadef.getDimension_names()[i]).setDataType(translate_datatype(nadef.getDimension_values()[i])).setDimensions(Collections.singletonList(d));
      if(nadef.getDimension_units() != null) {
        this_dims_var.addAttribute(new Attribute("units", nadef.getDimension_units()[i]));
      }
      gb.addVariable(this_dims_var);
      dims.add(d);
    }
    Variable.Builder vb =Variable.builder().setName(nadef.getName()).setDataType(nadef.getDataType().translate()).setDimensions(dims);
    if(nadef.getDescription() != null) vb.addAttribute(new Attribute("longname", nadef.getDescription()));
    if(nadef.getUnits() != null) vb.addAttribute(new Attribute("units", nadef.getUnits()));
    gb.addVariable(vb);
  }

  public static DataType translate_datatype(Object o) {
    if (Array.newInstance(Integer.class, 0).getClass().equals(o.getClass()) || Array.newInstance(int.class, 0).getClass().equals(o.getClass())) {
      return DataType.INT;
    } else if (Array.newInstance(Double.class, 0).getClass().equals(o.getClass()) || Array.newInstance(double.class, 0).getClass().equals(o.getClass())) {
      return DataType.DOUBLE;
    } else if(Array.newInstance(String.class, 0).getClass().equals(o.getClass())) {
      return DataType.STRING;
    }
    throw(new UnsupportedOperationException("can't translate object of class "+ o.getClass().getSimpleName() + " to NetCDF data type."));
  }

  Group.Builder getGroup(Group.Builder start_group, String group_name) {
    logger.trace("getGroup({}, {})", start_group, group_name);
    if(group_name.startsWith("/")) group_name = group_name.substring(1);
    if(start_group == null) start_group = netcdfBuilderWrapper.builder.getRootGroup();
    String[] split = group_name.split("/", 2);
    if(start_group.findGroupLocal(split[0]).isPresent()){
      if(split.length == 2) {
        return getGroup(start_group.findGroupLocal(split[0]).get(), split[1]);
      }else {
        return start_group.findGroupLocal(split[0]).get();
      }
    }else{
      if(split.length == 2) {
        return createGroup(start_group, split[0], split[1]);
      }else{
        return createGroup(start_group, split[0], null);
      }
    }
  }
  Group.Builder createGroup(Group.Builder start_group, String groupName, String subgroups) {
    logger.trace("createGroup({}, {}, {})", start_group, groupName, subgroups);
    Group.Builder newGroup = Group.builder().setParentGroup(start_group).setName(groupName);
    start_group.addGroup(newGroup);
    if(subgroups == null) {
      return newGroup;
    }
    String[] split = subgroups.split("/", 2);
    if(split.length == 2) return createGroup(newGroup, split[0], split[1]);
    return createGroup(newGroup, split[0], null);
  }

  @Override
  public void close() {
    logger.trace("close()");
    cleanable.clean();
  }

}
