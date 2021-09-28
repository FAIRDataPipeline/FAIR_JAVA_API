package org.fairdatapipeline.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * A component of a Object being used as the input to a CodeRun or produced as an output from a
 * CodeRun
 */
@XmlRootElement
public class RegistryObject_component extends Registry_Updateable {
  @XmlElement private APIURL object;

  @XmlElement private String name;

  @XmlElement private String description;

  @XmlElement private boolean whole_object;

  @XmlElement private List<APIURL> issues;

  @XmlElement private List<APIURL> inputs_of;

  @XmlElement private List<APIURL> outputs_of;

  /** Empty constructor */
  public RegistryObject_component() {}

  /**
   * Constructor
   *
   * @param name Name of the ObjectComponent, unique in the context of ObjectComponent and its
   *     Object reference.
   */
  public RegistryObject_component(String name) {
    this.name = name;
  }

  /**
   * The APIURL of the Object to associate this ObjectComponent with.
   *
   * @return The APIURL of the Object associated with this ObjectComponent.
   */
  public APIURL getObject() {
    return this.object;
  }

  /**
   * Name of the ObjectComponent, unique in the context of ObjectComponent and its Object reference.
   *
   * @return Name of the ObjectComponent, unique in the context of ObjectComponent and its Object
   *     reference.
   */
  public String getName() {
    return this.name;
  }

  /**
   * (optional): Free text description of the ObjectComponent.
   *
   * @return Free text description of the ObjectComponent.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * (optional): Specifies if this ObjectComponent refers to the whole object or not (by default
   * this is False)
   *
   * @return If this ObjectComponent refers to the whole object or not.
   */
  public boolean isWhole_object() {
    return this.whole_object;
  }

  /**
   * (optional): List of Issues APIURLs to associate with this ObjectComponent.
   *
   * @return List of Issues APIURLs to associate with this ObjectComponent.
   */
  public List<APIURL> getIssues() {
    return (this.issues == null) ? new ArrayList<>() {} : new ArrayList<>(this.issues);
  }

  /**
   * List of CodeRun APIURLs that the ObjectComponent is being used as an input to.
   *
   * @return List of CodeRun APIURLs that the ObjectComponent is being used as an input to.
   */
  public List<APIURL> getInputs_of() {
    return (inputs_of == null) ? new ArrayList<>() {} : new ArrayList<>(this.inputs_of);
  }

  /**
   * List of CodeRun APIURLs that the ObjectComponent was created as an output of.
   *
   * @return List of CodeRun APIURLs that the ObjectComponent was created as an output of.
   */
  public List<APIURL> getOutputs_of() {
    return (outputs_of == null) ? new ArrayList<>() {} : new ArrayList<>(this.outputs_of);
  }

  /** @param object The APIURL of the Object to associate this ObjectComponent with. */
  public void setObject(APIURL object) {
    this.object = object;
  }

  /**
   * @param name Name of the ObjectComponent, unique in the context of ObjectComponent and its
   *     Object reference.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** @param description (optional): Free text description of the ObjectComponent. */
  public void setDescription(String description) {
    this.description = description;
  }

  /** @param whole_object If this ObjectComponent refers to the whole object or not. */
  public void setWhole_object(boolean whole_object) {
    this.whole_object = whole_object;
  }
}
