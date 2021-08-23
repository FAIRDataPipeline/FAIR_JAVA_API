package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryCode_repo_release extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String version;

  @XmlElement private String website;

  @XmlElement private String object;

  public RegistryCode_repo_release() {}

  public String getName() {
    return this.name;
  }

  public String getVersion() {
    return this.version;
  }

  public String getWebsite() {
    return this.website;
  }

  public String getObject() {
    return this.object;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public void setObject(String object) {
    this.object = object;
  }
}
