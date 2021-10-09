package org.fairdatapipeline.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.fairdatapipeline.dataregistry.content.RegistryIssue;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * An issue that can be raised with objects or their components.
 *
 * <p>Issues can be created using {@link Coderun#raise_issue(String, Integer)} or with {@link
 * Object_component#raise_issue(String, Integer)}.
 *
 * <p><b>Usage example</b>
 *
 * <blockquote>
 *
 * <pre>
 *       Object_component_write oc1 = dp.getComponent(component1);
 *       oc1.raise_issue("something is terribly wrong with this component", 10);
 * </pre>
 *
 * </blockquote>
 *
 * or
 *
 * <blockquote>
 *
 * <pre>
 *      Issue i = coderun.raise_issue("moderately bad data", 7);
 *      i.add_components(oc1, oc2, oc3);
 * </pre>
 *
 * </blockquote>
 *
 * or
 *
 * <blockquote>
 *
 * <pre>
 *      Issue i = coderun.raise_issue("moderately bad data", 7);
 *      i.add_components(oc1, oc2, oc3);
 *      i.add_fileObjects(coderun.getCode_repo(), coderun.getScript(), coderun.getConfig());
 * </pre>
 *
 * </blockquote>
 */
public class Issue {
  RegistryIssue registryIssue;
  String description;
  Integer severity;
  List<Object_component> components;

  /**
   * Constructor is not public, for issues should be created by coderun.
   *
   * @param description the text (description) for this issue
   * @param severity an integer indicating the severity of the issue - high is more severe
   */
  Issue(String description, Integer severity) {
    this.description = description;
    this.severity = severity;
    this.registryIssue = new RegistryIssue(description, severity);
    this.components = new ArrayList<>();
  }

  /**
   * at the end of coderun the obj_components have been registered, and the registryIssue can
   * receive the obj_component URLs to put in its component_issues List.
   *
   * @return the registryIssue with its Component_issues filled in
   */
  RegistryIssue getRegistryIssue() {
    this.components.forEach(
        component ->
            this.registryIssue.addComponent_issue(component.registryObject_component.getUrl()));
    return registryIssue;
  }

  /**
   * When we have created an issue using coderun.raise_issue() we still need to attach this issue to
   * one or more object components. Note: attach the issue to the 'whole_object' component to
   * indicate that it is linked to the object rather than a specific component.
   *
   * @param components - any number of components can be listed as arguments
   */
  public void add_components(Object_component... components) {
    this.components.addAll(Arrays.asList(components));
  }

  /**
   * When we have created an issue using coderun.raise_issue() we can also attach FileObjects to
   * this issue. This can be used to raise issues with Code_repo, Submission Script, and/or Config
   * File.
   *
   * @param objects any number of FileObjects, e.g. coderun.getScript(), coderun.getConfig(),
   *     coderun.getCode_repo()
   */
  public void add_fileObjects(FileObject... objects) {
    Arrays.asList(objects)
        .forEach(
            object -> this.registryIssue.addComponent_issue(object.getWholeObjectComponentUrl()));
  }

  void add_registryObject_component(APIURL objectComponentUrl) {
    this.registryIssue.addComponent_issue(objectComponentUrl);
  }
}
