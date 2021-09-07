package org.fairdatapipeline.api;

import java.util.List;
import org.fairdatapipeline.dataregistry.content.RegistryObject;

/** create a new registryObject with given storage_location, description, authors, file_type. */
public class FileObject {
  RegistryObject o;

  /**
   * This constructor is used to store actual files, such as the config and script files the coderun
   * needs to store.
   *
   * @param file_type
   * @param storage_location
   * @param description
   * @param authors
   * @param coderun
   */
  FileObject(
      File_type file_type,
      Storage_location storage_location,
      String description,
      List<String> authors,
      Coderun coderun) {
    this.o = new RegistryObject();
    this.o.setStorage_location(storage_location.getUrl());
    this.o.setDescription(description);
    if (file_type != null) this.o.setFile_type(file_type.getUrl());
    this.o.setAuthors(authors);
    this.o = (RegistryObject) coderun.restClient.post(this.o);
    if (this.o == null) {
      throw (new IllegalArgumentException(
          "failed to create FileObject for " + storage_location.getUrl()));
    }
  }

  /**
   * this constructor is used to store an Object without a file_type, such as the Object for a
   * code_repo
   *
   * @param storage_location
   * @param description
   * @param authors
   * @param coderun
   */
  FileObject(
      Storage_location storage_location,
      String description,
      List<String> authors,
      Coderun coderun) {
    this(null, storage_location, description, authors, coderun);
  }

  String getUrl() {
    return this.o.getUrl();
  }
}
