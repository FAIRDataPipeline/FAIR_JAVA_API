package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryExternal_object extends Registry_Updateable {
  @XmlElement private String identifier;

  @XmlElement private String alternate_identifier;

  @XmlElement private String alternate_identifier_type;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @XmlElement
  private Boolean primary_not_supplement;

  @XmlElement private LocalDateTime release_date;

  @XmlElement private String title;

  @XmlElement private String description;

  @XmlElement private String data_product;

  @XmlElement private String original_store;

  @XmlElement private String version;

  public RegistryExternal_object() {};

  public String getIdentifier() {
    return identifier;
  }

  public String getAlternate_identifier() {
    return alternate_identifier;
  }

  public String getAlternate_identifier_type() {
    return alternate_identifier_type;
  }

  public Boolean isPrimary_not_supplement() {
    return primary_not_supplement;
  }

  public LocalDateTime getRelease_date() {
    return release_date;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getData_product() {
    return data_product;
  }

  public String getOriginal_store() {
    return original_store;
  }

  public String getVersion() {
    return version;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setAlternate_identifier(String alternate_identifier) {
    this.alternate_identifier = alternate_identifier;
  }

  public void setAlternate_identifier_type(String alternate_identifier_type) {
    this.alternate_identifier_type = alternate_identifier_type;
  }

  public void setPrimary_not_supplement(Boolean primary_not_supplement) {
    this.primary_not_supplement = primary_not_supplement;
  }

  public void setRelease_date(LocalDateTime release_date) {
    this.release_date = release_date;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setData_product(String data_product) {
    this.data_product = data_product;
  }

  public void setOriginal_store(String original_store) {
    this.original_store = original_store;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
