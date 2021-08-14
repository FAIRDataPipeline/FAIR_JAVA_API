package uk.ramp.api;

import com.google.common.collect.Table;
import java.io.IOException;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import uk.ramp.dataregistry.content.*;
import uk.ramp.distribution.Distribution;
import uk.ramp.estimate.ImmutableEstimate;
import uk.ramp.file.CleanableFileChannel;
import uk.ramp.objects.NumericalArray;
import uk.ramp.parameters.*;
import uk.ramp.samples.Samples;
import uk.ramp.toml.TOMLMapper;
import uk.ramp.toml.TomlReader;
import uk.ramp.toml.TomlWriter;

/**
 * Java implementation of Data Pipeline Standard API.
 *
 * <p>Standard API knows about Data Pipeline data such as distributions, arrays, parameter.
 *
 * <p>It uses File API only for dealing with the YAML config file, and for its Hasher.
 *
 * <p>The distinction between File API and Standard API comes from the previous version of the Data
 * Pipeline API; i'm not sure if we should change this.
 *
 * <p>Standard API uses dataregistry.restclient.RestClient for interacting with the local registry.
 *
 * <p>Access
 */
public class StandardApi implements AutoCloseable {
  private static final Cleaner cleaner = Cleaner.create(); // safety net for closing
  private final Cleanable cleanable;
  private final CleanableFileApi fileApi;
  private final ParameterDataReader parameterDataReader;
  private final ParameterDataWriter parameterDataWriter;
  private final RandomGenerator rng;

  public StandardApi(Path configPath, Path scriptPath, RandomGenerator rng) {
    this(
        new FileApi(configPath, scriptPath),
        new ParameterDataReaderImpl(new TomlReader(new TOMLMapper(rng))),
        new ParameterDataWriterImpl(new TomlWriter(new TOMLMapper(rng))),
        rng);
  }

  StandardApi(
      FileApi fileApi,
      ParameterDataReader parameterDataReader,
      ParameterDataWriter parameterDataWriter,
      RandomGenerator rng) {
    this.fileApi = new CleanableFileApi(fileApi);
    this.parameterDataReader = parameterDataReader;
    this.parameterDataWriter = parameterDataWriter;
    this.rng = rng;
    this.cleanable = cleaner.register(this, this.fileApi);
  }

  private static class CleanableFileApi implements Runnable {
    private final FileApi fileApi;

    CleanableFileApi(FileApi fileApi) {
      this.fileApi = fileApi;
    }

    @Override
    public void run() {
      fileApi.close();
    }
  }

  /*
   *
   */
  public CleanableFileChannel readLink(String dataProduct) {
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(dataProduct)) {
      return fileChannel;
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open the file."));
    }
  }

  public CleanableFileChannel writeLink(String dataProduct) {
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForWrite(dataProduct)) {
      return fileChannel;
    }/* catch (IOException e) {
      throw (new IllegalArgumentException("failed to open the file."));
    }*/
  }

  public Number readEstimate(String dataProduct, String component) {
    System.out.println("standardApi.readEstimate(" + dataProduct + ", " + component + ")");
    ReadComponent data;
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(dataProduct, component)) {
      System.out.println("\n\nreadEstimate\n\nopened the fileChannel - trying to read component");
      data = parameterDataReader.read(fileChannel, component);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open the file."));
    }
    if(data == null) {
      System.out.println("parameterDataReader.read() returned NULL");
    }
    return data.getEstimate();
  }

  public void writeEstimate(String dataProduct, String component, Number estimateNumber) {
    var estimate = ImmutableEstimate.builder().internalValue(estimateNumber).rng(rng).build();

    try (CleanableFileChannel fileChannel =
        fileApi.fileApi.openForWrite(dataProduct, component, "toml")) {
      parameterDataWriter.write(fileChannel, component, estimate);
    }/* catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file" + e.toString()));
    }*/
  }

  public Distribution readDistribution(String dataProduct, String component) {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(dataProduct, component)) {
      data = parameterDataReader.read(fileChannel, component);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for read " + e.toString()));
    }
    return data.getDistribution();
  }

  public void writeDistribution(String dataProduct, String component, Distribution distribution) {
    try (CleanableFileChannel fileChannel =
        fileApi.fileApi.openForWrite(dataProduct, component, "toml")) {
      parameterDataWriter.write(fileChannel, component, distribution);
    }/* catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for write " + e.toString()));
    }*/
  }

  public List<Number> readSamples(String dataProduct, String component) {
    ReadComponent data;
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(dataProduct, component)) {
      data = parameterDataReader.read(fileChannel, component);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for read " + e.toString()));
    }
    return data.getSamples();
  }

  /*public void writeSamples(String dataProduct, String component, Samples samples) {
    try (CleanableFileChannel fileChannel =
        fileApi.fileApi.openForWrite(dataProduct, component, "toml")) {
      parameterDataWriter.write(fileChannel, component, samples);
    } catch (IOException e) {
      throw (new IllegalArgumentException("failed to open file for write " + e.toString()));
    }
  }*/

  public void writeSamples(String dataProduct, String component, Samples samples) {
    CleanableFileChannel fileChannel =
                 fileApi.fileApi.openForWrite(dataProduct, component, "toml");
    System.out.println("about to write to filechannel");
    if(fileChannel == null) System.out.println("filechannel is NULL!!");
    else {
      parameterDataWriter.write(fileChannel, component, samples);
      System.out.println("written to filechannel");
    }

  }

  public NumericalArray readArray(String dataProduct, String component) {
    Path filepath = fileApi.fileApi.getFilePathForRead(dataProduct, component);
    // HDF5 hdf5 = new HDF5(filepath);
    return null; // hdf5.read(component);
  }

  public void writeTable(
      String dataProduct, String component, Table<Integer, String, Number> table) {
    throw new UnsupportedOperationException();
  }

  public Table<Integer, String, Number> readTable(String dataProduct, String component) {
    throw new UnsupportedOperationException();
  }

  public void writeArray(String dataProduct, String component, Number[] arr) {
    Path filepath = fileApi.fileApi.getFilePathForWrite(dataProduct, component, "h5");
    // HDF5 hdf5 = new HDF5(filepath);
    // hdf5.write(component, arr);
  }

  public void add_to_register(String name) {
    // fileApi.add_to_register(name);
  }

  @Override
  public void close() {
    cleanable.clean();
  }
}
