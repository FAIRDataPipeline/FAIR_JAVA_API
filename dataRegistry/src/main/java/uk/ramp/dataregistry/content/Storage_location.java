package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Storage_location extends FDP_Updateable {
  @XmlElement private String path;

  @XmlElement private String hash;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @JsonProperty("public")
  private boolean is_public;

  @XmlElement private String storage_root;

  public Storage_location() {}

  public String getPath() {
    return path;
  }

  public String getHash() {
    return hash;
  }

  public boolean isIs_public() {
    return this.is_public;
  }

  public String getStorage_root() {
    return storage_root;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public void setIs_public(boolean is_public) {
    this.is_public = is_public;
  }

  public void setStorage_root(String storage_root) {
    this.storage_root = storage_root;
  }
}
