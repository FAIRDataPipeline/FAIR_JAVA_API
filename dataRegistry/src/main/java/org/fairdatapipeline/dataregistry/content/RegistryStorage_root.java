package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryStorage_root extends Registry_Updateable {
  @XmlElement private String root;

  @XmlElement
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private Boolean local;

  @XmlElement private List<String> locations;

  public RegistryStorage_root() {}

  public RegistryStorage_root(String root) {
    this.root = root;
  }

  public String getRoot() {
    return this.root;
  }

  public Path getPath() {
    return Paths.get(this.root);
  }

  public Boolean getLocal() {
    return this.local;
  }

  public List<String> getLocations() {
    return (this.locations == null) ? new ArrayList<>() {} : new ArrayList<>(this.locations);
  }

  public void setRoot(String root) {
    this.root = root;
  }

  public void setLocal(Boolean local) {
    this.local = local;
  }

  public void setLocations(List<String> locations) {
    this.locations = new ArrayList<String>(locations);
  }
}
