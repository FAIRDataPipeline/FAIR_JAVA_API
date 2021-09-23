package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryCode_repo_release extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String version;

  @XmlElement private URL website;

  @XmlElement private URL object;

  public RegistryCode_repo_release() {}

  public String getName() {
    return this.name;
  }

  public String getVersion() {
    return this.version;
  }

  public URL getWebsite() {
    return this.website;
  }

  public URL getObject() {
    return this.object;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setWebsite(URL website) {
    this.website = website;
  }

  public void setObject(URL object) {
    this.object = object;
  }
}
