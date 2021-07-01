package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Locale;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.NotImplementedException;

@XmlRootElement
public abstract class FDP_RootObject {
  @XmlElement private String url;

  public FDP_RootObject() {}

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @JsonIgnore
  public String get_django_path() {
    throw (new NotImplementedException("this abstract class doesn't have a django path"));
  }

  @JsonIgnore
  public Integer get_id() {
    if (this.url == null) {
      return null;
    }
    String id_part;
    try {
      id_part = Paths.get(new URL(this.url).getPath()).getFileName().toString();
    } catch (MalformedURLException e) {
      throw (new IllegalArgumentException(
          "Trying to parse a bad URL " + this.url + " (" + e + ")"));
    }
    return Integer.parseInt(id_part);
  }

  @JsonIgnore
  public static String get_django_path(String n) {
    if (n.startsWith("FDP_")) {
      throw (new NotImplementedException("this abstract class doesn't have a django path"));
    }
    if (n.equals("FDPObject")) {
      return "object/";
    }
    return n.toLowerCase(Locale.ROOT) + "/";
  }
}
