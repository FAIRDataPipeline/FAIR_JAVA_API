package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Text_file extends FDP_Updateable{
    @XmlElement
    private String text;

    public Text_file() {}

    public String getText() { return this.text; }

    public void setText(String text) { this.text = text; }
}
