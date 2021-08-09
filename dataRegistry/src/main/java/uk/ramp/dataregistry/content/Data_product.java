package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Data_product extends FDP_Updateable {
  @XmlElement private String name;

  @XmlElement private String version;

  @XmlElement private String object;

  @XmlElement private String namespace;

  @XmlElement private String external_object;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @XmlElement
  private boolean internal_format;

  public Data_product() {}

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public String getObject() {
    return object;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getExternal_object() {
    return external_object;
  }

  public boolean isInternal_format() {
    return this.internal_format;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public void setExternal_object(String external_object) {
    this.external_object = external_object;
  }

  public void setInternal_format(boolean internal_format) {
    this.internal_format = internal_format;
  }
}
