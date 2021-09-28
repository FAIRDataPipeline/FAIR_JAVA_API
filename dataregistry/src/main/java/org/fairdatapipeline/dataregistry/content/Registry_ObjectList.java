package org.fairdatapipeline.dataregistry.content;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * List to contain result of search queries in the Registry.
 *
 * @param <T> We can only search for 1 type of Registry resource; the List will only contain
 *     registry objects of Class T (extends Registry_RootObject)
 */
@XmlRootElement
public class Registry_ObjectList<T extends Registry_RootObject> {
  @XmlElement private Integer count;

  @XmlElement private APIURL next;

  @XmlElement private APIURL previous;

  @XmlElement private List<T> results;

  /**
   * Number of items in this list.
   *
   * @return Number of items in this list.
   */
  public Integer getCount() {
    return count;
  }

  /**
   * Reference to the next page in this result set.
   *
   * @return Reference to the next page in this result set.
   */
  public APIURL getNext() {
    return next;
  }

  /**
   * Reference to the previous page in this result set.
   *
   * @return Reference to the previous page in this result set.
   */
  public APIURL getPrevious() {
    return previous;
  }

  /**
   * List of actual {link Registry_RootObject Registry_RootObjects} found.
   *
   * @return List of actual {link Registry_RootObject Registry_RootObjects} found.
   */
  public List<T> getResults() {
    return (this.results == null) ? new ArrayList<>() {} : new ArrayList<>(results);
  }
}
