package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Source extends FDP_Updateable{
    @XmlElement
    private String name;

    @XmlElement
    private String abbreviation;

    @XmlElement
    private String website;

    public Source(){}

    public String getName() { return this.name; }
    public String getAbbreviation() { return this.abbreviation; }
    public String getWebsite() { return this.website; }

    public void setName(String name) { this.name = name; }
    public void setAbbreviation(String abbreviation) { this.abbreviation = abbreviation; }
    public void setWebsite(String website) { this.website = website; }
}
