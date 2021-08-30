package org.fairdatapipeline.api;

import java.net.URL;
import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_root;
import org.fairdatapipeline.dataregistry.restclient.RestClient;

public class Storage_root {
  RegistryStorage_root registryStorage_root;

  Storage_root(String storageRootPath, RestClient restClient) {
    this.registryStorage_root =
        (RegistryStorage_root)
            restClient.getFirst(
                RegistryStorage_root.class, Collections.singletonMap("root", storageRootPath));
    if (this.registryStorage_root == null) {
      this.registryStorage_root =
          (RegistryStorage_root) restClient.post(new RegistryStorage_root(storageRootPath));
      if (this.registryStorage_root == null) {
        throw (new IllegalArgumentException("failed to register Storage_root " + storageRootPath));
      }
    }
  }

  String getUrl() {
    return this.registryStorage_root.getUrl();
  }

  String getPath() {
    return this.registryStorage_root.getPath();
  }

  static String[] url_to_root(URL url) {
    // url.getPath()
    // Pattern p = Pattern.compile("([a-z]*://[a-z]*.[a-z]*/).*");
    /*Matcher m = p.matcher(url);
    if(m.matches()) return m.group(1);
    return "";*/
    String path = url.getPath().substring(1);
    System.out.println("url.getPath(): " + path);
    String scheme_and_authority_part =
        url.toString().substring(0, url.toString().length() - path.length());
    System.out.println("url scheme/authority part: " + scheme_and_authority_part);
    return new String[] {scheme_and_authority_part, path};
  }
}
