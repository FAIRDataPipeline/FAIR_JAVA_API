package org.fairdatapipeline.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class Registry_ObjectList<T extends Registry_RootObject> {
  @XmlElement private Integer count;

  @XmlElement private APIURL next;

  @XmlElement private APIURL previous;

  @XmlElement private List<Registry_RootObject> results;

  public Registry_ObjectList() {}

  public Integer getCount() {
    return count;
  }

  public APIURL getNext() {
    return next;
  }

  public APIURL getPrevious() {
    return previous;
  }

  public List<Registry_RootObject> getResults() {
    return (this.results == null) ? new ArrayList<>() {} : new ArrayList<>(results);
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public void setNext(APIURL next) {
    this.next = next;
  }

  public void setPrevious(APIURL previous) {
    this.previous = previous;
  }

  public void setResults(List<T> results) {
    this.results = new ArrayList<>(results);
  }
}
