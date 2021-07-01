package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Author extends FDP_Updateable {
  @XmlElement private String family_name;

  @XmlElement private String given_name;

  @XmlElement private String identifier;

  @XmlElement private String uuid;

  public Author() {}

  public String getFamily_name() {
    return this.family_name;
  }

  public String getGiven_name() {
    return this.given_name;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public String getUuid() {
    return this.uuid;
  }

  public void setFamily_name(String family_name) {
    this.family_name = family_name;
  }

  public void setGiven_name(String given_name) {
    this.given_name = given_name;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
