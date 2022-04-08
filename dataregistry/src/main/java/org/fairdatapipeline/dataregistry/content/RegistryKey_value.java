package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** Free meta-data key-values associated with an Object. */
@XmlRootElement
public class RegistryKey_value extends Registry_Updateable {
  @XmlElement private String key;

  @XmlElement private String value;

  @XmlElement private APIURL object;

  /**
   * Meta-data name
   *
   * @return Meta-data name
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Meta-data value
   *
   * @return Meta-data value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * APIURL of the associated Object.
   *
   * @return APIURL of the associated Object.
   */
  public APIURL getObject() {
    return this.object;
  }

  /** @param key Meta-data name */
  public void setKey(String key) {
    this.key = key;
  }

  /** @param value Meta-data value */
  public void setValue(String value) {
    this.value = value;
  }

  /** @param object APIURL of the associated Object. */
  public void setObject(APIURL object) {
    this.object = object;
  }
}
