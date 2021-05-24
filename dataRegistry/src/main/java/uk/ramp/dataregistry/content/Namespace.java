package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures;


@XmlRootElement
public class Namespace extends FDP_Updateable {
    @XmlElement
    private String name;

    public Namespace() {
    }
    public Namespace(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }
}
