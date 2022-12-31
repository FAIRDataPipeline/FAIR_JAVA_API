package org.fairdatapipeline.netcdf;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.util.*;
import org.fairdatapipeline.objects.CoordinateVariableDefinition;
import org.fairdatapipeline.objects.DimensionalVariableDefinition;
import org.fairdatapipeline.objects.TableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.*;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFileFormat;
import ucar.nc2.write.NetcdfFormatWriter;

/** */
public class NetcdfBuilder implements AutoCloseable {
  private static final String ATTRIB_DESC = "description";
  private static final String ATTRIB_LNAME = "long_name";
  private static final String ATTRIB_FILLVALUE = "_FillValue";
  private static final String ATTRIB_UNITS = "units";
  private static final String ATTRIB_GROUP_TYPE = "group_type";

  private static final Logger LOGGER = LoggerFactory.getLogger(NetcdfBuilder.class);
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
    LOGGER.trace("NetcdfBuilder({}) ", filePath);
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
      LOGGER.trace("run() invoked by cleaner");
      runOnClose.run();
      if (!has_been_built) {
        // write the file:
        try (NetcdfFormatWriter w = builder.build()) {
          // do nothing
        } catch (IOException e) {
          LOGGER.error("failed to write netcdf file", e);
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
   * groups of the array group that is trying to use it. i.e. /time_group/time_dim can be used as a
   * dimension of the array in /time_group/personal_data/ but cannot be used as a dimension of the
   * array in /other_group/somedata
   *
   * @param coordinateVariable
   */
  public void prepare(CoordinateVariableDefinition coordinateVariable) {
    Group.Builder gb =
        getGroup(
            netcdfBuilderWrapper.builder.getRootGroup(),
            coordinateVariable.getVariableName().getGroupName().toString());
    Dimension d;
    if (coordinateVariable.isUnlimited()) {
      d =
          new Dimension(
              coordinateVariable.getVariableName().getName().toString(), 0, true, true, false);
    } else {
      d =
          new Dimension(
              coordinateVariable.getVariableName().getName().toString(),
              coordinateVariable.getSize());
    }
    gb.addDimension(d);
    Variable.Builder<?> varbuilder =
        Variable.builder()
            .setName(coordinateVariable.getVariableName().getName().toString())
            .setDataType(coordinateVariable.getDataType().translate())
            .setDimensions(Collections.singletonList(d));
    if (coordinateVariable.getDescription().length() > 0)
      varbuilder.addAttribute(new Attribute(ATTRIB_DESC, coordinateVariable.getDescription()));
    if (coordinateVariable.getUnits().length() > 0)
      varbuilder.addAttribute(new Attribute(ATTRIB_UNITS, coordinateVariable.getUnits()));
    if (coordinateVariable.getLong_name().length() > 0)
      varbuilder.addAttribute(new Attribute(ATTRIB_LNAME, coordinateVariable.getLong_name()));
    if (coordinateVariable.getMissingValue() != null) {
      // coordinateVariables should not have any missing values.
      if (coordinateVariable.getMissingValue().getClass() == String.class) {
        varbuilder.addAttribute(
            new Attribute(ATTRIB_FILLVALUE, (String) coordinateVariable.getMissingValue()));
      } else {
        varbuilder.addAttribute(
            new Attribute(ATTRIB_FILLVALUE, (Number) coordinateVariable.getMissingValue()));
      }
    }
    gb.addVariable(varbuilder);
  }

  public void prepare(TableDefinition tabledef) {
    String dimensionName = "index";
    Group.Builder gb =
        getGroup(
            netcdfBuilderWrapper.builder.getRootGroup(), tabledef.getGroupName().toString(), true);
    Dimension d;
    if (tabledef.isUnlimited()) {
      d = new Dimension(dimensionName, 0, true, true, false);
    } else {
      d = new Dimension(dimensionName, tabledef.getSize());
    }
    gb.addDimension(d);
    if (tabledef.getDescription().length() > 0)
      gb.addAttribute(new Attribute(ATTRIB_DESC, tabledef.getDescription()));
    if (tabledef.getLong_name().length() > 0)
      gb.addAttribute(new Attribute(ATTRIB_LNAME, tabledef.getLong_name()));
    tabledef
        .getOptional_attribs()
        .forEach(
            (key, value) ->
                gb.addAttribute(
                    Attribute.builder()
                        .setName(key)
                        .setDataType(DataType.STRING)
                        .setValues(NetcdfDataType.translateArray(value))
                        .build()));
    Arrays.stream(tabledef.getColumns())
        .forEach(
            localVarDef ->
                this.prepare(
                    new DimensionalVariableDefinition(
                        localVarDef, tabledef.getGroupName(), dimensionName)));
    gb.addAttribute(new Attribute(ATTRIB_GROUP_TYPE, "table"));
  }

  String generatedDimName(String vName, int i) {
    return "__fdp_" + vName + "_dim_" + i;
  }

  /**
   * prepareArray creates the dimensions and dimension variables for the given
   * NumericalArrayDefinition. It then creates the actual data variable.
   *
   * @param nadef
   */
  public void prepare(DimensionalVariableDefinition nadef) {
    List<Dimension> dims = new ArrayList<>();
    VariableName vbn = nadef.getVariableName();
    NetcdfGroupName gn = vbn.getGroupName();
    String groupName = gn.toString();
    Group.Builder gb = getGroup(netcdfBuilderWrapper.builder.getRootGroup(), groupName);
    for (int i = 0; i < nadef.getDimensions().length; i++) {
      if (nadef.getDimensions()[i].is_size()) {
        int[] values = new int[nadef.getDimensions()[i].size()];
        for (int j = 0; j < nadef.getDimensions()[i].size(); j++) values[j] = j + 1;
        CoordinateVariableDefinition cvdef =
            new CoordinateVariableDefinition(
                new VariableName(
                    new NetcdfName(this.generatedDimName(vbn.getName().getName(), i)),
                    vbn.getGroupName()),
                values,
                "",
                "",
                "");
        this.prepare(cvdef);
      }
    }

    for (int i = 0; i < nadef.getDimensions().length; i++) {
      String dimName;
      if (nadef.getDimensions()[i].is_size()) {
        dimName = this.generatedDimName(vbn.getName().getName(), i);
      } else {
        dimName = nadef.getDimensions()[i].name().getName();
      }
      Optional<Dimension> optionalDimension = gb.findDimension(dimName);
      if (optionalDimension.isEmpty())
        throw (new IllegalArgumentException("Can't find dimension " + dimName));
      Dimension d = optionalDimension.get();
      dims.add(d);
    }
    Variable.Builder<?> varbuilder =
        Variable.builder()
            .setName(nadef.getVariableName().getName().toString())
            .setDataType(nadef.getDataType().translate())
            .setDimensions(dims);
    if (nadef.getDescription().length() > 0)
      varbuilder.addAttribute(new Attribute(ATTRIB_DESC, nadef.getDescription()));
    if (nadef.getUnits().length() > 0)
      varbuilder.addAttribute(new Attribute(ATTRIB_UNITS, nadef.getUnits()));
    if (nadef.getLong_name().length() > 0)
      varbuilder.addAttribute(new Attribute(ATTRIB_LNAME, nadef.getLong_name()));
    if (nadef.getMissingValue() != null) {
      if (nadef.getMissingValue().getClass() == String.class) {
        varbuilder.addAttribute(new Attribute(ATTRIB_FILLVALUE, (String) nadef.getMissingValue()));
      } else {
        varbuilder.addAttribute(new Attribute(ATTRIB_FILLVALUE, (Number) nadef.getMissingValue()));
      }
    }

    gb.addVariable(varbuilder);
  }

  Group.Builder getGroup(Group.Builder start_group, String group_name) {
    return getGroup(start_group, group_name, false);
  }

  Group.Builder getGroup(Group.Builder start_group, String group_name, boolean mustBeFresh) {
    LOGGER.trace("getGroup({}, {}, {}})", start_group, group_name, mustBeFresh);
    if (group_name.startsWith("/")) group_name = group_name.substring(1);
    if (start_group == null) start_group = netcdfBuilderWrapper.builder.getRootGroup();
    if (group_name.equals("")) {
      if (mustBeFresh) throw (new IllegalArgumentException("this group already exists"));
      if (start_group.getAttributeContainer().findAttribute(ATTRIB_GROUP_TYPE) != null)
        throw (new IllegalArgumentException(
            "we can't create anything new in groups marked with group_type attribute."));
      return start_group;
    }
    String[] split = group_name.split("/", 2);
    Optional<Group.Builder> optGroupBuilder = start_group.findGroupLocal(split[0]);
    if (optGroupBuilder.isPresent()) {
      Group.Builder found_group = optGroupBuilder.get();
      if (found_group.getAttributeContainer().findAttribute(ATTRIB_GROUP_TYPE) != null)
        throw (new IllegalArgumentException(
            "we can't create anything new in groups marked with group_type attribute."));
      if (split.length == 2) {
        return getGroup(found_group, split[1], mustBeFresh);
      } else {
        if (mustBeFresh) throw (new IllegalArgumentException("this group already exists"));
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
    LOGGER.trace("createGroup({}, {}, {})", start_group, groupName, subgroups);
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
    LOGGER.trace("close()");
    cleanable.clean();
  }
}
