package org.fairdatapipeline.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** The file type of an object. */
@XmlRootElement
public class RegistryFile_type extends Registry_Updateable {
  @XmlElement private String name;

  @XmlElement private String extension;

  /** Empty constructor */
  public RegistryFile_type() {}

  /**
   * Constructor
   *
   * @param name Name of the file type. (text description like 'Microsoft Excel Open XML
   *     Spreadsheet')
   * @param extension Filename extension. (like csv)
   */
  public RegistryFile_type(String name, String extension) {
    this.name = name;
    this.extension = extension;
  }

  /**
   * Name of the file type. Examples:
   *
   * <ul>
   *   <li>Hierarchical Data Format version 5
   *   <li>Comma-separated values file
   *   <li>Microsoft Excel Open XML Spreadsheet
   * </ul>
   *
   * @return Name of the file type.
   */
  public String getName() {
    return name;
  }

  /**
   * Filename extension. Examples:
   *
   * <ul>
   *   <li>h5
   *   <li>csv
   *   <li>xlsx
   * </ul>
   *
   * @return The filename extension.
   */
  public String getExtension() {
    return extension;
  }

  /**
   * @param name Name of the file type. (text description like 'Microsoft Excel Open XML
   *     Spreadsheet')
   */
  public void setName(String name) {
    this.name = name;
  }

  /** @param extension Filename extension. (like csv) */
  public void setExtension(String extension) {
    this.extension = extension;
  }
}
