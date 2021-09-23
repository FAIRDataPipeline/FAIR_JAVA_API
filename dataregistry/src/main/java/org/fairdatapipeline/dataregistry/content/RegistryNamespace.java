package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryNamespace extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String full_name;

  @XmlElement private URL website;

  public RegistryNamespace() {}

  public RegistryNamespace(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public String getFull_name() {
    return this.full_name;
  }

  public URL getWebsite() {
    return this.website;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }

  public void setWebsite(URL website) {
    this.website = website;
  }
}
