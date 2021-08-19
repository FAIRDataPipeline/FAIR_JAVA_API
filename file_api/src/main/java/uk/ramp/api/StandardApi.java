package uk.ramp.api;

// import com.google.common.collect.Table;
import org.apache.commons.math3.random.RandomGenerator;
import uk.ramp.dataregistry.content.*;
import uk.ramp.parameters.*;
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
public class StandardApi {
  protected final ParameterDataReader parameterDataReader;
  protected final ParameterDataWriter parameterDataWriter;
  private final RandomGenerator rng;

  public StandardApi(RandomGenerator rng) {
    this(
        new ParameterDataReaderImpl(new TomlReader(new TOMLMapper(rng))),
        new ParameterDataWriterImpl(new TomlWriter(new TOMLMapper(rng))),
        rng);
  }

  StandardApi(
      ParameterDataReader parameterDataReader,
      ParameterDataWriter parameterDataWriter,
      RandomGenerator rng) {
    this.parameterDataReader = parameterDataReader;
    this.parameterDataWriter = parameterDataWriter;
    this.rng = rng;
  }
}
