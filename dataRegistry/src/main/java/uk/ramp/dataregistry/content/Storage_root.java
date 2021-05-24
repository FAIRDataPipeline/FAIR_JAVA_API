package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Storage_root extends FDP_Updateable{
    @XmlElement
    private String name;

    @XmlElement
    private String root;

    @XmlElement
    private Boolean accessibility;

    @XmlElement
    private List<String> locations;

    public String getName() { return this.name; }
    public String getRoot() { return this.root; }
    public Boolean getAccessibility() { return this.accessibility; }
    public List<String> getLocations() { return this.locations; }

    public void setName(String name) { this.name = name;}
    public void setRoot(String root) { this.root = root;}
    public void setAccessibility(Boolean accessibility) { this.accessibility = accessibility; }
    public void setLocations(List<String> locations) { this.locations = new ArrayList<String>(locations); }
}
