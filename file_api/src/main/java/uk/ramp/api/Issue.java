package uk.ramp.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ramp.dataregistry.content.RegistryIssue;
import uk.ramp.dataregistry.content.RegistryObject_component;

public class Issue {
  RegistryIssue registryIssue;
  String description;
  Integer severity;
  List<Object_component_RW> components;

  Issue(String description, Integer severity) {
    this.description = description;
    this.severity = severity;
    this.registryIssue = new RegistryIssue(description, severity);
    this.components = new ArrayList<>();
  }

  public RegistryIssue getRegistryIssue() {
    this.components.stream().forEach(component -> this.registryIssue.addComponent_issue(component.registryObject_component.getUrl()));
    return registryIssue;
  }

  public void add_components(Object_component_RW... components) {
    Arrays.stream(components)
        .forEach(
            component -> {
              this.components.add(component);
            });
  }
}
