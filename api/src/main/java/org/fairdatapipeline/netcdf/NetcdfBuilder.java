package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.*;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFileFormat;
import ucar.nc2.write.NetcdfFormatWriter;

/** */
public class NetcdfBuilder implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(NetcdfBuilder.class);
  private static final Cleaner cleaner = Cleaner.create();
  // todo: we should not create more cleaners than necessary
  private final Cleanable cleanable;
  private final NetcdfBuilderWrapper netcdfBuilderWrapper;

  public NetcdfBuilder(
      String filePath,
      Nc4Chunking.Strategy nc4chunkingStrategy,
      int nc4deflateLevel,
      boolean nc4shuffle,
      Runnable onClose) {
    logger.trace("NetcdfBuilder({}) ", filePath);
    this.netcdfBuilderWrapper =
        new NetcdfBuilderWrapper(
            filePath, nc4chunkingStrategy, nc4deflateLevel, nc4shuffle, onClose);
    this.cleanable = cleaner.register(this, this.netcdfBuilderWrapper);
  }

  // Defining a resource that requires cleaning
  private static class NetcdfBuilderWrapper implements Runnable {
    private final NetcdfFormatWriter.Builder builder;
    private boolean has_been_built = false;

    private final Runnable runOnClose;

    NetcdfBuilderWrapper(
        String filePath,
        Nc4Chunking.Strategy nc4chunkingStrategy,
        int nc4deflateLevel,
        boolean nc4shuffle,
        Runnable runOnClose) {
      Nc4Chunking chunker =
          Nc4ChunkingStrategy.factory(nc4chunkingStrategy, nc4deflateLevel, nc4shuffle);

      this.builder =
          NetcdfFormatWriter.createNewNetcdf4(NetcdfFileFormat.NETCDF4, filePath, chunker);

      this.runOnClose = runOnClose;
    }

    public NetcdfFormatWriter build() throws IOException {
      NetcdfFormatWriter w = builder.build();
      has_been_built = true;
      return w;
    }

    // Invoked by close method or cleaner
    @Override
    public void run() {
      logger.trace("run() invoked by cleaner");
      runOnClose.run();
      if (!has_been_built) {
        // write the file:
        try (NetcdfFormatWriter w = builder.build()) {
          // do nothing
        } catch (IOException e) {
          logger.error("failed to write netcdf file", e);
        }
      } else {
        // it has already been built. do nothing.
      }
    }
  }

  NetcdfFormatWriter build() throws IOException {
    return this.netcdfBuilderWrapper.build();
  }

  /**
   * prepare a separate dimension in its own group, which other arrays can later refer to. this
   * allows different arrays to share dimensions. these shared dimensions MUST reside in parent
   * groups of the array group that is trying to use it. ie. /time_group/time_dim can be used as a
   * dimension of the array in /time_group/personal_data/ but cannot be used as a dimension of the
   * array in /other_group/somedata
   *
   * @param coordinateVariable
   */
  public void prepareDimension(CoordinateVariableDefinition coordinateVariable) {
    Group.Builder gb =
        getGroup(
            netcdfBuilderWrapper.builder.getRootGroup(),
            coordinateVariable.getVariableName().getGroupName());
    Dimension d;
    if (coordinateVariable.isUnlimited()) {
      d = new Dimension(coordinateVariable.getVariableName().getName(), 0, true, true, false);
    } else {
      d =
          new Dimension(
              coordinateVariable.getVariableName().getName(), coordinateVariable.getSize());
    }
    gb.addDimension(d);
    Variable.Builder<?> varbuilder =
        Variable.builder()
            .setName(coordinateVariable.getVariableName().getName())
            .setDataType(coordinateVariable.getDataType().translate())
            .setDimensions(Collections.singletonList(d));
    if (coordinateVariable.getDescription().length() > 0)
      varbuilder.addAttribute(new Attribute("description", coordinateVariable.getDescription()));
    if (coordinateVariable.getUnits().length() > 0)
      varbuilder.addAttribute(new Attribute("units", coordinateVariable.getUnits()));
    if (coordinateVariable.getLong_name().length() > 0)
      varbuilder.addAttribute(new Attribute("long_name", coordinateVariable.getLong_name()));
    gb.addVariable(varbuilder);
  }

  /**
   * prepareArray creates the dimensions and dimension variables for the given
   * NumericalArrayDefinition. It then creates the actual data variable.
   *
   * @param nadef
   */
  public void prepareArray(DimensionalVariableDefinition nadef) {
    List<Dimension> dims = new ArrayList<>();
    Group.Builder gb =
        getGroup(
            netcdfBuilderWrapper.builder.getRootGroup(), nadef.getVariableName().getGroupName());
    for (int i = 0; i < nadef.getDimensions().length; i++) {
      VariableName dimName = nadef.getDimensions()[i];
      if (!nadef.getVariableName().getGroupName().equals(dimName.getGroupName())
          && !nadef.getVariableName().getGroupName().startsWith(dimName.getGroupName() + "/")) {
        throw (new IllegalArgumentException(
            "you can only link to variables in parent groups; "
                + dimName.getGroupName()
                + " is not a parent group of "
                + nadef.getVariableName().getGroupName()
                + "."));
      }
      Optional<Group.Builder> optGroupBuilder =
          netcdfBuilderWrapper.builder.getRootGroup().findGroupNested(dimName.getGroupName());
      if (!optGroupBuilder.isPresent())
        throw (new IllegalArgumentException(
            "Trying to link shared dimension but Group "
                + dimName.getGroupName()
                + " does not exist."));
      Optional<Dimension> optionalDimension =
          optGroupBuilder.get().findDimension(dimName.getName());
      if (!optionalDimension.isPresent())
        throw (new IllegalArgumentException("Can't find dimension " + dimName));
      Dimension d = optionalDimension.get();
      dims.add(d);
    }
    Variable.Builder<?> varbuilder =
        Variable.builder()
            .setName(nadef.getVariableName().getName())
            .setDataType(nadef.getDataType().translate())
            .setDimensions(dims);
    if (nadef.getDescription().length() > 0)
      varbuilder.addAttribute(new Attribute("description", nadef.getDescription()));
    if (nadef.getUnits().length() > 0)
      varbuilder.addAttribute(new Attribute("units", nadef.getUnits()));
    if (nadef.getLong_name().length() > 0)
      varbuilder.addAttribute(new Attribute("long_name", nadef.getLong_name()));
    gb.addVariable(varbuilder);
  }

  Group.Builder getGroup(Group.Builder start_group, String group_name) {
    logger.trace("getGroup({}, {})", start_group, group_name);
    if (group_name.startsWith("/")) group_name = group_name.substring(1);
    if (start_group == null) start_group = netcdfBuilderWrapper.builder.getRootGroup();
    if (group_name.equals("")) return start_group;
    String[] split = group_name.split("/", 2);
    Optional<Group.Builder> optGroupBuilder = start_group.findGroupLocal(split[0]);
    if (optGroupBuilder.isPresent()) {
      Group.Builder found_group = optGroupBuilder.get();
      if (split.length == 2) {
        return getGroup(found_group, split[1]);
      } else {
        return found_group;
      }
    } else {
      if (split.length == 2) {
        return createGroup(start_group, split[0], split[1]);
      } else {
        return createGroup(start_group, split[0], null);
      }
    }
  }

  Group.Builder createGroup(Group.Builder start_group, String groupName, String subgroups) {
    logger.trace("createGroup({}, {}, {})", start_group, groupName, subgroups);
    Group.Builder newGroup = Group.builder().setParentGroup(start_group).setName(groupName);
    start_group.addGroup(newGroup);
    if (subgroups == null) {
      return newGroup;
    }
    String[] split = subgroups.split("/", 2);
    if (split.length == 2) return createGroup(newGroup, split[0], split[1]);
    return createGroup(newGroup, split[0], null);
  }

  @Override
  public void close() {
    logger.trace("close()");
    cleanable.clean();
  }
}
