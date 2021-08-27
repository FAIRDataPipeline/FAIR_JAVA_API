package org.fairdatapipeline.api;

import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.parameters.ParameterDataReader;
import org.fairdatapipeline.parameters.ParameterDataReaderImpl;
import org.fairdatapipeline.parameters.ParameterDataWriter;
import org.fairdatapipeline.parameters.ParameterDataWriterImpl;
import org.fairdatapipeline.toml.TOMLMapper;
import org.fairdatapipeline.toml.TomlReader;
import org.fairdatapipeline.toml.TomlWriter;

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
