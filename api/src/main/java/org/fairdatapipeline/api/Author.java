package org.fairdatapipeline.api;

import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryAuthor;
import org.fairdatapipeline.dataregistry.content.RegistryUser_author;
import org.fairdatapipeline.dataregistry.content.RegistryUsers;
import org.fairdatapipeline.dataregistry.restclient.RestClient;

public class Author {
  RegistryAuthor registryAuthor;

  Author(RestClient restClient) {
    this.registryAuthor =
        (RegistryAuthor) restClient.getFirst(RegistryAuthor.class, Collections.emptyMap());
    if (this.registryAuthor == null) {
      throw (new IllegalArgumentException("couldn't find an author in the local registry!"));
    }
  }

  void SoniaAuthor(RestClient restClient) {
    // sonia's get_author gets the admin user, then find the author linked to this use by the
    // user_author table.
    // not using this (yet) as my user_author table is empty.
    RegistryUsers u =
        (RegistryUsers)
            restClient.getFirst(RegistryUsers.class, Collections.singletonMap("username", "admin"));
    RegistryUser_author ua =
        (RegistryUser_author)
            restClient.getFirst(
                RegistryUser_author.class, Collections.singletonMap("user", u.getUrl()));
    this.registryAuthor = (RegistryAuthor) restClient.get(RegistryAuthor.class, ua.getAuthor());
  }

  String getUrl() {
    return this.registryAuthor.getUrl();
  }
}
