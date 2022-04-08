package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** Information marking that an Object is an official release of a model code. */
@XmlRootElement
public class RegistryCode_repo_release extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String version;

  @XmlElement private URL website;

  @XmlElement private APIURL object;

  /**
   * Name of the CodeRepoRelease, unique in the context of the CodeRepoRelease.version
   *
   * @return Name of the CodeRepoRelease, unique in the context of the CodeRepoRelease.version
   */
  public String getName() {
    return this.name;
  }

  /**
   * Version identifier of the CodeRepoRelease, must conform to semantic versioning syntax, unique
   * in the context of the CodeRepoRelease.name.
   *
   * @return Version identifier of the CodeRepoRelease.
   */
  public String getVersion() {
    return this.version;
  }

  /**
   * (optional): URL of the website for this code release, if applicable.
   *
   * @return URL of the website for this code release.
   */
  public URL getWebsite() {
    return this.website;
  }

  /**
   * APIURL of the associated Object.
   *
   * @return APIURL of the associated Object.
   */
  public APIURL getObject() {
    return this.object;
  }

  /**
   * @param name Name of the CodeRepoRelease, unique in the context of the CodeRepoRelease.version
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @param version Version identifier of the CodeRepoRelease, must conform to semantic versioning
   *     syntax, unique in the context of the CodeRepoRelease.name.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /** @param website (optional): URL of the website for this code release, if applicable. */
  public void setWebsite(URL website) {
    this.website = website;
  }

  /** @param object APIURL of the associated Object. */
  public void setObject(APIURL object) {
    this.object = object;
  }
}
