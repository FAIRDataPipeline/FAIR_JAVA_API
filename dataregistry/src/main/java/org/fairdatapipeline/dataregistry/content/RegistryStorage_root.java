package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryStorage_root extends Registry_Updateable {
  @XmlElement private URI root;

  @XmlElement
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private boolean local;

  @XmlElement private List<URL> locations;

  public RegistryStorage_root() {}

  public RegistryStorage_root(URI root) {
    this.root = root;
  }

  public URI getRoot() {
    return this.root;
  }

  @JsonIgnore
  public Path getPath() {
    return Path.of(new File(getRoot()).getAbsolutePath());
  }

  public boolean isLocal() {
    return this.local;
  }

  public List<URL> getLocations() {
    return (this.locations == null) ? new ArrayList<>() {} : new ArrayList<>(this.locations);
  }

  public void setRoot(URI root) {
    this.root = root;
  }

  public void setLocal(boolean local) {
    this.local = local;
  }

  public void setLocations(List<URL> locations) {
    this.locations = new ArrayList<>(locations);
  }
}
