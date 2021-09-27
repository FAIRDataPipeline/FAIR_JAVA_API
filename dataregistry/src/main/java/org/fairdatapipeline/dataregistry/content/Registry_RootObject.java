package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.NotImplementedException;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public abstract class Registry_RootObject {
  @JsonIgnore List<String> methods_allowed = List.of("GET", "HEAD", "OPTIONS");

  @XmlElement private APIURL url;

  public Registry_RootObject() {}

  public APIURL getUrl() {
    return url;
  }

  public void setUrl(APIURL url) {
    this.url = url;
  }

  @JsonIgnore
  public String get_django_path() {
    return Registry_RootObject.get_django_path(this.getClass().getSimpleName());
  }

  @JsonIgnore
  public Integer get_id() {
    if (this.url == null) {
      return null;
    }
    return Registry_RootObject.get_id(this.url);
  }

  @JsonIgnore
  public static Integer get_id(APIURL url) {
    String id_part;
    id_part = Paths.get(url.getPath()).getFileName().toString();
    return Integer.parseInt(id_part);
  }

  @JsonIgnore
  public static String get_django_path(String n) {
    if (n.startsWith("Registry_")) {
      throw (new NotImplementedException("this abstract class doesn't have a django path"));
    }
    return n.substring(8).toLowerCase(Locale.ROOT) + "/";
  }

  @JsonIgnore
  public boolean allow_method(String method) {
    return this.methods_allowed.stream().anyMatch(s -> s.equals(method));
  }
}
