package uk.ramp.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FDP_ObjectList<T extends FDP_RootObject> {
  @XmlElement private Integer count;

  @XmlElement private String next;

  @XmlElement private String previous;

  @XmlElement private List<T> results;

  public FDP_ObjectList() {};

  public Integer getCount() {
    return count;
  }

  public String getNext() {
    return next;
  }

  public String getPrevious() {
    return previous;
  }

  public List<T> getResults() {
    return (this.results == null) ? new ArrayList<T>() {} : new ArrayList<T>(results);
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public void setResults(List<T> results) {
    this.results = new ArrayList<>(results);
  }
}
