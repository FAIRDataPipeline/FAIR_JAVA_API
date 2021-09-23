package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Registry_Updateable extends Registry_RootObject {

  @XmlElement private LocalDateTime last_updated;

  @XmlElement private URL updated_by;

  public Registry_Updateable() {}

  public LocalDateTime getLast_updated() {
    return last_updated;
  }

  public URL getUpdated_by() {
    return updated_by;
  }

  public void setLast_updated(LocalDateTime last_updated) {
    this.last_updated = last_updated;
  }

  public void setUpdated_by(URL updated_by) {
    this.updated_by = updated_by;
  }
}
