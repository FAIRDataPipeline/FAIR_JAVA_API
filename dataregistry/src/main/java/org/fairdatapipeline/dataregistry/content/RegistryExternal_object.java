package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.net.URL;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * An external data object, one that has come from somewhere other than being generated as part of
 * the modelling pipeline.
 */
@XmlRootElement
public class RegistryExternal_object extends Registry_Updateable {
  @XmlElement private URL identifier;

  @XmlElement private String alternate_identifier;

  @XmlElement private String alternate_identifier_type;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @XmlElement
  private boolean primary_not_supplement;

  @XmlElement private LocalDateTime release_date;

  @XmlElement private String title;

  @XmlElement private String description;

  @XmlElement private APIURL data_product;

  @XmlElement private APIURL original_store;

  @XmlElement private String version;

  /** Empty constructor. */
  public RegistryExternal_object() {}

  /**
   * Full URL of identifier (e.g. DataCite DOI) of the ExternalObject, unique in the context of the
   * triple (identifier, title, version). At least one of identifier and alternate_identifier must
   * be defined.
   *
   * @return Full URL of identifier (e.g. DataCite DOI) of the ExternalObject.
   */
  public URL getIdentifier() {
    return identifier;
  }

  /**
   * Name of the ExternalObject, unique in the context of the quadruple (alternate_identifier,
   * alternate_identifier_type, title, version). Unlike identifier, this is free text, not a url.
   * For instance, it can be a locally unique name for a data resource within the domain of issue.
   * It is associated with a alternate_identifier_type which describes its origin.
   *
   * @return Name of the ExternalObject. Unlike identifier, this is free text, not a url.
   */
  public String getAlternate_identifier() {
    return alternate_identifier;
  }

  /**
   * Type of alternate_identifier, required if alternate_identifier is defined.
   *
   * @return Type of alternate_identifier.
   */
  public String getAlternate_identifier_type() {
    return alternate_identifier_type;
  }

  /**
   * (optional): Boolean flag to indicate that the ExternalObject is a primary source.
   *
   * @return Boolean flag to indicate that the ExternalObject is a primary source.
   */
  public boolean isPrimary_not_supplement() {
    return primary_not_supplement;
  }

  /**
   * Date-time the ExternalObject was released.
   *
   * @return Date-time the ExternalObject was released.
   */
  public LocalDateTime getRelease_date() {
    return release_date;
  }

  /**
   * Title of the ExternalObject.
   *
   * @return Title of the ExternalObject.
   */
  public String getTitle() {
    return title;
  }

  /**
   * (optional): Free text description of the ExternalObject.
   *
   * @return Free text description of the ExternalObject.
   */
  public String getDescription() {
    return description;
  }

  /**
   * API URL of the associated DataProduct.
   *
   * @return API URL of the associated DataProduct.
   */
  public APIURL getData_product() {
    return data_product;
  }

  /**
   * (optional): StorageLocation that references the original location of this ExternalObject. For
   * example, if the original data location could be transient and so the data has been copied to a
   * more robust location, this would be the reference to the original data location.
   *
   * @return API URL of the StorageLocation that references the original location of this
   *     ExternalObject.
   */
  public APIURL getOriginal_store() {
    return original_store;
  }

  /**
   * (read-only) Version identifier of the DataProduct associated with this ExternalObject.
   *
   * @return Version identifier of the DataProduct associated with this ExternalObject.
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param identifier Full URL of identifier (e.g. DataCite DOI) of the ExternalObject, unique in
   *     the context of the triple (identifier, title, version). At least one of identifier and
   *     alternate_identifier must be defined.
   */
  public void setIdentifier(URL identifier) {
    this.identifier = identifier;
  }

  /**
   * @param alternate_identifier Name of the ExternalObject, unique in the context of the quadruple
   *     (alternate_identifier, alternate_identifier_type, title, version). Unlike identifier, this
   *     is free text, not a url.
   */
  public void setAlternate_identifier(String alternate_identifier) {
    this.alternate_identifier = alternate_identifier;
  }

  /**
   * @param alternate_identifier_type Type of alternate_identifier, required if alternate_identifier
   *     is defined.
   */
  public void setAlternate_identifier_type(String alternate_identifier_type) {
    this.alternate_identifier_type = alternate_identifier_type;
  }

  /**
   * @param primary_not_supplement (optional): Boolean flag to indicate that the ExternalObject is a
   *     primary source.
   */
  public void setPrimary_not_supplement(boolean primary_not_supplement) {
    this.primary_not_supplement = primary_not_supplement;
  }

  /** @param release_date Date-time the ExternalObject was released. */
  public void setRelease_date(LocalDateTime release_date) {
    this.release_date = release_date;
  }

  /** @param title Title of the ExternalObject. */
  public void setTitle(String title) {
    this.title = title;
  }

  /** @param description (optional): Free text description of the ExternalObject. */
  public void setDescription(String description) {
    this.description = description;
  }

  /** @param data_product API URL of the associated DataProduct. */
  public void setData_product(APIURL data_product) {
    this.data_product = data_product;
  }

  /**
   * @param original_store (optional): StorageLocation that references the original location of this
   *     ExternalObject.
   */
  public void setOriginal_store(APIURL original_store) {
    this.original_store = original_store;
  }
}
