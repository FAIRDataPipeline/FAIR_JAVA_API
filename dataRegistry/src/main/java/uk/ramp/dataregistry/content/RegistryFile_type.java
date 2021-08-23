package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryFile_type extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String extension;

  public RegistryFile_type() {};

  public RegistryFile_type(String name, String extension) {
    this.name = name;
    this.extension = extension;
  }

  public String getName() {
    return name;
  }

  public String getExtension() {
    return extension;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }
}
