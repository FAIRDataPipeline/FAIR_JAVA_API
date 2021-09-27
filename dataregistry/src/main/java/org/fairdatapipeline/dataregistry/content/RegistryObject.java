package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

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

  public RegistryObject() {}

  public String getDescription() {
    return description;
  }

  public APIURL getStorage_location() {
    return storage_location;
  }

  public List<APIURL> getAuthors() {
    return (this.authors == null) ? new ArrayList<>() {} : new ArrayList<>(authors);
  }

  public String getUuid() {
    return this.uuid;
  }

  public APIURL getFile_type() {
    return this.file_type;
  }

  public List<APIURL> getComponents() {
    return (this.components == null) ? new ArrayList<>() {} : new ArrayList<>(components);
  }

  public List<APIURL> getData_products() {
    return (this.data_products == null) ? new ArrayList<>() {} : new ArrayList<>(data_products);
  }

  public APIURL getCode_repo_release() {
    return code_repo_release;
  }

  public APIURL getQuality_control() {
    return quality_control;
  }

  public List<APIURL> getLicences() {
    return (this.licences == null) ? new ArrayList<>() {} : new ArrayList<>(licences);
  }

  public List<APIURL> getKeywords() {
    return (this.keywords == null) ? new ArrayList<>() {} : new ArrayList<>(keywords);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setStorage_location(APIURL storage_location) {
    this.storage_location = storage_location;
  }

  public void setAuthors(List<APIURL> authors) {
    this.authors = new ArrayList<>(authors);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setFile_type(APIURL file_type) {
    this.file_type = file_type;
  }

  public void setComponents(List<APIURL> components) {
    this.components = new ArrayList<>(components);
  }

  public void setData_products(List<APIURL> data_products) {
    this.data_products = new ArrayList<>(data_products);
  }

  public void setCode_repo_release(APIURL code_repo_release) {
    this.code_repo_release = code_repo_release;
  }

  public void setQuality_control(APIURL quality_control) {
    this.quality_control = quality_control;
  }

  public void setLicences(List<APIURL> licences) {
    this.licences = new ArrayList<>(licences);
  }

  public void setKeywords(List<APIURL> keywords) {
    this.keywords = new ArrayList<>(keywords);
  }

  @JsonIgnore
  public String get_django_path() {
    return "object/";
  }
}
