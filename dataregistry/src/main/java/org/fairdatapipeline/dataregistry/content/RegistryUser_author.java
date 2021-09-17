package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryUser_author extends Registry_RootObject {

  @XmlElement private String user;

  @XmlElement private String author;

  public RegistryUser_author() {}

  public String getUser() {
    return user;
  }

  public String getAuthor() {
    return author;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
