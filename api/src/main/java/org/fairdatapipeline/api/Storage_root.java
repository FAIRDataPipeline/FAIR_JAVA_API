package org.fairdatapipeline.api;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fairdatapipeline.dataregistry.content.RegistryStorage_root;
import org.fairdatapipeline.dataregistry.restclient.APIURL;
import org.fairdatapipeline.dataregistry.restclient.RestClient;

/** Retrieve or create the RegistryStorage_root with a given 'root'. */
class Storage_root {
  private static Pattern git_repo_url =
      Pattern.compile("(\\w+://)(.+@)*([\\w\\d\\.]+)(:[\\d]+){0,1}/*(.*)");
  private static Pattern git_repo_file = Pattern.compile("file://(.*)");
  private static Pattern git_repo_ssh = Pattern.compile("(.+@)([\\w\\d\\.]+):(.*)");
  RegistryStorage_root registryStorage_root;

  /**
   * Retrieve or create the RegistryStorage_root with the given storageRootURI.
   *
   * @param storageRootURI the path/root for this Storage_root
   * @param restClient link to the restClient to access the registry.
   */
  Storage_root(URI storageRootURI, RestClient restClient) {
    this.registryStorage_root =
        (RegistryStorage_root)
            restClient.getFirst(
                RegistryStorage_root.class,
                Collections.singletonMap("root", storageRootURI.toString()));
    if (this.registryStorage_root == null) {
      this.registryStorage_root =
          (RegistryStorage_root) restClient.post(new RegistryStorage_root(storageRootURI));
      if (this.registryStorage_root == null) {
        throw (new RegistryException(
            "Failed to create in registry:  Storage_root " + storageRootURI));
      }
    }
  }

  APIURL getUrl() {
    return this.registryStorage_root.getUrl();
  }

  URI getRoot() {
    return this.registryStorage_root.getRoot();
  }

  Path getPath() {
    return this.registryStorage_root.getPath();
  }

  /**
   * split the repository location into a storage root HTTPS: proto://authority/path/to/stuff
   * becomes proto://authority AND /path/to/stuff SSH: git@epic.sruc.ac.uk:bboskamp/BTv.git becomes
   * git@epic.sruc.ac.uk AND /bboskamp/BTv.git
   *
   * @param repo_location the repository location string to split up into scheme/authority and path.
   * @return string array of length 2.
   */
  static String[] gitrepo_to_root(String repo_location) {
    Matcher m1 = git_repo_url.matcher(repo_location);
    if (m1.find())
      return new String[] {
        Objects.toString(m1.group(1), "")
            + Objects.toString(m1.group(2), "")
            + Objects.toString(m1.group(3), "")
            + Objects.toString(m1.group(4), ""),
        m1.group(5)
      };
    m1 = git_repo_file.matcher(repo_location);
    if (m1.find()) return new String[] {"file://", m1.group(1)};
    m1 = git_repo_ssh.matcher(repo_location);
    if (m1.find()) return new String[] {m1.group(1) + m1.group(2), m1.group(3)};
    return new String[] {};
  }
}
