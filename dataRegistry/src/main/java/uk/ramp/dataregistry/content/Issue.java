package uk.ramp.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Issue extends FDP_Updateable {
  @XmlElement private Integer severity;

  @XmlElement private String description;

  @XmlElement private List<String> component_issues;

  @XmlElement private String uuid;

  public Issue() {}

  public Issue(String description, Integer severity) {
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

  public void addComponent_issue(String component) {
    if(this.component_issues == null) this.component_issues = new ArrayList<>();
    this.component_issues.add(component);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
