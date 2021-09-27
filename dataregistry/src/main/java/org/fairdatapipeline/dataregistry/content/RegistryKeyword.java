package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class RegistryKeyword extends Registry_Updateable {
  @XmlElement private APIURL object;

  @XmlElement private String keyphrase;

  @XmlElement private String identifier;

  public RegistryKeyword() {}

  public APIURL getObject() {
    return this.object;
  }

  public String getKeyphrase() {
    return this.keyphrase;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setObject(APIURL object) {
    this.object = object;
  }

  public void setKeyphrase(String keyphrase) {
    this.keyphrase = keyphrase;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
}
