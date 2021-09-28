package org.fairdatapipeline.dataregistry.content;

import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** All updateable registry objects inherit from this one. (only Users aren't updateable) */
@XmlRootElement
public abstract class Registry_Updateable extends Registry_RootObject {

  @XmlElement private LocalDateTime last_updated;

  @XmlElement private APIURL updated_by;

  /**
   * (read-only) Datetime that this record was last updated.
   *
   * @return Datetime that this record was last updated.
   */
  public LocalDateTime getLast_updated() {
    return last_updated;
  }

  /**
   * (read-only) Reference to the user that updated this record.
   *
   * @return Reference to the user that updated this record.
   */
  public APIURL getUpdated_by() {
    return updated_by;
  }

  /**
   * Last_updated should be read-only, so setter shouldn't really exist.
   *
   * @param last_updated DateTime that this record was last updated.
   */
  public void setLast_updated(LocalDateTime last_updated) {
    this.last_updated = last_updated;
  }

  /**
   * Updated_by should be read-only, so setter shouldn't really exist.
   *
   * @param updated_by Reference to the user that updated this record.
   */
  public void setUpdated_by(APIURL updated_by) {
    this.updated_by = updated_by;
  }
}
