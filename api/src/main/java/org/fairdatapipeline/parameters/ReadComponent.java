package org.fairdatapipeline.parameters;

import java.util.List;
import org.fairdatapipeline.distribution.Distribution;

public interface ReadComponent {
  Number getEstimate();

  List<Number> getSamples();

  Distribution getDistribution();
}
