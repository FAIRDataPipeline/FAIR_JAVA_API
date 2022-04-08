package org.fairdatapipeline.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** A registry User. */
@XmlRootElement
public class RegistryUsers extends Registry_RootObject {

  @XmlElement private String username;

  @XmlElement private String full_name;

  @XmlElement private String email;

  @XmlElement private List<String> orgs;

  /**
   * login/username
   *
   * @return login/username
   */
  public String getUsername() {
    return username;
  }

  /**
   * The users full name
   *
   * @return the users full name
   */
  public String getFull_name() {
    return full_name;
  }

  /**
   * the users email address
   *
   * @return the users email address
   */
  public String getEmail() {
    return email;
  }

  /**
   * List of Orgs.
   *
   * @return List of Orgs.
   */
  public List<String> getOrgs() {
    return (orgs == null) ? new ArrayList<>() {} : new ArrayList<>(orgs);
  }
}
