package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Author extends FDP_Updateable {
  @XmlElement private String name;

  @XmlElement private String identifier;

  @XmlElement private String uuid;

  public Author() {}

  public String getName() {
    return this.name;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public String getUuid() {
    return this.uuid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
