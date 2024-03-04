package org.fairdatapipeline.samples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.stream.Collectors;

import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.distribution.ImmutableDistribution;
import org.fairdatapipeline.parameters.ImmutableNumberList;
import org.fairdatapipeline.parameters.NumberList;
import org.fairdatapipeline.parameters.RngComponent;
import org.immutables.value.Value;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@JsonDeserialize
public interface Samples extends RngComponent {
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

  @Value.Check
  default Samples avoidHeterogeneous() {
    // count the number of integers in this list:
    int i = (int) samples().stream().filter((x) -> ((Number) x.intValue()) == x).count();
    if (i != 0 && i < samples().size()) {
      return ImmutableSamples.builder().samples(samples().stream().map(Number::doubleValue).collect(Collectors.toList())).rng(rng()).build();
    }
    return this;
  }
}
