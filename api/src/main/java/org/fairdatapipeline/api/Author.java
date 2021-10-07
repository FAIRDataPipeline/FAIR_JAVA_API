package org.fairdatapipeline.api;

import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryAuthor;
import org.fairdatapipeline.dataregistry.content.RegistryUser_author;
import org.fairdatapipeline.dataregistry.content.RegistryUsers;
import org.fairdatapipeline.dataregistry.restclient.APIURL;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Retrieve the Author from the local registry. */
public class Author {
  private static final Logger logger = LoggerFactory.getLogger(Author.class);
  RegistryAuthor registryAuthor;

  /**
   * This implementation just retrieves the first Author it finds in the Registry.
   *
   * @param restClient link to the restClient to use.
   */
  void DummyAuthor(RestClient restClient) {
    this.registryAuthor =
        (RegistryAuthor) restClient.getFirst(RegistryAuthor.class, Collections.emptyMap());
    if (this.registryAuthor == null) {
      String msg = "Couldn't find an author in the local registry!";
      logger.error(msg);
      throw (new RegistryObjectNotfoundException(msg));
    }
  }

  /**
   * Not used. Sonia's version of the Author-retrieval code which finds the admin user, and then
   * retrieves the Author it links to via the User_author table. (not using this as my user_author
   * table is empty)
   *
   * @param restClient link to the restClient to use.
   */
  Author(RestClient restClient) {
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
                RegistryUser_author.class, Collections.singletonMap("user", u.get_id().toString()));
    if (ua == null) {
      String msg = "Couldn't find a User_author in the local registry!";
      logger.error(msg);
      throw (new RegistryObjectNotfoundException(msg));
    }
    this.registryAuthor = (RegistryAuthor) restClient.get(RegistryAuthor.class, ua.getAuthor());
  }

  APIURL getUrl() {
    return this.registryAuthor.getUrl();
  }
}
