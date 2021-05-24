package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Key_value extends FDP_Updateable{
    @XmlElement
    private String key;

    @XmlElement
    private String value;

    @XmlElement
    private String object;


    public Key_value() {}

    public String getKey() { return this.key; }
    public String getValue() { return this.value; }
    public String getObject() { return this.object; }

    public void setKey(String key) { this.key = key; }
    public void setValue(String value) { this.value = value; }
    public void setObject(String object) { this.object = object; }
}
