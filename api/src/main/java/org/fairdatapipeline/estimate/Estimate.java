package org.fairdatapipeline.estimate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.fairdatapipeline.distribution.Distribution;
import org.fairdatapipeline.parameters.Component;
import org.immutables.value.Value.Immutable;

/** a component to store a plain simple single Number */
@Immutable
@JsonDeserialize
@JsonSerialize
public interface Estimate extends Component {
  /** @return Number - the value that is stored in this component */
  @JsonProperty("value")
  Number internalValue();

  @Override
  @JsonIgnore
  default Number getEstimate() {
    return internalValue();
  }

  @Override
  @JsonIgnore
  default List<Number> getSamples() {
    throw new UnsupportedOperationException(
        "Cannot produce list of samples from an estimate parameter");
  }

  @Override
  @JsonIgnore
  default Distribution getDistribution() {
    throw new UnsupportedOperationException(
        "Cannot produce a distribution from an estimate parameter");
  }
}
