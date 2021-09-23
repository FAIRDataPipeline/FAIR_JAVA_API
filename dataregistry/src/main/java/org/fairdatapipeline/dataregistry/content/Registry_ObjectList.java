package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Registry_ObjectList<T extends Registry_RootObject> {
  @XmlElement private Integer count;

  @XmlElement private URL next;

  @XmlElement private URL previous;

  @XmlElement private List<Registry_RootObject> results;

  public Registry_ObjectList() {}

  public Integer getCount() {
    return count;
  }

  public URL getNext() {
    return next;
  }

  public URL getPrevious() {
    return previous;
  }

  public List<Registry_RootObject> getResults() {
    return (this.results == null) ? new ArrayList<>() {} : new ArrayList<>(results);
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public void setNext(URL next) {
    this.next = next;
  }

  public void setPrevious(URL previous) {
    this.previous = previous;
  }

  public void setResults(List<T> results) {
    this.results = new ArrayList<>(results);
  }
}
