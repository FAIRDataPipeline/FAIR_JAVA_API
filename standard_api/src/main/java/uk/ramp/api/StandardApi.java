package uk.ramp.api;

import com.google.common.collect.Table;
import java.lang.ref.Cleaner;
import java.lang.ref.Cleaner.Cleanable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.random.RandomGenerator;
import uk.ramp.config.ImmutableConfigItem;
import uk.ramp.distribution.Distribution;
import uk.ramp.distribution.ImmutableDistribution;
import uk.ramp.objects.NumericalArray;
import uk.ramp.parameters.*;
import uk.ramp.samples.Samples;
import uk.ramp.toml.TOMLMapper;
import uk.ramp.toml.TomlReader;
import uk.ramp.toml.TomlWriter;
import uk.ramp.dataregistry.restclient.RestClient;
import uk.ramp.dataregistry.content.*;

public class StandardApi implements AutoCloseable {
  private static final Cleaner cleaner = Cleaner.create(); // safety net for closing
  private final Cleanable cleanable;
  private final CleanableFileApi fileApi;
  private final ParameterDataReader parameterDataReader;
  private final ParameterDataWriter parameterDataWriter;
  private final RandomGenerator rng;
  private final RestClient restClient;

  public StandardApi(Path configPath, RandomGenerator rng) {
    this(
        new FileApi(configPath),
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
    this.restClient = new RestClient(fileApi.getConfig().run_metadata().local_data_registry_url().orElse("http://localhost:8000/api/"));
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

  public String readLink(String alias) {
    Optional<ImmutableConfigItem> l =
            fileApi.fileApi.getConfig().readItems().stream().filter(ci -> ci.external_object().orElse("") == alias).findFirst();
    Map<String, String> m = new HashMap<String, String>();
    if(l.isPresent()){
      if(l.get().doi_or_unique_name().isPresent()) {
        m.put("doi_or_unique_name", l.get().doi_or_unique_name().orElse(""));
      }
    }
    External_object eo = (External_object) restClient.get(External_object.class, m);

    return "";
  }

  public Number readEstimate(String dataProduct, String component) {
    /*ReadComponent data;
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(query)) {
      data = parameterDataReader.read(fileChannel, component);
    }
    return data.getEstimate();*/
    return 0.5;
  }

  public void writeEstimate(String dataProduct, String component, Number estimateNumber) {
    /*var query =
        ImmutableMetadataItem.builder()
            .dataProduct(dataProduct)
            .component(component)
            .extension("toml")
            .build();
    var estimate = ImmutableEstimate.builder().internalValue(estimateNumber).rng(rng).build();

    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForWrite(query)) {
      parameterDataWriter.write(fileChannel, component, estimate);
    }*/
  }

  public Distribution readDistribution(String dataProduct, String component) {
    /*var query =
        ImmutableMetadataItem.builder().dataProduct(dataProduct).component(component).build();

    ReadComponent data;
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(query)) {
      data = parameterDataReader.read(fileChannel, component);
    }
    return data.getDistribution();*/
    return ImmutableDistribution.builder().build();
  }

  public void writeDistribution(String dataProduct, String component, Distribution distribution) {
    /*var query =
        ImmutableMetadataItem.builder()
            .dataProduct(dataProduct)
            .component(component)
            .extension("toml")
            .build();

    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForWrite(query)) {
      parameterDataWriter.write(fileChannel, component, distribution);
    }*/
  }

  public List<Number> readSamples(String dataProduct, String component) {
    /*var query =
        ImmutableMetadataItem.builder().dataProduct(dataProduct).component(component).build();

    ReadComponent data;
    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForRead(query)) {
      data = parameterDataReader.read(fileChannel, component);
    }
    return data.getSamples();*/
    return List.of(0.4, 0.5);
  }

  public void writeSamples(String dataProduct, String component, Samples samples) {
    /*var query =
        ImmutableMetadataItem.builder()
            .dataProduct(dataProduct)
            .component(component)
            .extension("toml")
            .build();

    try (CleanableFileChannel fileChannel = fileApi.fileApi.openForWrite(query)) {
      parameterDataWriter.write(fileChannel, component, samples);
    }*/
  }

  public NumericalArray readArray(String dataProduct, String component) {
    throw new UnsupportedOperationException();
  }

  public void writeTable(
      String dataProduct, String component, Table<Integer, String, Number> table) {
    throw new UnsupportedOperationException();
  }

  public Table<Integer, String, Number> readTable(String dataProduct, String component) {
    throw new UnsupportedOperationException();
  }

  public void writeArray(String dataProduct, String component, Number[] arr) {
    throw new UnsupportedOperationException();
  }

  public void add_to_register(String name) {
    // fileApi.add_to_register(name);
  }

  @Override
  public void close() {
    cleanable.clean();
  }
}
