package org.fairdatapipeline.toml;

import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.mapper.DataPipelineMapper;

public class TOMLMapper extends DataPipelineMapper {
  public TOMLMapper(RandomGenerator rng) {
    super(new TOMLFactory(rng), rng);
  }
}
