package org.fairdatapipeline.api;

import java.util.List;
import org.fairdatapipeline.dataregistry.content.RegistryObject;
import org.fairdatapipeline.dataregistry.restclient.APIURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** create a new registryObject with given storage_location, description, authors, file_type. */
public class FileObject {
  private static final Logger logger = LoggerFactory.getLogger(FileObject.class);
  Coderun coderun;
  RegistryObject o;

  /**
   * This constructor is used to store actual files, such as the config and script files the coderun
   * needs to store.
   *
   * @param file_type The File_type (extension) for this file. (the file type must have already been
   *     registered in the registry)
   * @param storage_location The Storage_location given must be already registered in the registry.
   * @param description name/description for this object.
   * @param authors a list of Author API URLS.
   * @param coderun link back to the coderun; to access its restClient.
   */
  FileObject(
      File_type file_type,
      Storage_location storage_location,
      String description,
      List<APIURL> authors,
      Coderun coderun) {
    this.coderun = coderun;
    this.o = new RegistryObject();
    this.o.setStorage_location(storage_location.getUrl());
    this.o.setDescription(description);
    if (file_type != null) this.o.setFile_type(file_type.getUrl());
    this.o.setAuthors(authors);
    this.o = (RegistryObject) coderun.restClient.post(this.o);
    if (this.o == null) {
      String msg = "Failed to create Object in registry: " + storage_location.getUrl();
      logger.error(msg);
      throw (new RegistryException(msg));
    }
  }

  /**
   * this constructor is used to store an Object without a file_type, such as the Object for a
   * code_repo
   *
   * @param storage_location The storage location must be already registered in the registry.
   * @param description name/description for this Object.
   * @param authors list of Author API urls.
   * @param coderun link back to the coderun; to access its restClient.
   */
  FileObject(
      Storage_location storage_location,
      String description,
      List<APIURL> authors,
      Coderun coderun) {
    this(null, storage_location, description, authors, coderun);
  }

  /**
   * raise an issue with this component
   *
   * @param description the text description of this issue
   * @param severity Integer - higher means more severe
   */
  public void raise_issue(String description, Integer severity) {
    if (o.getComponents().isEmpty()) {
      String msg = "Object component not found.";
      logger.error(msg);
      throw (new IllegalActionException(msg));
    }
    Issue i = this.coderun.raise_issue(description, severity);
    i.add_registryObject_component(getWholeObjectComponentUrl());
  }

  APIURL getWholeObjectComponentUrl() {
    return this.o.getComponents().get(0);
  }

  APIURL getUrl() {
    return this.o.getUrl();
  }
}
