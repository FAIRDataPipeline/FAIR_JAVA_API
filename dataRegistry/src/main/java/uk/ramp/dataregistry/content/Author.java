package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Author extends FDP_Updateable{
    @XmlElement
    private String family_name;

    @XmlElement
    private String personal_name;

    @XmlElement
    private String object;

    public Author() {}


    public String getFamily_name() { return this.family_name; }
    public String getPersonal_name() { return this.personal_name; }
    public String getObject() { return this.object; }

    public void setFamily_name(String family_name) { this.family_name = family_name; }
    public void setPersonal_name(String personal_name) { this.personal_name = personal_name; }
    public void setObject(String object) { this.object = object; }
}
