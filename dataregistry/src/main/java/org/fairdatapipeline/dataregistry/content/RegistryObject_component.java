package org.fairdatapipeline.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class RegistryObject_component extends Registry_Updateable {
  @XmlElement private APIURL object;

  @XmlElement private String name;

  @XmlElement private String description;

  @XmlElement private boolean whole_object;

  @XmlElement private List<APIURL> issues;

  @XmlElement private List<APIURL> inputs_of;

  @XmlElement private List<APIURL> outputs_of;

  public RegistryObject_component() {}

  public RegistryObject_component(String name) {
    this.name = name;
  }

  public RegistryObject_component(boolean whole_object) {
    this.whole_object = whole_object;
  }

  public RegistryObject_component(APIURL object, String name) {
    this.object = object;
    this.name = name;
  }

  public RegistryObject_component(APIURL object, boolean whole_object) {
    this.object = object;
    this.whole_object = whole_object;
  }

  @Override
  public APIURL getUrl() {
    return super.getUrl();
  }

  public APIURL getObject() {
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

  public List<APIURL> getIssues() {
    return (this.issues == null) ? new ArrayList<>() {} : new ArrayList<>(this.issues);
  }

  public List<APIURL> getInputs_of() {
    return (inputs_of == null) ? new ArrayList<>() {} : new ArrayList<>(this.inputs_of);
  }

  public List<APIURL> getOutputs_of() {
    return (outputs_of == null) ? new ArrayList<>() {} : new ArrayList<>(this.outputs_of);
  }

  public void setObject(APIURL object) {
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

  public void setIssues(List<APIURL> issues) {
    this.issues = new ArrayList<>(issues);
  }

  public void setInputs_of(List<APIURL> inputs_of) {
    this.inputs_of = new ArrayList<>(inputs_of);
  }

  public void setOutputs_of(List<APIURL> outputs_of) {
    this.outputs_of = new ArrayList<>(outputs_of);
  }
}
