package org.fairdatapipeline.api;

import java.net.URL;
import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_root;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** retrieve or create the RegistryStorage_root with a given 'root'. */
public class Storage_root {
  private static final Logger logger = LoggerFactory.getLogger(Storage_root.class);
  RegistryStorage_root registryStorage_root;

  /**
   * Retrieve or create the RegistryStorage_root with the given storageRootPath.
   *
   * @param storageRootPath the path/root for this Storage_root
   * @param restClient link to the restClient to access the registry.
   */
  Storage_root(String storageRootPath, RestClient restClient) {
    this.registryStorage_root =
        (RegistryStorage_root)
            restClient.getFirst(
                RegistryStorage_root.class, Collections.singletonMap("root", storageRootPath));
    if (this.registryStorage_root == null) {
      this.registryStorage_root =
          (RegistryStorage_root) restClient.post(new RegistryStorage_root(storageRootPath));
      if (this.registryStorage_root == null) {
        String msg = "Failed to create in registry:  Storage_root " + storageRootPath;
        logger.error(msg);
        throw (new RegistryException(msg));
      }
    }
  }

  String getUrl() {
    return this.registryStorage_root.getUrl();
  }

  String getPath() {
    return this.registryStorage_root.getPath();
  }

  /**
   * split the repository URL into a storage root (proto://authority/ part) and path (/xxx/xxx) part
   *
   * @param url the URL to split up into scheme/authority and path.
   * @return string array of length 2.
   */
  static String[] url_to_root(URL url) {
    String path = url.getPath().substring(1);
    String scheme_and_authority_part =
        url.toString().substring(0, url.toString().length() - path.length());
    return new String[] {scheme_and_authority_part, path};
  }
}
