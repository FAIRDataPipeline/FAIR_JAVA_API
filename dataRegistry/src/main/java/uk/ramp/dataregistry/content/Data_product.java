package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Data_product extends FDP_Updateable {
  @XmlElement private String name;

  @XmlElement private String version;

  @XmlElement private String object;

  @XmlElement private String namespace;

  @XmlElement private List<String> external_objects;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @XmlElement private boolean internal_format;

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

  public List<String> getExternal_objects() { return (this.external_objects == null) ? new ArrayList<>() {} : new ArrayList<>(this.external_objects);}

  public boolean isInternal_format() { return this.internal_format; }

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

  public void setExternal_objects(List<String> external_objects) { this.external_objects = new ArrayList<>(external_objects); }

  public void setInternal_format(boolean internal_format) { this.internal_format = internal_format; }
}
