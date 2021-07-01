package uk.ramp.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Object_component extends FDP_Updateable {
  @XmlElement private String object;

  @XmlElement private String name;

  @XmlElement private String description;

  @XmlElement private boolean whole_object;

  @XmlElement private List<String> issues;

  @XmlElement private List<String> inputs_of;

  @XmlElement private List<String> outputs_of;

  public String getObject() {
    return this.object;
  }

  public String getName() {
    return this.name;
  }

  public String getDescription() {
    return this.description;
  }

  public boolean isWhole_object() {
    return this.whole_object;
  }

  public List<String> getIssues() {
    return (this.issues == null) ? new ArrayList<>() {} : new ArrayList<String>(this.issues);
  }

  public List<String> getInputs_of() {
    return (inputs_of == null) ? new ArrayList<>() {} : new ArrayList<String>(this.inputs_of);
  }

  public List<String> getOutputs_of() {
    return (outputs_of == null) ? new ArrayList<>() {} : new ArrayList<String>(this.outputs_of);
  }

  public void setObject(String object) {
    this.object = object;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setWhole_object(boolean whole_object) {
    this.whole_object = whole_object;
  }

  public void setIssues(List<String> issues) {
    this.issues = new ArrayList<>(issues);
  }

  public void setInputs_of(List<String> inputs_of) {
    this.inputs_of = new ArrayList<>(inputs_of);
  }

  public void setOutputs_of(List<String> outputs_of) {
    this.outputs_of = new ArrayList<>(outputs_of);
  }
}
