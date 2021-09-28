package org.fairdatapipeline.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.NotImplementedException;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** Abstract class that all registry resources inherit from. */
@XmlRootElement
public abstract class Registry_RootObject {
  @JsonIgnore List<String> methods_allowed = List.of("GET", "HEAD", "OPTIONS");

  @XmlElement private APIURL url;

  /**
   * (read-only) The {@link APIURL} of this object; read-only: this is set by the registry upon
   * POSTing the object.
   *
   * @return The {@link APIURL} of this object.
   */
  public APIURL getUrl() {
    return url;
  }

  /**
   * The resource name on the django server, which is used to create the WebTarget URL.
   *
   * @return The resource name on the django server, which is used to create the WebTarget URL.
   */
  @JsonIgnore
  public String get_django_path() {
    return Registry_RootObject.get_django_path(this.getClass().getSimpleName());
  }

  /**
   * If the {@link APIURL} of this object is set, return the ID part of the APIURL (as an Integer)
   *
   * @return The ID part (Integer) of the APIURL of this object, or null if URL is not set.
   */
  @JsonIgnore
  public Integer get_id() {
    if (this.url == null) {
      return null;
    }
    return Registry_RootObject.get_id(this.url);
  }

  /**
   * Extract ID from {@link APIURL}
   *
   * @param url the {@link APIURL} we want to get the ID from
   * @return the ID part of the given {@link APIURL}
   */
  @JsonIgnore
  public static Integer get_id(APIURL url) {
    String id_part;
    id_part = Paths.get(url.getPath()).getFileName().toString();
    return Integer.parseInt(id_part);
  }

  /**
   * Create resource path from Class.simpleName
   *
   * @param simpleName SimpleName of a class inherited from Registry_RootObject
   * @return the resource name on the django FAIR DataRegistry for the class referred to by
   *     simpleName.
   */
  @JsonIgnore
  public static String get_django_path(String simpleName) {
    if (simpleName.startsWith("Registry_")) {
      throw (new NotImplementedException("this abstract class doesn't have a django path"));
    }
    return simpleName.substring(8).toLowerCase(Locale.ROOT) + "/";
  }

  /**
   * Check if a given HTTP method is allowed for this resource
   *
   * @param method a HTTP method: GET, POST, PUT, DELETE or PATCH
   * @return Is this HTTP method allowed on this resource?
   */
  @JsonIgnore
  public boolean allow_method(String method) {
    return this.methods_allowed.stream().anyMatch(s -> s.equals(method));
  }

  /**
   * URL is supposed to be read-only so this shouldn't really be here. But it makes life so much
   * easier being able to set everything.
   *
   * @param url The {@link APIURL} to set.
   */
  public void setUrl(APIURL url) {
    this.url = url;
  }
}
