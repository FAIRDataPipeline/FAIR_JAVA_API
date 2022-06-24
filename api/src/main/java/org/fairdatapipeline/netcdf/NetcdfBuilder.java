package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Array;
import java.util.*;

import org.fairdatapipeline.objects.NumericalArrayDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFormatWriter;
import ucar.nc2.write.NetcdfFileFormat;

/**
 *
 */
public class NetcdfBuilder implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(NetcdfBuilder.class);
  private static final Cleaner cleaner = Cleaner.create();
  // todo: we should not create more cleaners than necessary
  private final Cleanable cleanable;
  private final NetcdfBuilderWrapper netcdfBuilderWrapper;



  public NetcdfBuilder(String filePath, Nc4Chunking.Strategy nc4chunkingStrategy, int nc4deflateLevel, boolean nc4shuffle, Runnable onClose) {
    logger.trace("NetcdfBuilder({}) ", filePath);
    this.netcdfBuilderWrapper = new NetcdfBuilderWrapper(filePath, nc4chunkingStrategy, nc4deflateLevel,  nc4shuffle, onClose);
    this.cleanable = cleaner.register(this, this.netcdfBuilderWrapper);
  }

  // Defining a resource that requires cleaning
  private static class NetcdfBuilderWrapper implements Runnable {
    private final NetcdfFormatWriter.Builder builder;
    private boolean has_been_built = false;

    private final Runnable runOnClose;

    NetcdfBuilderWrapper(String filePath,  Nc4Chunking.Strategy nc4chunkingStrategy, int nc4deflateLevel, boolean nc4shuffle, Runnable runOnClose) {
      Nc4Chunking chunker =
              Nc4ChunkingStrategy.factory(nc4chunkingStrategy, nc4deflateLevel, nc4shuffle);

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

  /** prepare a separate dimension in its own group, which other arrays can later refer to. this allows different arrays to share dimensions.
   *  these shared dimensions MUST reside in parent groups of the array group that is trying to use it. ie. /time_group/time_dim can be used as a
   *  dimension of the array in /time_group/personal_data/ but cannot be used as a dimension of the array in /other_group/somedata
   *
   *
   * @param group_name
   * @param dimensionDefinition
   */
  public void prepareDimension(String group_name, DimensionDefinitionLocal dimensionDefinition) {
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

  /** prepareArray creates the dimensions and dimension variables for the given NumericalArrayDefinition. It then creates the actual data variable.
   *
   * @param group_name
   * @param nadef
   */
  public void prepareArray(String group_name, NumericalArrayDefinition nadef) {
    List<Dimension> dims = new ArrayList<Dimension>();
    Group.Builder gb = getGroup(netcdfBuilderWrapper.builder.getRootGroup(), group_name);
    for(int i = 0; i < nadef.getDimensions().length; i++) {
      if(!nadef.getDimensions()[i].isLocal()) {
        DimensionDefinitionRemote dimdef = (DimensionDefinitionRemote) nadef.getDimensions()[i];
        // a DimensionDefinitionRemote is a shared dimension pointing to an already existing dimension in one of the
        // parent groups of group_name. (netCDF requires shared dimension reside within a parent group; trying to link to a dimension on a separate branch
        // results in java.lang.IllegalStateException: "does not exist in a parent group"
        String varname = dimdef.getName() + "_dim";
        String vargroup = dimdef.getGroupName();
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
        // it is a local dimension with all its details and needs to be created
        DimensionDefinitionLocal dimdef = (DimensionDefinitionLocal) nadef.getDimensions()[i];
        Dimension d;
        if(dimdef.getSize() == DimensionDefinitionLocal.UNLIMITED) {
          //if(i != 0) throw(new IllegalArgumentException("Only the first dimension can be unlimited"));
          // just found out that more than one dimension can be unlimited!
          d = new Dimension(dimdef.getName(), 0,true, true, false);
        }else {
          d = new Dimension(dimdef.getName() + "_dim", dimdef.getSize());
        }
        gb.addDimension(d);
        Variable.Builder this_dims_var = Variable.builder().setName(dimdef.getName()).setDataType(dimdef.getDataType().translate()).setDimensions(Collections.singletonList(d));
        this_dims_var.addAttribute(new Attribute("units", dimdef.getUnits()));
        gb.addVariable(this_dims_var);
        dims.add(d);
      }
    }
    Variable.Builder vb =Variable.builder().setName(nadef.getName()).setDataType(nadef.getDataType().translate()).setDimensions(dims);
    if(nadef.getDescription() != null) vb.addAttribute(new Attribute("longname", nadef.getDescription()));
    if(nadef.getUnits() != null) vb.addAttribute(new Attribute("units", nadef.getUnits()));
    gb.addVariable(vb);
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
