package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryIssue extends Registry_Updateable {

  @XmlElement private Integer severity;

  @XmlElement private String description;

  @XmlElement private List<String> component_issues;

  @XmlElement private String uuid;

  public RegistryIssue() {
    this.methods_allowed = List.of("GET", "PUT", "PATCH", "HEAD", "OPTIONS");
  }

  public RegistryIssue(String description, Integer severity) {
    this();
    this.description = description;
    this.severity = severity;
  }

  public Integer getSeverity() {
    return this.severity;
  }

  public String getDescription() {
    return this.description;
  }

  public List<String> getComponent_issues() {
    return (this.component_issues == null)
        ? new ArrayList<String>() {}
        : new ArrayList<String>(this.component_issues);
  }

  public String getUuid() {
    return this.uuid;
  }

  public void setSeverity(Integer severity) {
    this.severity = severity;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setComponent_issues(List<String> component_issues) {
    this.component_issues = new ArrayList<String>(component_issues);
  }

  public void addComponent_issue(String object_component_url) {
    if (this.component_issues == null) this.component_issues = new ArrayList<>();
    if (object_component_url != null && !this.component_issues.contains(object_component_url)) {
      this.component_issues.add(object_component_url);
    }
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
