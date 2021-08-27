package org.fairdatapipeline.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fairdatapipeline.dataregistry.content.RegistryIssue;

/**
 * An issue that can be raised with objects or their components.
 *
 * Issues can be created using {@link FileApi#raise_issue(String, Integer)} or with {@link Object_component#raise_issue(String, Integer)}.
 *
 * <p>
 *     <b>Usage example</b>
 *     <blockquote><pre>
 *       Object_component_write oc1 = dp.getComponent(component1);
 *       oc1.raise_issue("something is terribly wrong with this component", 10);
 *     </pre></blockquote>
 *     or
 *     <blockquote><pre>
 *      Issue i = FileApi.raise_issue("moderately bad data", 7);
 *      i.add_components(oc1, oc2, oc3);
 *     </pre></blockquote>
 */
public class Issue {
  RegistryIssue registryIssue;
  String description;
  Integer severity;
  List<Object_component> components;

  Issue(String description, Integer severity) {
    this.description = description;
    this.severity = severity;
    this.registryIssue = new RegistryIssue(description, severity);
    this.components = new ArrayList<>();
  }

  RegistryIssue getRegistryIssue() {
    this.components.stream()
        .forEach(
            component ->
                this.registryIssue.addComponent_issue(component.registryObject_component.getUrl()));
    return registryIssue;
  }

  /**
   * When we have created an issue using FileApi.raise_issue() we still need to attach this issue to one or more
   * object components. Note: attach the issue to the 'whole_object' component to indicate that it is linked to
   * the object rather than a specific component.
   *
   * @param components - any number of components can be listed as arguments
   */
  public void add_components(Object_component... components) {
    Arrays.stream(components)
        .forEach(
            component -> {
              this.components.add(component);
            });
  }
}
