package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class RegistryKey_value extends Registry_Updateable {
  @XmlElement private String key;

  @XmlElement private String value;

  @XmlElement private APIURL object;

  public RegistryKey_value() {}

  public String getKey() {
    return this.key;
  }

  public String getValue() {
    return this.value;
  }

  public APIURL getObject() {
    return this.object;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setObject(APIURL object) {
    this.object = object;
  }
}
