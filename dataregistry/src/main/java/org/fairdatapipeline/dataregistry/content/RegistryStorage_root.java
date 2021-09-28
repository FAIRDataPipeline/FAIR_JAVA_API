package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** The root location of a storage cache where model files are stored */
@XmlRootElement
public class RegistryStorage_root extends Registry_Updateable {
  @XmlElement private URI root;

  @XmlElement
  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  private boolean local;

  @XmlElement private List<APIURL> locations;

  /** Empty constructor */
  public RegistryStorage_root() {}

  /**
   * Constructor
   *
   * @param root URI (including protocol) to the root of a StorageLocation.
   */
  public RegistryStorage_root(URI root) {
    this.root = root;
  }

  /**
   * URI (including protocol) to the root of a StorageLocation, which when prepended to a
   * StorageLocation path produces a complete URI to a file. Examples:
   *
   * <ul>
   *   <li>https://somewebsite.com/
   *   <li>ftp://host/ (ftp://username:password@host:port/)
   *   <li>ssh://host/
   *   <li>file:///someroot/ (file://C:)
   *   <li>github://org:repo@sha/ (github://org:repo/ (master))
   * </ul>
   *
   * @return URI (including protocol) to the root of a StorageLocation.
   */
  public URI getRoot() {
    return this.root;
  }

  /**
   * Get the Path of the Root (if scheme is 'file')
   *
   * @return the Path of the Root (if scheme is 'file'), otherwise null.
   */
  @JsonIgnore
  public Path getPath() {
    if (this.root.getScheme().equals("file")) return Path.of(new File(getRoot()).getAbsolutePath());
    return null;
  }

  /**
   * (optional): Boolean indicating whether the StorageRoot is local or not (by default this is
   * False)
   *
   * @return Boolean indicating whether the StorageRoot is local or not (by default this is False)
   */
  public boolean isLocal() {
    return this.local;
  }

  /**
   * List of APIURL references of the StorageLocations stored under this Root.
   *
   * @return List of APIURL references of the StorageLocations stored under this Root.
   */
  public List<APIURL> getLocations() {
    return (this.locations == null) ? new ArrayList<>() {} : new ArrayList<>(this.locations);
  }

  /** @param root URI (including protocol) to the root of a StorageLocation. */
  public void setRoot(URI root) {
    this.root = root;
  }

  /**
   * @param local (optional): Boolean indicating whether the StorageRoot is local or not (by default
   *     this is False)
   */
  public void setLocal(boolean local) {
    this.local = local;
  }
}
