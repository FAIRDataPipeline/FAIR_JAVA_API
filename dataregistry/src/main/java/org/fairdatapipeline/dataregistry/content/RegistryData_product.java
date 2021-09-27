package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class RegistryData_product extends Registry_Updateable {

  @XmlElement private String name;

  @XmlElement private String version;

  @XmlElement private APIURL object;

  @XmlElement private APIURL namespace;

  @XmlElement private APIURL external_object;

  @JsonFormat(shape = JsonFormat.Shape.NUMBER)
  @XmlElement
  private boolean internal_format;

  // @XmlElement private String prov_report;

  public RegistryData_product() {
    this.methods_allowed = List.of("GET", "PUT", "PATCH", "HEAD", "OPTIONS");
  }

  public String getName() {
    return name;
  }

  public String getVersion() {
    return version;
  }

  public APIURL getObject() {
    return object;
  }

  public APIURL getNamespace() {
    return namespace;
  }

  public APIURL getExternal_object() {
    return external_object;
  }

  public boolean isInternal_format() {
    return this.internal_format;
  }

  // public String getProv_report() {
  //  return this.prov_report;
  // }

  public void setName(String name) {
    this.name = name;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setObject(APIURL object) {
    this.object = object;
  }

  public void setNamespace(APIURL namespace) {
    this.namespace = namespace;
  }

  public void setExternal_object(APIURL external_object) {
    this.external_object = external_object;
  }

  public void setInternal_format(boolean internal_format) {
    this.internal_format = internal_format;
  }

  // public void setProv_report(String prov_report) {
  //  this.prov_report = prov_report;
  // }
}
