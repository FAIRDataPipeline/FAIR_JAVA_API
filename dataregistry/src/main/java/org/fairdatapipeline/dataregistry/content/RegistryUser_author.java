package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryUser_author extends Registry_RootObject {

  @XmlElement private URL user;

  @XmlElement private URL author;

  public RegistryUser_author() {}

  public URL getUser() {
    return user;
  }

  public URL getAuthor() {
    return author;
  }

  public void setUser(URL user) {
    this.user = user;
  }

  public void setAuthor(URL author) {
    this.author = author;
  }
}
