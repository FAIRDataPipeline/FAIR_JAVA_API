package org.fairdatapipeline.parameters;

import java.util.List;
import org.apache.commons.math3.random.RandomGenerator;
import org.fairdatapipeline.distribution.Distribution;
import org.immutables.value.Value;

public interface RngComponent extends Component {
  @Value.Auxiliary
  RandomGenerator rng();

  Number getEstimate();

  List<Number> getSamples();

  Distribution getDistribution();
}
