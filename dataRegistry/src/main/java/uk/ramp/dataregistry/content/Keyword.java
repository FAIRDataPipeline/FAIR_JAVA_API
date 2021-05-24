package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Keyword extends FDP_Updateable{
    @XmlElement
    private String keyphrase;

    @XmlElement
    private String object;


    public Keyword(){}

    public String getKeyphrase() { return this.keyphrase; }
    public String getObject() { return this.object; }

    public void setKeyphrase(String keyphrase) { this.keyphrase = keyphrase; }
    public void setObject(String object) { this.object = object; }
}
