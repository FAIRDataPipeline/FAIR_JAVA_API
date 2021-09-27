package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class RegistryUser_author extends Registry_RootObject {

  @XmlElement private APIURL user;

  @XmlElement private APIURL author;

  public RegistryUser_author() {}

  public APIURL getUser() {
    return user;
  }

  public APIURL getAuthor() {
    return author;
  }

  public void setUser(APIURL user) {
    this.user = user;
  }

  public void setAuthor(APIURL author) {
    this.author = author;
  }
}
