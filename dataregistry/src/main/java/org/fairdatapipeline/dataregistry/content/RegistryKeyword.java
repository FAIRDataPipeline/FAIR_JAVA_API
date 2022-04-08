package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * Keywords that can be associated with an Object usually for use with ExternalObjects to record
 * paper keywords, etc.
 */
@XmlRootElement
public class RegistryKeyword extends Registry_Updateable {
  @XmlElement private APIURL object;

  @XmlElement private String keyphrase;

  @XmlElement private URL identifier;

  /**
   * {@link APIURL} of the associated Object.
   *
   * @return {@link APIURL} of the associated Object.
   */
  public APIURL getObject() {
    return this.object;
  }

  /**
   * Free text field for the key phrase to associate with the Object.
   *
   * @return Free text field for the key phrase to associate with the Object.
   */
  public String getKeyphrase() {
    return this.keyphrase;
  }

  /**
   * (optional): URL of ontology annotation to associate with this Keyword.
   *
   * @return URL of ontology annotation to associate with this Keyword.
   */
  public URL getIdentifier() {
    return this.identifier;
  }

  /** @param object {@link APIURL} of the associated Object. */
  public void setObject(APIURL object) {
    this.object = object;
  }

  /** @param keyphrase Free text field for the key phrase to associate with the Object. */
  public void setKeyphrase(String keyphrase) {
    this.keyphrase = keyphrase;
  }

  /** @param identifier (optional): URL of ontology annotation to associate with this Keyword. */
  public void setIdentifier(URL identifier) {
    this.identifier = identifier;
  }
}
