package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Array;
import java.util.*;

import com.google.errorprone.annotations.Var;
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
  // todo: we should not create more cleaners than necessary
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

  public void prepareDimension(String group_name, DimensionDefinition dimensionDefinition) {
    Group.Builder gb = getGroup(netcdfBuilderWrapper.builder.getRootGroup(), group_name);
    Dimension d;
    if(dimensionDefinition.isUnlimited()) {
      d = new Dimension(dimensionDefinition.getName() + "_dim", 0, true, true, false);
    }else{
      d = new Dimension(dimensionDefinition.getName() + "_dim", dimensionDefinition.getSize());
    }
    gb.addDimension(d);
    Variable.Builder var = Variable.builder().setName(dimensionDefinition.getName()).setDataType(dimensionDefinition.getDataType().translate()).setDimensions(Collections.singletonList(d));
    gb.addVariable(var);
  }

  public void prepareArray(String group_name, NumericalArrayDefinition nadef) {
    List<Dimension> dims = new ArrayList<Dimension>();
    Group.Builder gb = getGroup(netcdfBuilderWrapper.builder.getRootGroup(), group_name);
    for(int i = 0; i < nadef.getDimension_names().length; i++) {
      if(nadef.getDimension_names()[i].startsWith("/")) {
        // a dimension_name starting with '/' is a shared dimension pointing to an already existing dimension in one of the
        // parent groups of group_name. (netCDF requires shared dimension reside within a parent group; trying to link to a dimension on a separate branch
        // results in java.lang.IllegalStateException: "does not exist in a parent group"
        String[] split = nadef.getDimension_names()[i].split("/");
        String varname = split[split.length-1] + "_dim";
        String vargroup = String.join("/", Arrays.copyOfRange(split, 0, split.length-1));
        logger.error("vargroup: " + vargroup);
        logger.error("varname: " + varname);
        if(!group_name.startsWith(vargroup + "/")) {
          throw(new IllegalArgumentException("you can only link to variables in parent groups; " + vargroup + " is not a parent group of " + group_name + "."));
        }
        Optional<Group.Builder> optGroupBuilder = netcdfBuilderWrapper.builder.getRootGroup().findGroupNested(vargroup);
        if(!optGroupBuilder.isPresent()) throw(new IllegalArgumentException("Trying to link shared dimension but Group " + vargroup + " does not exist."));
        Optional<Dimension> optionalDimension = optGroupBuilder.get().findDimension(varname);
        if(!optionalDimension.isPresent()) throw(new IllegalArgumentException("Can't find dimension " + varname  + " in group " + vargroup));
        Dimension d = optionalDimension.get();
        if(d.isUnlimited() && i != 0) throw(new IllegalArgumentException("Only the first dimension can be unlimited"));
        dims.add(d);
      }else {
        Dimension d;
        if(nadef.getDimension_sizes()[i] == 0) {
          if(i != 0) throw(new IllegalArgumentException("Only the first dimension can be unlimited"));
          d = new Dimension(nadef.getDimension_names()[i], 0,true, true, false);
        }else {
          d = new Dimension(nadef.getDimension_names()[i] + "_dim", nadef.getDimension_sizes()[i]);
        }
        gb.addDimension(d);
        Variable.Builder this_dims_var = Variable.builder().setName(nadef.getDimension_names()[i]).setDataType(translate_datatype(nadef.getDimension_values()[i])).setDimensions(Collections.singletonList(d));
        if (nadef.getDimension_units() != null) {
          this_dims_var.addAttribute(new Attribute("units", nadef.getDimension_units()[i]));
        }
        gb.addVariable(this_dims_var);
        dims.add(d);
      }
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
