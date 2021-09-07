package org.fairdatapipeline.api;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_location;

/** This is used to store a file or a remote repo to the registry as a RegistryStorage_location. */
public class Storage_location {
  RegistryStorage_location registryStorage_location;

  /**
   * Constructor used for storing remote_repo
   *
   * @param remote_repo the URL of the repository to store. (the protocol://server.com/ part is
   *     stored as storage_root)
   * @param latest_commit the unique hash of the repository. if the repo/hash combo already exists,
   *     we will use the existing one instead of storing this one.
   * @param coderun
   */
  Storage_location(URL remote_repo, String latest_commit, Coderun coderun) {
    String[] split_repo = Storage_root.url_to_root(remote_repo);
    Storage_root storage_root = new Storage_root(split_repo[0], coderun.restClient);
    create_storagelocation(latest_commit, storage_root, coderun, split_repo[1], null);
  }

  /**
   * Constructor used for storing an existing file.
   *
   * @param filePath the filePath of the file to store.
   * @param storage_root the storage root.
   * @param coderun
   * @param delete_if_hash_exists we'll only create the RegistryStorage_location if the root/hash
   *     combo is unique, but if delete_if_hash_exists we will also delete the file if the root/hash
   *     combo already exists.
   */
  Storage_location(
      Path filePath, Storage_root storage_root, Coderun coderun, boolean delete_if_hash_exists) {
    String hash = coderun.hasher.fileHash(filePath.toString());

    Path relativePath = Path.of(storage_root.getPath()).relativize(filePath);
    Path filePath_to_delete = null;
    if (delete_if_hash_exists) filePath_to_delete = filePath;
    create_storagelocation(
        hash, storage_root, coderun, relativePath.toString(), filePath_to_delete);
  }

  void create_storagelocation(
      String hash,
      Storage_root storage_root,
      Coderun coderun,
      String path,
      Path filePath_to_delete_if_hash_exists) {
    Map<String, String> find_stolo_from_hash =
        new HashMap<>() {
          {
            put("hash", hash);
            put("public", "true");
            put("storage_root", storage_root.registryStorage_root.get_id().toString());
          }
        };
    this.registryStorage_location =
        (RegistryStorage_location)
            coderun.restClient.getFirst(RegistryStorage_location.class, find_stolo_from_hash);
    if (this.registryStorage_location != null) {
      // there is an already existing StorageLocation for this hash; we may need to delete the file
      if (filePath_to_delete_if_hash_exists != null) {
        try {
          Files.delete(filePath_to_delete_if_hash_exists);
        } catch (IOException e) {
          // logger - log failure to delete configFile
        }
      }
    } else {
      // create the registryStorage_location
      this.registryStorage_location = new RegistryStorage_location();
      this.registryStorage_location.setHash(hash);
      this.registryStorage_location.setStorage_root(storage_root.getUrl());
      this.registryStorage_location.setIs_public(true);
      this.registryStorage_location.setPath(path);
      this.registryStorage_location =
          (RegistryStorage_location) coderun.restClient.post(this.registryStorage_location);
      if (this.registryStorage_location == null) {
        throw (new IllegalArgumentException("failed to create StorageLocation for " + path));
      }
    }
  }

  String getUrl() {
    return this.registryStorage_location.getUrl();
  }
}
