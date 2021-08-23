package uk.ramp.dataregistry.content;

import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.*;

@XmlRootElement
public class Registry_Updateable extends Registry_RootObject {

  @XmlElement private LocalDateTime last_updated;

  @XmlElement private String updated_by;

  public Registry_Updateable() {}

  public LocalDateTime getLast_updated() {
    return last_updated;
  }

  public String getUpdated_by() {
    return updated_by;
  }

  public void setLast_updated(LocalDateTime last_updated) {
    this.last_updated = last_updated;
  }

  public void setUpdated_by(String updated_by) {
    this.updated_by = updated_by;
  }
}
