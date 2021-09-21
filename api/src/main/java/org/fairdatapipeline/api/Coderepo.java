package org.fairdatapipeline.api;

import java.net.URL;
import java.util.List;

/**
 * create a coderepo registryStorage_location storing the repo URL and latest_commit hash (if not
 * exist yet) create a coderepo registryObject storing the storage_location, authors, and
 * description. (if an object pointing to stolo not exists yet)
 */
public class Coderepo {
  Storage_location storage_location;
  FileObject fileObject;

  Coderepo(
      String latest_commit,
      URL repo_url,
      String description,
      List<String> authors,
      Coderun coderun) {
    this.storage_location = new Storage_location(repo_url, latest_commit, coderun);
    this.fileObject = new FileObject(this.storage_location, description, authors, coderun);
  }

  public FileObject getFileObject() {
    return this.fileObject;
  }

  String getUrl() {
    return this.fileObject.getUrl();
  }
}
