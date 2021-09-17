package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryObject extends Registry_Updateable {
  @XmlElement private String description;

  @XmlElement private String storage_location;

  @XmlElement private List<String> authors;

  @XmlElement private String uuid;

  @XmlElement private String file_type;

  @XmlElement private List<String> components;

  @XmlElement private List<String> data_products;

  @XmlElement private String code_repo_release;

  @XmlElement private String quality_control;

  @XmlElement private List<String> licences;

  @XmlElement private List<String> keywords;

  public RegistryObject() {}

  public String getDescription() {
    return description;
  }

  public String getStorage_location() {
    return storage_location;
  }

  public List<String> getAuthors() {
    return (this.authors == null) ? new ArrayList<>() {} : new ArrayList<>(authors);
  }

  public String getUuid() {
    return this.uuid;
  }

  public String getFile_type() {
    return this.file_type;
  }

  public List<String> getComponents() {
    return (this.components == null) ? new ArrayList<>() {} : new ArrayList<>(components);
  }

  public List<String> getData_products() {
    return (this.data_products == null) ? new ArrayList<>() {} : new ArrayList<>(data_products);
  }

  public String getCode_repo_release() {
    return code_repo_release;
  }

  public String getQuality_control() {
    return quality_control;
  }

  public List<String> getLicences() {
    return (this.licences == null) ? new ArrayList<>() {} : new ArrayList<>(licences);
  }

  public List<String> getKeywords() {
    return (this.keywords == null) ? new ArrayList<>() {} : new ArrayList<>(keywords);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setStorage_location(String storage_location) {
    this.storage_location = storage_location;
  }

  public void setAuthors(List<String> authors) {
    this.authors = new ArrayList<>(authors);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setFile_type(String file_type) {
    this.file_type = file_type;
  }

  public void setComponents(List<String> components) {
    this.components = new ArrayList<>(components);
  }

  public void setData_products(List<String> data_products) {
    this.data_products = new ArrayList<>(data_products);
  }

  public void setCode_repo_release(String code_repo_release) {
    this.code_repo_release = code_repo_release;
  }

  public void setQuality_control(String quality_control) {
    this.quality_control = quality_control;
  }

  public void setLicences(List<String> licences) {
    this.licences = new ArrayList<>(licences);
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = new ArrayList<>(keywords);
  }

  @JsonIgnore
  public String get_django_path() {
    return "object/";
  }
}
