package org.fairdatapipeline.netcdf;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.errorprone.annotations.Var;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.fairdatapipeline.objects.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NetcdfWriterTest {
  private static final String COMMA_DELIMITER = ",";
  final Runnable onClose = this::myClose;

  private void myClose() {
    // do nothing
  }

  /**
   * test a simple INT array stored using NetcdfBuilder.prepareArray and .writeDimensionVariables
   * and .writeArrayData
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_build_prepare_write_INT() throws IOException, URISyntaxException {
    String filename = "test_build_prepare_write_INT";
    String resourceName = "/netcdf/test_build_prepare_write_INT.nc";
    String group = "aap/noot/mies";
    VariableName xName = new VariableName("X", group);
    VariableName yName = new VariableName("Y", group);
    VariableName tempName = new VariableName("temperature", group);

    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(
            xName,
            new int[] {2, 4},
            "the x-axis is measured in along the length of my football pitch; (0,0) is the southwest corner.",
            "m",
            "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(
            yName,
            new int[] {3, 6, 9},
            "the y-axis is measured in along the width of my football pitch; (0,0) is the southwest corner.",
            "m",
            "");
    DimensionalVariableDefinition nadef =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {xName, yName},
            "a test dataset with temperatures in 2d space",
            "C",
            "surface temperature");
    NumericalArray nadat = new NumericalArrayImpl(new int[][] {{1, 2, 3}, {11, 12, 13}});
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(xdim);
      b.prepare(ydim);
      b.prepare(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        w.writeArrayData(w.getVariable(nadef.getVariableName()), nadat);
      } catch (InvalidRangeException e) {
        //
      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }


  @Test
  void test_build_prepare_write_with_fillvalue() throws IOException, URISyntaxException {
    String filename = "test_build_prepare_write_with_fillvalue";
    String resourceName = "/netcdf/test_build_prepare_write_with_fillvalue.nc";
    String group = "mytestgroup";
    VariableName dimName = new VariableName("dim", group);
    VariableName dataName = new VariableName("data", group);
    NetcdfGroupName gn = dataName.getGroupName();
    System.out.println(gn.toString());

    System.out.println(dataName);

    CoordinateVariableDefinition dim =
            new CoordinateVariableDefinition(
                    dimName,
                    new int[] {1, 2, 3, 4, 5, 6, 7, 8},
                    "as simple as ABC",
                    "",
                    "");
    DimensionalVariableDefinition nadef =
            new DimensionalVariableDefinition(
                    dataName,
                    NetcdfDataType.INT,
                    new VariableName[] {dimName},
                    "a test dataset with missing values",
                    "",
                    "", Collections.emptyMap(), -1);
    System.out.println("nadef1");
    System.out.println(nadef);
    System.out.println("nadef1.name");
    System.out.println(nadef.getVariableName());
    NumericalArray nadat = new NumericalArrayImpl(new int[] {1, 2, 3, -1, 5, 6});
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(dim);
      b.prepare(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(dim);
        w.writeArrayData(w.getVariable(nadef.getVariableName()), nadat);
      } catch (InvalidRangeException e) {
        //
      }
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void test_table() throws IOException, URISyntaxException {
    String filename = "test_table";
    String resourceName = "/netcdf/test_table.nc";

    LocalVariableDefinition[] columns;

    NetcdfGroupName tableName = new NetcdfGroupName("myTestTable");

    columns = new LocalVariableDefinition[] {
            new LocalVariableDefinition(
                    new NetcdfName("id"),
                    NetcdfDataType.INT,
                    "", "", ""),
            new LocalVariableDefinition(
                    new NetcdfName("itemName"),
                    NetcdfDataType.STRING,
                    "", "", "item name"),
            new LocalVariableDefinition(
                    new NetcdfName("value"),
                    NetcdfDataType.DOUBLE,
                    "", "", "value of the item")
    };

    TableDefinition table = new TableDefinition(tableName, 0, "This is a simple test table to see what it will look like in a netCDF file.", "My little simple initial test table", Collections.emptyMap(), columns);
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      // prescription variables:
      b.prepare(table);


      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        int[] ids = new int[]{1, 2, 3};
        String[] names = new String[]{"apples", "bananas", "pears"};
        double[] doubles = new double[]{1.1, 2.2, 3.3};

        w.writeArrayData(w.getVariable(table.getVariableName(0)), NetcdfDataType.translateArray(ids));
        w.writeArrayData(w.getVariable(table.getVariableName(1)), NetcdfDataType.translateArray(names));
        w.writeArrayData(w.getVariable(table.getVariableName(2)), NetcdfDataType.translateArray(doubles));
      }catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void test_table_with_missing() throws IOException, URISyntaxException {
    String filename = "test_table_with_missing";
    String resourceName = "/netcdf/test_table_with_missing.nc";

    LocalVariableDefinition[] columns;

    NetcdfGroupName tableName = new NetcdfGroupName("myTestTable");

    columns = new LocalVariableDefinition[] {
            new LocalVariableDefinition(
                    new NetcdfName("id"),
                    NetcdfDataType.INT,
                    "", "", "", Collections.emptyMap(), -1),
            new LocalVariableDefinition(
                    new NetcdfName("itemName"),
                    NetcdfDataType.STRING,
                    "", "", "item name", Collections.emptyMap(), ""),
            new LocalVariableDefinition(
                    new NetcdfName("value"),
                    NetcdfDataType.DOUBLE,
                    "", "", "value of the item", Collections.emptyMap(), Double.NaN)
    };

    TableDefinition table = new TableDefinition(tableName, 0, "This is a simple test table to see what it will look like in a netCDF file.", "My little simple test table with some missing values", Collections.singletonMap("random_attribute", new String[] {"just testing a random attribute"}), columns);
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      // prescription variables:
      b.prepare(table);


      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        int[] ids = new int[]{1, -1, 3, 4, 5, 6};
        String[] names = new String[]{"apples", "bananas", "pears", "", "kiwis", "mangos"};
        double[] doubles = new double[]{1.1, 2.2, 3.3, 4.4, Double.NaN, 6.6};

        w.writeArrayData(w.getVariable(table.getVariableName(0)), NetcdfDataType.translateArray(ids));
        w.writeArrayData(w.getVariable(table.getVariableName(1)), NetcdfDataType.translateArray(names));
        w.writeArrayData(w.getVariable(table.getVariableName(2)), NetcdfDataType.translateArray(doubles));
      }catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
            FileUtils.contentEquals(
                    filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void test_prescribing_data() throws IOException, URISyntaxException {
    String filename = "test_prescribing_data";
    String resourceName = "/netcdf/test_prescribing_data.nc.bz2";
    String prescribingCsvResourceName = "/prescribing_june2016.csv.bz2";
    String hbtResourceName = "/health boards.csv";
    String countrycodeName = "/country code.csv";

    LocalVariableDefinition[] columns;

    NetcdfGroupName prescribingGroupName = new NetcdfGroupName("themes/health_and_care/prescriptions_in_the_community/prescribing_data_june_2016");
    NetcdfGroupName healthboardGroupName = new NetcdfGroupName("themes/health_and_care/geography_codes_and_labels/health_board_2014_-_health_board_2019");
    NetcdfGroupName countriesGroupName = new NetcdfGroupName("themes/health_and_care/geography_codes_and_labels/country");
    int num_healthboards = 18;
    int num_countries = 1;

    //     COUNTRY VARIABLES:

    columns = new LocalVariableDefinition[] {
            new LocalVariableDefinition(
                    new NetcdfName("id"),
                    NetcdfDataType.INT,
                    "", "", ""),
            new LocalVariableDefinition(
                    new NetcdfName("Country"),
                    NetcdfDataType.STRING,
                    "", "", "Country Code"),
            new LocalVariableDefinition(
                    new NetcdfName("CountryName"),
                    NetcdfDataType.STRING,
                    "", "", "Name of the Country")
    };
    TableDefinition countries = new TableDefinition(
            countriesGroupName,
            0,
            "9 digit standard geography codes (S92) and matching labels for Country",
            "",
            Stream.of(new String[][]{
                    {"source", "https://www.opendata.nhs.scot/dataset/geography-codes-and-labels/resource/9c6e6c56-2697-4184-92c6-60d69c2b6792"},
                    {"linked_from", new VariableName(new NetcdfName("Country"), healthboardGroupName).toString()}
            }).collect(Collectors.toMap(data -> data[0], data -> new String[] {data[1]})),
            columns);

    // HEALTH BOARDS VARIABLES:

    columns = new LocalVariableDefinition[] {
            new LocalVariableDefinition(
                    new NetcdfName("id"),
                    NetcdfDataType.INT,
                    "", "", ""),
            new LocalVariableDefinition(
                    new NetcdfName("HB"),
                    NetcdfDataType.STRING,
                    "", "", "Health Board 2014 Code (revised in 2018 & 2019)"),
            new LocalVariableDefinition(
                    new NetcdfName("HBName"),
                    NetcdfDataType.STRING,
                    "", "", "Name of the Health Board 2014 (revised in 2018 & 2019)"),
            new LocalVariableDefinition(
                    new NetcdfName("HBDateEnacted"),
                    NetcdfDataType.INT,
                    "", "", "Date Health Board Code was enacted."),
            new LocalVariableDefinition(
                    new NetcdfName("HBDateArchived"),
                    NetcdfDataType.INT,
                    "", "", "Date Health Board Code was archived.", Collections.emptyMap(), 0),
            new LocalVariableDefinition(
                    new NetcdfName("Country"),
                    NetcdfDataType.STRING,
                    "", "", "Country Code for Scotland",
                    Collections.singletonMap("linked_to", new String[] {countriesGroupName.toString()}))
    };

    TableDefinition healthboards = new TableDefinition(
            healthboardGroupName,
            0,
            "9 digit standard geography codes (S08) and matching labels Health Board 2014 (revised in 2018 & 2019) in the health sector.\n" +
            "\n" +
            "All 14 Health Boards are listed with their corresponding name and the country code for Scotland.\n" +
            "\n" +
            "From 02-Feb 2018 there has been a minor boundary change to Keltybridge and Fife Environmental Energy Park at Westfield. From 01-Apr 2019 " +
            "there has been a boundary change to Cardowan by Stepps. On both occasions, the affected Council Area, Health and Social Care Partnership " +
            "and Health Board codes have been archived, and new codes introduced reflecting the new boundaries.",
            "Health Board 2014 - Health Board 2019",
            Stream.of(new String[][]{
                    {"source", "https://www.opendata.nhs.scot/dataset/geography-codes-and-labels/resource/652ff726-e676-4a20-abda-435b98dd7bdc"},
                    {"linked_from", new VariableName(new NetcdfName("HBT"), prescribingGroupName).toString()}
            }).collect(Collectors.toMap(data -> data[0], data -> new String[] {data[1]})),
            columns);




    // ACTUAL PRESCRIPTION VARIABLES:

    columns = new LocalVariableDefinition[]{
            new LocalVariableDefinition(
                    new NetcdfName("id"),
                    NetcdfDataType.INT,
                    "",
                    "",
                    ""),
            new LocalVariableDefinition(
                    new NetcdfName("HBT"),
                    NetcdfDataType.STRING,
                    "",
                    "",
                    "Each NHS health board has a unique nine digit code identifying the NHS board where prescribing of an item took place, based on boundaries as at 1st April 2014",
                    Collections.singletonMap("linked_to", new String[] {healthboardGroupName.toString()})),
            new LocalVariableDefinition(
                    new NetcdfName("GPPractice"),
                    NetcdfDataType.INT,
                    "",
                    "",
                    "Unique five digit numeric GP practice code identifying where the prescribing of an item took place. If it is not possible to determine the exact location from which a prescription originates it is assigned to an unallocated practice code. __Unallocated__ practice codes have been assigned the 99997. Prescriptions that originated from a __dentist surgery__ have been assigned the code 99999. Prescriptions that originated from a community __pharmacy__ have been assigned the code 99996. Prescriptions that originated from a __hospital__ have been assigned the code 99998.",
                    Collections.singletonMap("linked_to", new String[] {"https://www.opendata.nhs.scot/dataset/gp-practice-contact-details-and-list-sizes"})),
            new LocalVariableDefinition(
                    new NetcdfName("BNFItemCode"),
                    NetcdfDataType.STRING,
                    "",
                    "",
                    "A 15 digit British National Formulary (BNF) Item code in which the first seven digits are allocated according to the categories in the BNF and the last 8 digits represent the medicinal product, form, strength and the link to the generic equivalent product. The BNF Item Code takes the following form: *Characters 1 & 2 show BNF chapter; *3 & 4 show the BNF section; *5 & 6 show the BNF paragraph; *7 shows the BNF sub-paragraph; *8 & 9 show the chemical substance; *10 & 11 show the product; *12 & 13 show the strength and formulation; *14 & 15 show the link to the generic equivalent product: Where the product is a generic, the 14th and 15th characters will be the same as the 12th and 13th character; Where the product is a brand, the 14th and 15th characters will be the same as the generic equivalent (if this exists); Where the product is a brand and a generic equivalent does not exist, the 14th and 15th characters will be \"A0\". There are items within the prescribing dataset (mainly from the additional chapters that are included as appendices within the BNF or not listed in the BNF) that have a shorter BNF item code which has been manually input to allow users of the data to identify the BNF chapter, BNF section, BNF paragraph, and BNF sub-paragraph an item aligns to. These items follow the numbering convention above for the initial six digits."),
            new LocalVariableDefinition(
                    new NetcdfName("BNFItemDescription"),
                    NetcdfDataType.STRING,
                    "",
                    "",
                    "The drug item description as it appears in the latest edition of the British National Formulary (BNF), detailing the product name, formulation and strength."),
            new LocalVariableDefinition(
                    new NetcdfName("NumberOfPaidItems"),
                    NetcdfDataType.INT,
                    "",
                    "",
                    "The number of paid items relates to the number of prescription items dispensed and for which the dispenser has been reimbursed. An item is an individual product dispensed, e.g. 100 aspirin tablets of 300mg. There should be a maximum of three line items on a prescription; this should be three individual products defined by active ingredient, formulation type and strength for medicines, with appropriate parallel measures for appliances. A compounded product with a known formula will count as one item despite the number of ingredients."),
            new LocalVariableDefinition(
                    new NetcdfName("PaidQuantity"),
                    NetcdfDataType.FLOAT,
                    "",
                    "",
                    "Paid quantity of an individual item for which the dispenser has been reimbursed, e.g. 100 tablets."),
            new LocalVariableDefinition(
                    new NetcdfName("GrossIngredientCost"),
                    NetcdfDataType.FLOAT,
                    "",
                    "",
                    "Paid Gross Ingredient Cost (excluding Broken Bulk) is the cost of drugs and appliances reimbursed before deduction of any dispenser discount, i.e. the basic price of a drug as listed in the Scottish Drug Tariff or price lists. Note that this definition differs from other parts of the UK. The figures are in £s and pence. The Gross Ingredient Cost measure excludes broken bulk, that allows a contractor to claim a complete pack where a prescription is received for a product which comes in a larger pack and there is a risk of no further prescriptions for the product before the stock expires."),
            new LocalVariableDefinition(
                    new NetcdfName("PaidDateMonth"),
                    NetcdfDataType.INT,
                    "",
                    "",
                    "The date (YYYYMM) in which the prescription item was processed for payment.")
    };

    TableDefinition prescribing = new TableDefinition(
            prescribingGroupName,
            0,
            "Information on community pharmacy activity and direct pharmaceutical care services, covering June 2016 for all NHS health boards. Publication Date 13 September 2016",
            "Prescribing Data June 2016",
            Collections.singletonMap("source",new String[] {"https://www.opendata.nhs.scot/dataset/prescriptions-in-the-community/resource/a636862a-77e0-4c97-ba97-268578534a8e"}),
            columns);


    Path filePath = Files.createTempFile(filename, ".nc");
    Instant start = Instant.now();
    try (NetcdfBuilder b =
                 new NetcdfBuilder(
                         filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      // prescription variables:
      b.prepare(countries);
      b.prepare(healthboards);
      b.prepare(prescribing);


      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)){
        try(BufferedReader csvFile = new BufferedReader(
                new FileReader(
                        Path.of(getClass().getResource(hbtResourceName).toURI()).toFile()))){
          // read the health board CSV file
          String line = csvFile.readLine(); // skip the header line
          int[] idbuffer = new int[num_healthboards];
          String[] hbbuffer = new String[num_healthboards];
          String[] hbnamebuffer = new String[num_healthboards];
          int[] hbenactedbuffer = new int[num_healthboards];
          int[] hbarchivedbuffer = new int[num_healthboards];
          String[] countrybuffer = new String[num_healthboards];
          int i = 0;
          while ((line = csvFile.readLine()) != null) {
            String[] values = line.split(COMMA_DELIMITER);
            idbuffer[i] = Integer.parseInt(values[0]);
            hbbuffer[i] = values[1];
            hbnamebuffer[i] = values[2];
            hbenactedbuffer[i] = Integer.parseInt(values[3]);
            try {
              if (values[4].length() > 0) hbarchivedbuffer[i] = Integer.parseInt(values[4]);
              else hbarchivedbuffer[i] = (int) healthboards.getColumns()[4].getMissingValue();
            }catch(NumberFormatException e) {
              System.out.println("numberformatexception: '" + values[4] + "'");
              hbarchivedbuffer[i] = (int) healthboards.getColumns()[4].getMissingValue();
            }
            countrybuffer[i] = values[5];
            i += 1;
          }
          w.writeArrayData(w.getVariable(healthboards.getVariableName(0)), NetcdfDataType.translateArray(idbuffer));
          w.writeArrayData(w.getVariable(healthboards.getVariableName(1)), NetcdfDataType.translateArray(hbbuffer));
          w.writeArrayData(w.getVariable(healthboards.getVariableName(2)), NetcdfDataType.translateArray(hbnamebuffer));
          w.writeArrayData(w.getVariable(healthboards.getVariableName(3)), NetcdfDataType.translateArray(hbenactedbuffer));
          w.writeArrayData(w.getVariable(healthboards.getVariableName(4)), NetcdfDataType.translateArray(hbarchivedbuffer));
          w.writeArrayData(w.getVariable(healthboards.getVariableName(5)), NetcdfDataType.translateArray(countrybuffer));
        }
        try(BufferedReader csvFile = new BufferedReader(
                new FileReader(
                        Path.of(getClass().getResource(countrycodeName).toURI()).toFile()))) {
          // read the country code CSV file; treat it as if it has more than 1 line.
          String line = csvFile.readLine(); // skip the header line
          int[] idbuffer = new int[num_countries];
          String[] countrybuffer = new String[num_countries];
          String[] countrynamebuffer = new String[num_countries];
          int i = 0;
          while ((line = csvFile.readLine()) != null) {
            String[] values = line.split(COMMA_DELIMITER);
            idbuffer[i] = Integer.parseInt(values[0]);
            countrybuffer[i] = values[1];
            countrynamebuffer[i] = values[2];
            i += 1;
          }
          w.writeArrayData(w.getVariable(countries.getVariableName(0)), NetcdfDataType.translateArray(idbuffer));
          w.writeArrayData(w.getVariable(countries.getVariableName(1)), NetcdfDataType.translateArray(countrybuffer));
          w.writeArrayData(w.getVariable(countries.getVariableName(2)), NetcdfDataType.translateArray(countrynamebuffer));
        }
        try(BufferedReader csvFile = new BufferedReader(
                new InputStreamReader(
                new BZip2CompressorInputStream(
                    new FileInputStream(
                      Path.of(
                          getClass().getResource(prescribingCsvResourceName).toURI()
                      ).toFile()), true)))) {

          String line = csvFile.readLine(); // skip the header line..

          Variable idVar = w.getVariable(prescribing.getVariableName(0));
          Variable hbtVar = w.getVariable(prescribing.getVariableName(1));
          Variable gpVar = w.getVariable(prescribing.getVariableName(2));
          Variable bnfcodeVar = w.getVariable(prescribing.getVariableName(3));
          Variable bnfdescVar = w.getVariable(prescribing.getVariableName(4));
          Variable numitemsVar = w.getVariable(prescribing.getVariableName(5));
          Variable paidqVar = w.getVariable(prescribing.getVariableName(6));
          Variable grossVar = w.getVariable(prescribing.getVariableName(7));
          Variable paiddatemonthVar = w.getVariable(prescribing.getVariableName(8));

          int[] origin = new int[]{0};
          int linenum = 0;
          int bufindex = 0;
          final int bufsize = 50000;
          int[] idbuffer = new int[bufsize];
          String[] hbtwritebuffer = new String[bufsize];
          int[] gppracticebuffer = new int[bufsize];
          String[] bnfcodebuffer = new String[bufsize];
          String[] bnfdescbuffer = new String[bufsize];
          int[] numitemsbuffer = new int[bufsize];
          float[] paidquantitybuffer = new float[bufsize];
          float[] grossingredientcostbuffer = new float[bufsize];
          int[] paiddatemonthbuffer = new int[bufsize];

          while ((line = csvFile.readLine()) != null) {
            String[] values = line.split(COMMA_DELIMITER);
            idbuffer[bufindex] = Integer.parseInt(values[0]);
            hbtwritebuffer[bufindex] = values[1];
            gppracticebuffer[bufindex] = Integer.parseInt(values[2]);
            bnfcodebuffer[bufindex] = values[3];
            bnfdescbuffer[bufindex] = values[4];
            numitemsbuffer[bufindex] = Integer.parseInt(values[5]);
            paidquantitybuffer[bufindex] = Float.parseFloat(values[6]);
            grossingredientcostbuffer[bufindex] = Float.parseFloat(values[7]);
            paiddatemonthbuffer[bufindex] = Integer.parseInt(values[8]);
            bufindex += 1;
            if (bufindex == bufsize) {
              w.writeArrayData(idVar, NetcdfDataType.translateArray(idbuffer), origin);
              w.writeArrayData(hbtVar, NetcdfDataType.translateArray(hbtwritebuffer), origin);
              w.writeArrayData(gpVar, NetcdfDataType.translateArray(gppracticebuffer), origin);
              w.writeArrayData(bnfcodeVar, NetcdfDataType.translateArray(bnfcodebuffer), origin);
              w.writeArrayData(bnfdescVar, NetcdfDataType.translateArray(bnfdescbuffer), origin);
              w.writeArrayData(numitemsVar, NetcdfDataType.translateArray(numitemsbuffer), origin);
              w.writeArrayData(paidqVar, NetcdfDataType.translateArray(paidquantitybuffer), origin);
              w.writeArrayData(grossVar, NetcdfDataType.translateArray(grossingredientcostbuffer), origin);
              w.writeArrayData(paiddatemonthVar, NetcdfDataType.translateArray(paiddatemonthbuffer), origin);
              origin[0] = origin[0] + bufsize;
              bufindex = 0;
            }
            linenum += 1;
            if (linenum == 100000 * Math.floor(linenum / 100000)) {
              System.out.println(linenum);
            }
          }
          w.writeArrayData(idVar, NetcdfDataType.translateArray(Arrays.copyOf(idbuffer, bufindex)), origin);
          w.writeArrayData(hbtVar, NetcdfDataType.translateArray(Arrays.copyOf(hbtwritebuffer, bufindex)), origin);
          w.writeArrayData(gpVar, NetcdfDataType.translateArray(Arrays.copyOf(gppracticebuffer, bufindex)), origin);
          w.writeArrayData(bnfcodeVar, NetcdfDataType.translateArray(Arrays.copyOf(bnfcodebuffer, bufindex)), origin);
          w.writeArrayData(bnfdescVar, NetcdfDataType.translateArray(Arrays.copyOf(bnfdescbuffer, bufindex)), origin);
          w.writeArrayData(numitemsVar, NetcdfDataType.translateArray(Arrays.copyOf(numitemsbuffer, bufindex)), origin);
          w.writeArrayData(paidqVar, NetcdfDataType.translateArray(Arrays.copyOf(paidquantitybuffer, bufindex)), origin);
          w.writeArrayData(grossVar, NetcdfDataType.translateArray(Arrays.copyOf(grossingredientcostbuffer, bufindex)), origin);
          w.writeArrayData(paiddatemonthVar, NetcdfDataType.translateArray(Arrays.copyOf(paiddatemonthbuffer, bufindex)), origin);
        }


      } catch (InvalidRangeException e) {
        //
      }
    }
    Instant end = Instant.now();
    System.out.println(Duration.between(start, end));
    start = Instant.now();
    Path bz2file = Path.of(filePath + ".bz2");
    {
      InputStream in = Files.newInputStream(filePath);
      OutputStream fout = Files.newOutputStream(bz2file);
      BufferedOutputStream out = new BufferedOutputStream(fout);
      BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out);
      final byte[] buffer = new byte[65536];
      int i = 0;
      while (-1 != (i = in.read(buffer))) {
        bzOut.write(buffer, 0, i);
      }
      bzOut.close();
      in.close();
    }
    end = Instant.now();
    System.out.println(Duration.between(start, end));

    Assertions.assertTrue(
            FileUtils.contentEquals(
                    bz2file.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
    FileUtils.delete(bz2file.toFile());
  }

  /** testing the prepare/write sequence with 2 arrays. */
  @Test
  void test_build_write_two_arrays() throws IOException, URISyntaxException {
    String filename = "test_build_write_two_arrays";
    String resourceName = "/netcdf/test_build_write_two_arrays.nc";
    String group1 = "my/group/temps";
    String group2 = "my/othergroup/heights";
    VariableName xName = new VariableName("X", group1);
    VariableName yName = new VariableName("Y", group1);
    VariableName tempName = new VariableName("temp", group1);

    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(
            xName,
            new int[] {2, 4},
            "the x-axis runs east-west with 0 = south-east corner of my garden",
            "cm",
            "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(
            yName,
            new int[] {3, 6, 9},
            "the y-axis runs south-north with 0 = south-east corner of my garden",
            "cm",
            "");
    DimensionalVariableDefinition temperature =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {xName, yName},
            "a test dataset with int temperatures in 2d space, measure in a 2cm grid",
            "C",
            "surface temperature");

    VariableName personName = new VariableName("person", group2);
    VariableName dateName = new VariableName("date", group2);
    VariableName personheightName = new VariableName("personheight", group2);
    CoordinateVariableDefinition persondim =
        new CoordinateVariableDefinition(
            personName,
            new String[] {"Bram Boskamp", "Rosalie Boskamp"},
            "the person's name is good enough an identifier for me",
            "",
            "named person");
    CoordinateVariableDefinition datedim =
        new CoordinateVariableDefinition(
            dateName,
            new int[] {1640995200, 1643673600, 1646092800},
            "the date the measurement was taken",
            "seconds since 01-01-1970 00:00:00",
            "");

    DimensionalVariableDefinition heights =
        new DimensionalVariableDefinition(
            personheightName,
            NetcdfDataType.DOUBLE,
            new VariableName[] {personName, dateName},
            "a test dataset with real height in 2d space, with measurements for each person on a number of dates",
            "m",
            "");

    NumericalArray temp_data = new NumericalArrayImpl(new int[][] {{1, 2, 3}, {11, 12, 13}});
    NumericalArray height_data =
        new NumericalArrayImpl(new double[][] {{1.832, 1.828, 1.823}, {1.229, 1.232, 1.239}});
    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(xdim);
      b.prepare(ydim);
      b.prepare(temperature);
      b.prepare(persondim);
      b.prepare(datedim);
      b.prepare(heights);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        w.writeDimensionVariable(persondim);
        w.writeDimensionVariable(datedim);
        w.writeArrayData(w.getVariable(tempName), temp_data);
        w.writeArrayData(w.getVariable(personheightName), height_data);
      } catch (InvalidRangeException e) {
        //
      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   * test using NetcdfWritehandle to write a 3d array in timeslices: an XY 2d set for 1 write each t
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_build_write_in_parts1() throws IOException, URISyntaxException {
    String filename = "test_build_write_in_parts1";
    String resourceName = "/netcdf/test_build_write_in_parts.nc";

    String group = "three/d/intime";

    VariableName timeName = new VariableName("time", group);
    VariableName xName = new VariableName("X", group);
    VariableName yName = new VariableName("Y", group);

    CoordinateVariableDefinition timedim =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {1640995200, 1640995201, 1640995202, 1640995203, 1640995204},
            "",
            "seconds since 01-01-1970",
            "");
    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(xName, new int[] {2, 4}, "my x axis", "cm", "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(yName, new int[] {3, 6, 9}, "my y axis", "cm", "");

    VariableName temperatureName = new VariableName("temperature", group);

    DimensionalVariableDefinition nadef =
        new DimensionalVariableDefinition(
            temperatureName,
            NetcdfDataType.INT,
            new VariableName[] {timeName, xName, yName},
            "a test dataset with temperatures in time and space",
            "C",
            "");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(timedim);
      b.prepare(xdim);
      b.prepare(ydim);
      b.prepare(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(timedim);
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        Variable v = w.getVariable(temperatureName);
        int[][] xyMeasurements = new int[2][3];
        int i = 0;
        int[] origin = new int[] {0, 0, 0};
        for (int time = 0; time < 5; time++) {
          for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) xyMeasurements[x][y] = i++;
          Array data = ucar.ma2.Array.makeFromJavaArray(xyMeasurements);
          origin[0] = time;
          w.writeArrayData(v, Array.makeArrayRankPlusOne(data), origin);
        }
      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  /**
   * test using NetcdfWritehandle to write a TXY 3d array in 1d vectors: write a Y 1d vector set for
   * 1 write each TX
   *
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  void test_build_write_in_parts2() throws IOException, URISyntaxException {
    String filename = "test_build_write_in_parts2";
    String resourceName = "/netcdf/test_build_write_in_parts.nc";
    String group = "three/d/intime";
    VariableName timeName = new VariableName("time", group);
    VariableName xName = new VariableName("X", group);
    VariableName yName = new VariableName("Y", group);

    VariableName tempName = new VariableName("temperature", group);

    CoordinateVariableDefinition timedim =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {1640995200, 1640995201, 1640995202, 1640995203, 1640995204},
            "",
            "seconds since 01-01-1970",
            "");
    CoordinateVariableDefinition xdim =
        new CoordinateVariableDefinition(xName, new int[] {2, 4}, "my x axis", "cm", "");
    CoordinateVariableDefinition ydim =
        new CoordinateVariableDefinition(yName, new int[] {3, 6, 9}, "my y axis", "cm", "");
    DimensionalVariableDefinition nadef =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {timeName, xName, yName},
            "a test dataset with temperatures in time and space",
            "C",
            "");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(timedim);
      b.prepare(xdim);
      b.prepare(ydim);
      b.prepare(nadef);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(timedim);
        w.writeDimensionVariable(xdim);
        w.writeDimensionVariable(ydim);
        Variable v = w.getVariable(nadef.getVariableName());
        int[] origin = new int[] {0, 0, 0};
        int[] yMeasurements = new int[3];
        int i = 0;
        for (int time = 0; time < 5; time++)
          for (int x = 0; x < 2; x++) {
            origin[0] = time;
            origin[1] = x;
            for (int y = 0; y < 3; y++) yMeasurements[y] = i++;
            Array data = ucar.ma2.Array.makeFromJavaArray(yMeasurements);
            w.writeArrayData(
                v, Array.makeArrayRankPlusOne(Array.makeArrayRankPlusOne(data)), origin);
          }
      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void sharedDimension() throws IOException, URISyntaxException {
    String filename = "sharedDimension";
    String resourceName = "/netcdf/sharedDimension.nc";

    String group_time = "time";
    String group_temp = group_time + "/temp";

    VariableName timeName = new VariableName("time", group_time);
    VariableName xName = new VariableName("X", group_temp);
    VariableName yName = new VariableName("Y", group_temp);
    VariableName tempName = new VariableName("temp", group_temp);

    CoordinateVariableDefinition time =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {12, 13, 14, 15, 16},
            "this is the time dimension that other arrays should link to",
            "seconds (since 01-01-1970)",
            "");

    CoordinateVariableDefinition dim_x =
        new CoordinateVariableDefinition(xName, new int[] {2, 4}, "my x axis", "cm", "");
    CoordinateVariableDefinition dim_y =
        new CoordinateVariableDefinition(yName, new int[] {3, 6, 9}, "my y axis", "cm", "");

    DimensionalVariableDefinition nadef_temp =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {timeName, xName, yName},
            "a test dataset with temperatures in time and space",
            "C",
            "surface temperature");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(time);
      b.prepare(dim_x);
      b.prepare(dim_y);
      b.prepare(nadef_temp);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(time);
        w.writeDimensionVariable(dim_x);
        w.writeDimensionVariable(dim_y);
        Variable v = w.getVariable(nadef_temp.getVariableName());
        int[][] xyMeasurements = new int[2][3];
        int[] origin = new int[] {0, 0, 0};
        int i = 0;
        for (int t = 0; t < 5; t++) {
          origin[0] = t;
          for (int x = 0; x < 2; x++) for (int y = 0; y < 3; y++) xyMeasurements[x][y] = i++;
          Array data = ucar.ma2.Array.makeFromJavaArray(xyMeasurements);
          w.writeArrayData(v, Array.makeArrayRankPlusOne(data), origin);
        }

      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }

  @Test
  void inRoot() throws IOException, URISyntaxException {
    String filename = "inRoot";
    String resourceName = "/netcdf/inRoot.nc";

    String root_group = "";

    VariableName timeName = new VariableName("time", root_group);
    VariableName tempName = new VariableName("temp", root_group);

    CoordinateVariableDefinition time =
        new CoordinateVariableDefinition(
            timeName,
            new int[] {12, 13, 14, 15, 16},
            "this is the time dimension that other arrays should link to",
            "seconds (since 01-01-1970)",
            "");

    DimensionalVariableDefinition nadef_temp =
        new DimensionalVariableDefinition(
            tempName,
            NetcdfDataType.INT,
            new VariableName[] {timeName},
            "a test dataset with temperatures in time",
            "C",
            "");

    Path filePath = Files.createTempFile(filename, ".nc");
    try (NetcdfBuilder b =
        new NetcdfBuilder(
            filePath.toString(), Nc4Chunking.Strategy.standard, 3, true, this.onClose)) {
      b.prepare(time);
      b.prepare(nadef_temp);
      try (NetcdfWriter w = new NetcdfWriter(b, this.onClose)) {
        w.writeDimensionVariable(time);
        Variable v = w.getVariable(nadef_temp.getVariableName());
        int[] temp = new int[] {10, 14, 12, 19, 11};
        w.writeArrayData(v, new NumericalArrayImpl(temp));

      } catch (InvalidRangeException e) {

      }
    }
    Assertions.assertTrue(
        FileUtils.contentEquals(
            filePath.toFile(), Path.of(getClass().getResource(resourceName).toURI()).toFile()));
    FileUtils.delete(filePath.toFile());
  }
}
