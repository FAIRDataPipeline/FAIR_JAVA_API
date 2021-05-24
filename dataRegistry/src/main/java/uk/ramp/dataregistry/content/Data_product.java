package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Data_product extends FDP_Updateable{
    @XmlElement
    private String name;

    @XmlElement
    private String version;

    @XmlElement
    private String object;

    @XmlElement
    private String namespace;

    public Data_product() {}

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getObject() { return object; }
    public String getNamespace() { return namespace; }
    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setObject(String object) { this.object = object; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
}
