package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Authors that can be associated with an Object usually for use with ExternalObjects to record
 * paper authors, etc.
 */
@XmlRootElement
public class RegistryAuthor extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String identifier;

  @XmlElement private String uuid;

  /**
   * (optional): Full name or organisation name of the Author. Note that at least one of name or
   * identifier must be specified.
   *
   * @return Full name or organisation name of the Author.
   */
  public String getName() {
    return this.name;
  }

  /**
   * (optional): Full URL of identifier (e.g. ORCiD or ROR ID) of the Author.
   *
   * @return Full URL of identifier (e.g. ORCiD or ROR ID) of the Author.
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * (optional): UUID of the Author. If not specified a UUID is generated automatically by Registry
   *
   * @return UUID of the Author.
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @param name (optional): Full name or organisation name of the Author. Note that at least one of
   *     name or identifier must be specified.
   */
  public void setName(String name) {
    this.name = name;
  }

  /** @param identifier (optional): Full URL of identifier (e.g. ORCiD or ROR ID) of the Author. */
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  /**
   * @param uuid (optional): UUID of the Author. If not specified a UUID is generated automatically
   *     by Registry
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
