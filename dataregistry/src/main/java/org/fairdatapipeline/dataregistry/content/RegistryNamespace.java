package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** A namespace that can be used to group DataProducts. */
@XmlRootElement
public class RegistryNamespace extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String full_name;

  @XmlElement private URL website;

  /** Empty constructor. */
  public RegistryNamespace() {}

  /**
   * Constructor.
   *
   * @param name The Namespace name.
   */
  public RegistryNamespace(String name) {
    this.name = name;
  }

  /**
   * The Namespace name.
   *
   * @return The Namespace name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * (optional): The full name of the Namespace.
   *
   * @return The full name of the Namespace.
   */
  public String getFull_name() {
    return this.full_name;
  }

  /**
   * (optional): Website URL associated with the Namespace.
   *
   * @return Website URL associated with the Namespace.
   */
  public URL getWebsite() {
    return this.website;
  }

  /** @param name The Namespace name */
  public void setName(String name) {
    this.name = name;
  }

  /** @param full_name (optional): The full name of the Namespace. */
  public void setFull_name(String full_name) {
    this.full_name = full_name;
  }

  /** @param website (optional): Website URL associated with the Namespace. */
  public void setWebsite(URL website) {
    this.website = website;
  }
}
