package uk.ramp.dataregistry.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.NotImplementedException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Locale;

@XmlRootElement
public class FDP_RootObject {
    @XmlElement
    private String url;

    public FDP_RootObject() {}

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) { this.url = url; }

    @JsonIgnore
    public String get_django_path()  {
        throw(new NotImplementedException("this abstract class doesn't have a django path"));
    }

    @JsonIgnore
    public static String get_django_path(String n) {
        if(n.startsWith("FDP_")){
            throw(new NotImplementedException("this abstract class doesn't have a django path"));
        }
        if(n.equals("FDPObject")){
            return "object/";
        }
        return n.toLowerCase(Locale.ROOT) + "/";
    }

}
