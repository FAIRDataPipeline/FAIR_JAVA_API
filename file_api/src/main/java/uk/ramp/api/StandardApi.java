package uk.ramp.api;

// import com.google.common.collect.Table;
import org.apache.commons.math3.random.RandomGenerator;
import uk.ramp.dataregistry.content.*;
import uk.ramp.parameters.*;
import uk.ramp.toml.TOMLMapper;
import uk.ramp.toml.TomlReader;
import uk.ramp.toml.TomlWriter;

/**
 * StandardApi helper for the fileApi
 */
public class StandardApi {
  protected final ParameterDataReader parameterDataReader;
  protected final ParameterDataWriter parameterDataWriter;
  private final RandomGenerator rng;

  StandardApi(RandomGenerator rng) {
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
