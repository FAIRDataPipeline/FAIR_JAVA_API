package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** The location of an item relative to a StorageRoot */
@XmlRootElement
public class RegistryStorage_location extends Registry_Updateable {
  @XmlElement private String path;

  @XmlElement private String hash;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @JsonProperty("public")
  private boolean is_public = true;

  @XmlElement private APIURL storage_root;

  /** Empty constructor. */
  public RegistryStorage_location() {}

  /**
   * Path from a StorageRoot uri to the item location, when appended to a StorageRoot uri produces a
   * complete URI.
   *
   * @return Path from a StorageRoot uri to the item location, when appended to a StorageRoot uri
   *     produces a complete URI.
   */
  public String getPath() {
    return path;
  }

  /**
   * If StorageLocation references a file, this is the calculated SHA1 hash of the file. For GIT
   * repositories this is the git hash.
   *
   * @return The calculated SHA1 hash of the file, or git hash of the repo.
   */
  public String getHash() {
    return hash;
  }

  /**
   * (optional): Boolean indicating whether the StorageLocation is public or not (default is True)
   *
   * @return Boolean indicating whether the StorageLocation is public or not (default is True)
   */
  public boolean isIs_public() {
    return this.is_public;
  }

  /**
   * Reference to the StorageRoot to append the path to.
   *
   * @return Reference to the StorageRoot to append the path to.
   */
  public APIURL getStorage_root() {
    return storage_root;
  }

  /**
   * @param path Path from a StorageRoot uri to the item location, when appended to a StorageRoot
   *     uri produces a complete URI.
   */
  public void setPath(String path) {
    this.path = path;
  }

  /** @param hash The calculated SHA1 hash of the file, or git hash of the repo. */
  public void setHash(String hash) {
    this.hash = hash;
  }

  /**
   * @param is_public (optional): Boolean indicating whether the StorageLocation is public or not
   *     (default is True)
   */
  public void setIs_public(boolean is_public) {
    this.is_public = is_public;
  }

  /** @param storage_root Reference to the StorageRoot to append the path to. */
  public void setStorage_root(APIURL storage_root) {
    this.storage_root = storage_root;
  }
}
