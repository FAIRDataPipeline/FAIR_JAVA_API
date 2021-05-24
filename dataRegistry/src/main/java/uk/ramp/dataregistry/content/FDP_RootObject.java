package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FDP_RootObject {
    @XmlElement
    private String url;

    public FDP_RootObject() {}

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) { this.url = url; }

}
