package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryObject extends Registry_Updateable {
  @XmlElement private String description;

  @XmlElement private URL storage_location;

  @XmlElement private List<URL> authors;

  @XmlElement private String uuid;

  @XmlElement private URL file_type;

  @XmlElement private List<URL> components;

  @XmlElement private List<URL> data_products;

  @XmlElement private URL code_repo_release;

  @XmlElement private URL quality_control;

  @XmlElement private List<URL> licences;

  @XmlElement private List<URL> keywords;

  public RegistryObject() {}

  public String getDescription() {
    return description;
  }

  public URL getStorage_location() {
    return storage_location;
  }

  public List<URL> getAuthors() {
    return (this.authors == null) ? new ArrayList<>() {} : new ArrayList<>(authors);
  }

  public String getUuid() {
    return this.uuid;
  }

  public URL getFile_type() {
    return this.file_type;
  }

  public List<URL> getComponents() {
    return (this.components == null) ? new ArrayList<>() {} : new ArrayList<>(components);
  }

  public List<URL> getData_products() {
    return (this.data_products == null) ? new ArrayList<>() {} : new ArrayList<>(data_products);
  }

  public URL getCode_repo_release() {
    return code_repo_release;
  }

  public URL getQuality_control() {
    return quality_control;
  }

  public List<URL> getLicences() {
    return (this.licences == null) ? new ArrayList<>() {} : new ArrayList<>(licences);
  }

  public List<URL> getKeywords() {
    return (this.keywords == null) ? new ArrayList<>() {} : new ArrayList<>(keywords);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setStorage_location(URL storage_location) {
    this.storage_location = storage_location;
  }

  public void setAuthors(List<URL> authors) {
    this.authors = new ArrayList<>(authors);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setFile_type(URL file_type) {
    this.file_type = file_type;
  }

  public void setComponents(List<URL> components) {
    this.components = new ArrayList<>(components);
  }

  public void setData_products(List<URL> data_products) {
    this.data_products = new ArrayList<>(data_products);
  }

  public void setCode_repo_release(URL code_repo_release) {
    this.code_repo_release = code_repo_release;
  }

  public void setQuality_control(URL quality_control) {
    this.quality_control = quality_control;
  }

  public void setLicences(List<URL> licences) {
    this.licences = new ArrayList<>(licences);
  }

  public void setKeywords(List<URL> keywords) {
    this.keywords = new ArrayList<>(keywords);
  }

  @JsonIgnore
  public String get_django_path() {
    return "object/";
  }
}
