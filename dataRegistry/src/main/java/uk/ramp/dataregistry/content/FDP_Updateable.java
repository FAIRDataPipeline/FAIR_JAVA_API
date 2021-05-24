package uk.ramp.dataregistry.content;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDateTime;

@XmlRootElement
public class FDP_Updateable extends FDP_RootObject {

    @XmlElement
    private LocalDateTime last_updated;

    @XmlElement
    private String updated_by;

    public FDP_Updateable() {}

    public LocalDateTime getLast_updated() { return last_updated; }
    public String getUpdated_by() { return updated_by; }

    public void setLast_updated(LocalDateTime last_updated) { this.last_updated = last_updated; }
    public void setUpdated_by(String updated_by) { this.updated_by = updated_by ; }

}
