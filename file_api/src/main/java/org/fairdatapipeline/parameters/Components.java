package org.fairdatapipeline.parameters;

import java.util.Map;
import org.immutables.value.Value.Immutable;

@Immutable
public interface Components {
  Map<String, Component> components();
}
