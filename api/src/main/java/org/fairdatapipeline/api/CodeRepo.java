package org.fairdatapipeline.api;

import java.net.URL;
import java.util.List;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/**
 * Create a CodeRepo registryStorage_location storing the repo URL and latest_commit hash (if not
 * exist yet). Also create a CodeRepo registryObject storing the storage_location, authors, and
 * description. (if an object pointing to the Storage_location does not exists yet)
 */
class CodeRepo {
  final Storage_location storage_location;
  final FileObject fileObject;

  CodeRepo(
      String latest_commit,
      URL repo_url,
      String description,
      List<APIURL> authors,
      Coderun coderun) {
    this.storage_location = new Storage_location(repo_url, latest_commit, coderun);
    this.fileObject = new FileObject(this.storage_location, description, authors, coderun);
  }

  /**
   * Retrieve the FileObject for this CodeRepo. (probably to raise an issue with it)
   *
   * @return The FileObject for this CodeRepo. (probably to raise an issue with it)
   */
  public FileObject getFileObject() {
    return this.fileObject;
  }

  APIURL getUrl() {
    return this.fileObject.getUrl();
  }
}
