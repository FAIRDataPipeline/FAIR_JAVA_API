package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
public class User extends FDP_RootObject {

    @XmlElement
    private String username;

    @XmlElement
    private String full_name;

    @XmlElement
    private String email;

    @XmlElement
    private List<String> orgs;

    public User() {}

    public String getUsername() { return username; }
    public String getFull_name() { return full_name; }
    public String getEmail() { return email; }
    public List<String> getOrgs() { return (orgs==null)?new ArrayList<>(){} : new ArrayList<String>(orgs); }

    public void setUsername(String username) { this.username = username; }
    public void setFull_name(String full_name) { this.full_name = full_name; }
    public void setEmail(String email) { this.email = email; }
    public void setOrgs(List<String> orgs) { this.orgs = new ArrayList<String>(orgs); }

}
