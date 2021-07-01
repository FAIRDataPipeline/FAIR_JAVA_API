package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Namespace extends FDP_Updateable {
  @XmlElement private String name;

  @XmlElement private String full_name;

  @XmlElement private String website;

  public Namespace() {}

  public Namespace(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public String getFull_name() {
    return this.full_name;
  }

  public String getWebsite() {
    return this.website;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }

  public void setWebsite(String website) {
    this.website = website;
  }
}
