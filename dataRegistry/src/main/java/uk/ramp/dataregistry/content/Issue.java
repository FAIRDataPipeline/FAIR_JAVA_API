package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Issue extends FDP_Updateable{
    @XmlElement
    private Integer severity;

    @XmlElement
    private String description;

    @XmlElement
    private List<String> object_issues;

    @XmlElement
    private List<String> component_issues;

    public Integer getSeverity() { return this.severity; }
    public String getDescription() { return this.description; }
    public List<String> getObject_issues() { return new ArrayList<String>(this.object_issues); }
    public List<String> getComponent_issues() { return new ArrayList<String>(this.component_issues);}

    public void setSeverity(Integer severity) { this.severity = severity; }
    public void setDescription(String description) { this.description = description; }
    public void setObject_issues(List<String> object_issues) { this.object_issues = new ArrayList<String>(object_issues);}
    public void setComponent_issues(List<String> component_issues) { this.component_issues = new ArrayList<String>(component_issues); }
}
