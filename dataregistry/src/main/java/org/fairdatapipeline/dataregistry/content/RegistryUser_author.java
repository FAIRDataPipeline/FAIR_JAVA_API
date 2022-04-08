package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** A combination of an Author associated with a particular user. */
@XmlRootElement
public class RegistryUser_author extends Registry_Updateable {

  @XmlElement private APIURL user;

  @XmlElement private APIURL author;

  /**
   * The API URL of the User to associate with this UserAuthor.
   *
   * @return The API URL of the User to associate with this UserAuthor.
   */
  public APIURL getUser() {
    return user;
  }

  /**
   * The API URL of the Author to associate with this UserAuthor.
   *
   * @return The API URL of the Author to associate with this UserAuthor.
   */
  public APIURL getAuthor() {
    return author;
  }

  /** @param user The API URL of the User to associate with this UserAuthor. */
  public void setUser(APIURL user) {
    this.user = user;
  }

  /** @param author The API URL of the Author to associate with this UserAuthor. */
  public void setAuthor(APIURL author) {
    this.author = author;
  }
}
