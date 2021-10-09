package org.fairdatapipeline.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** A quality issue that can be attached to any Object or ObjectComponent. */
@XmlRootElement
public class RegistryIssue extends Registry_Updateable {

  @XmlElement private Integer severity;

  @XmlElement private String description;

  @XmlElement private List<APIURL> component_issues;

  @XmlElement private String uuid;

  /** Empty constructor */
  public RegistryIssue() {
    this.methods_allowed = List.of("GET", "PUT", "PATCH", "HEAD", "OPTIONS");
  }

  /**
   * Constructor
   *
   * @param description Free text description of the Issue.
   * @param severity Severity of this Issue as an integer, the larger the value the more severe the
   *     Issue.
   */
  public RegistryIssue(String description, Integer severity) {
    this();
    this.description = description;
    this.severity = severity;
  }

  /**
   * Severity of this Issue as an integer, the larger the value the more severe the Issue.
   *
   * @return Severity of this Issue as an integer, the larger the value the more severe the Issue.
   */
  public Integer getSeverity() {
    return this.severity;
  }

  /**
   * Free text description of the Issue.
   *
   * @return Free text description of the Issue.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * List of ObjectComponent {@link APIURL APIURLs} which the Issue is associated with.
   *
   * @return List of ObjectComponent {@link APIURL APIURLs} which the Issue is associated with.
   */
  public List<APIURL> getComponent_issues() {
    return (this.component_issues == null)
        ? new ArrayList<>() {}
        : new ArrayList<>(this.component_issues);
  }

  /**
   * (optional): UUID of the Issue. If not specified a UUID is generated automatically.
   *
   * @return UUID of the Issue.
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * Severity of this Issue as an integer, the larger the value the more severe the Issue.
   *
   * @param severity Severity of this Issue as an integer, the larger the value the more severe the
   *     Issue.
   */
  public void setSeverity(Integer severity) {
    this.severity = severity;
  }

  /**
   * Free text description of the Issue.
   *
   * @param description Free text description of the Issue.
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Set the list of Object_components associated with this Issue.
   *
   * @param component_issues The list of Object_components associated with this Issue.
   */
  public void setComponent_issues(List<APIURL> component_issues) {
    this.component_issues = new ArrayList<>(component_issues);
  }

  /**
   * Associate a Object_component with this Issue.
   *
   * @param object_component_url The RegistryObject_component APIURL to add to this Issue
   */
  public void addComponent_issue(APIURL object_component_url) {
    if (this.component_issues == null) this.component_issues = new ArrayList<>();
    if (object_component_url != null && !this.component_issues.contains(object_component_url)) {
      this.component_issues.add(object_component_url);
    }
  }

  /** @param uuid UUID of the Issue. If not specified a UUID is generated automatically. */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
