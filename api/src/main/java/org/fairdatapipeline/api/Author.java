package org.fairdatapipeline.api;

import java.net.URL;
import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryAuthor;
import org.fairdatapipeline.dataregistry.content.RegistryUser_author;
import org.fairdatapipeline.dataregistry.content.RegistryUsers;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This is used to retrieve the author from the local registry. */
public class Author {
  private static final Logger logger = LoggerFactory.getLogger(Author.class);
  RegistryAuthor registryAuthor;

  Author(RestClient restClient) {
    this.registryAuthor =
        (RegistryAuthor) restClient.getFirst(RegistryAuthor.class, Collections.emptyMap());
    if (this.registryAuthor == null) {
      String msg = "Couldn't find an author in the local registry!";
      logger.error(msg);
      throw (new RegistryObjectNotfoundException(msg));
    }
  }

  void SoniaAuthor(RestClient restClient) {
    // sonia's get_author gets the admin user, then find the author linked to this use by the
    // user_author table.
    // not using this (yet) as my user_author table is empty.
    RegistryUsers u =
        (RegistryUsers)
            restClient.getFirst(RegistryUsers.class, Collections.singletonMap("username", "admin"));
    if (u == null) {
      String msg = "Couldn't find a User in the local registry!";
      logger.error(msg);
      throw (new RegistryObjectNotfoundException(msg));
    }
    RegistryUser_author ua =
        (RegistryUser_author)
            restClient.getFirst(
                RegistryUser_author.class, Collections.singletonMap("user", u.getUrl().toString()));
    if (ua == null) {
      String msg = "Couldn't find a User_author in the local registry!";
      logger.error(msg);
      throw (new RegistryObjectNotfoundException(msg));
    }
    this.registryAuthor = (RegistryAuthor) restClient.get(RegistryAuthor.class, ua.getAuthor());
  }

  URL getUrl() {
    return this.registryAuthor.getUrl();
  }
}
