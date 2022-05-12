package org.fairdatapipeline.api;

import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryAuthor;
import org.fairdatapipeline.dataregistry.content.RegistryUser_author;
import org.fairdatapipeline.dataregistry.content.RegistryUsers;
import org.fairdatapipeline.dataregistry.restclient.APIURL;
import org.fairdatapipeline.dataregistry.restclient.RestClient;

/** Retrieve the Author from the local registry. */
class Author {
  RegistryAuthor registryAuthor;

  /**
   * Current implementation to find the author - find the admin user, then find the linked author
   * via the User_author table.
   *
   * @param restClient link to the restClient to use.
   */
  Author(RestClient restClient) {
    RegistryUsers u =
        (RegistryUsers)
            restClient.getFirst(RegistryUsers.class, Collections.singletonMap("username", "admin"));
    if (u == null) {
      throw (new RegistryObjectNotFoundException("Couldn't find a User in the local registry!"));
    }
    RegistryUser_author ua =
        (RegistryUser_author)
            restClient.getFirst(
                RegistryUser_author.class, Collections.singletonMap("user", u.get_id().toString()));
    if (ua == null) {
      throw (new RegistryObjectNotFoundException(
          "Couldn't find a User_author in the local registry!"));
    }
    this.registryAuthor = (RegistryAuthor) restClient.get(RegistryAuthor.class, ua.getAuthor());
  }

  APIURL getUrl() {
    return this.registryAuthor.getUrl();
  }
}
