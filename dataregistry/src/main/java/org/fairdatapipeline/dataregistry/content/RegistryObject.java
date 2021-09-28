package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * Core traceability object used to represent any data object such DataProduct, CodeRepoRelease, etc
 */
@XmlRootElement
public class RegistryObject extends Registry_Updateable {
  @XmlElement private String description;

  @XmlElement private APIURL storage_location;

  @XmlElement private List<APIURL> authors;

  @XmlElement private String uuid;

  @XmlElement private APIURL file_type;

  @XmlElement private List<APIURL> components;

  @XmlElement private List<APIURL> data_products;

  @XmlElement private APIURL code_repo_release;

  @XmlElement private APIURL quality_control;

  @XmlElement private List<APIURL> licences;

  @XmlElement private List<APIURL> keywords;

  /** Empty constructor. */
  public RegistryObject() {}

  /**
   * (optional): Free text description of the Object.
   *
   * @return Free text description of the Object.
   */
  public String getDescription() {
    return description;
  }

  /**
   * (optional): The APIURL of the StorageLocation which is the location of the physical data of
   * this object, if applicable.
   *
   * @return The APIURL of the StorageLocation which is the location of the physical data of this
   *     object, if applicable.
   */
  public APIURL getStorage_location() {
    return storage_location;
  }

  /**
   * (optional): List of Author APIURLs associated with this Object.
   *
   * @return List of Author APIURLs associated with this Object.
   */
  public List<APIURL> getAuthors() {
    return (this.authors == null) ? new ArrayList<>() {} : new ArrayList<>(authors);
  }

  /**
   * (optional): UUID of the Object. If not specified a UUID is generated automatically.
   *
   * @return UUID of the Object.
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * (optional): FileType of this Object.
   *
   * @return FileType of this Object.
   */
  public APIURL getFile_type() {
    return this.file_type;
  }

  /**
   * List of ObjectComponents APIURLs associated with this Object.
   *
   * @return List of ObjectComponents APIURLs associated with this Object.
   */
  public List<APIURL> getComponents() {
    return (this.components == null) ? new ArrayList<>() {} : new ArrayList<>(components);
  }

  /**
   * List of DataProduct APIURLs if one or more is associated with this Object.
   *
   * @return List of DataProduct APIURLs if one or more is associated with this Object.
   */
  public List<APIURL> getData_products() {
    return (this.data_products == null) ? new ArrayList<>() {} : new ArrayList<>(data_products);
  }

  /**
   * The CodeRepoRelease APIURL if one is associated with this Object.
   *
   * @return The CodeRepoRelease APIURL if one is associated with this Object.
   */
  public APIURL getCode_repo_release() {
    return code_repo_release;
  }

  /**
   * The QualityControl APIURL if one is associated with this Object.
   *
   * @return The QualityControl APIURL if one is associated with this Object
   */
  public APIURL getQuality_control() {
    return quality_control;
  }

  /**
   * List of Licence APIURLs associated with this Object.
   *
   * @return List of Licence APIURLs associated with this Object.
   */
  public List<APIURL> getLicences() {
    return (this.licences == null) ? new ArrayList<>() {} : new ArrayList<>(licences);
  }

  /**
   * List of Keyword APIURLs associated with this Object.
   *
   * @return List of Keyword APIURLs associated with this Object.
   */
  public List<APIURL> getKeywords() {
    return (this.keywords == null) ? new ArrayList<>() {} : new ArrayList<>(keywords);
  }

  /** @param description (optional): Free text description of the Object. */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param storage_location (optional): The APIURL of the StorageLocation which is the location of
   *     the physical data of this object, if applicable.
   */
  public void setStorage_location(APIURL storage_location) {
    this.storage_location = storage_location;
  }

  /** @param authors (optional): List of Author APIURLs associated with this Object. */
  public void setAuthors(List<APIURL> authors) {
    this.authors = new ArrayList<>(authors);
  }

  /**
   * @param uuid (optional): UUID of the Object. If not specified a UUID is generated automatically.
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /** @param file_type (optional): FileType of this Object. */
  public void setFile_type(APIURL file_type) {
    this.file_type = file_type;
  }

  @JsonIgnore
  public String get_django_path() {
    return "object/";
  }
}
