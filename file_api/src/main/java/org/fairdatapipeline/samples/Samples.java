package org.fairdatapipeline.samples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;

import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.parameters.Component;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
public interface Samples extends Component {
  List<Number> samples();

  @JsonIgnore
  default Number mean() {
    return samples().stream().mapToDouble(Number::doubleValue).average().orElseThrow();
  }

  @Override
  @JsonIgnore
  default Number getEstimate() {
    return mean();
  }

  @Override
  @JsonIgnore
  default List<Number> getSamples() {
    return samples();
  }

  @Override
  @JsonIgnore
  default Distribution getDistribution() {
    return ImmutableDistribution.builder()
        .internalType(Distribution.DistributionType.empirical)
        .empiricalSamples(samples())
        .rng(rng())
        .build();
  }
}
