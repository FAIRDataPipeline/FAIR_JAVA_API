package org.fairdatapipeline.api;

import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryFile_type;
import org.fairdatapipeline.dataregistry.restclient.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Encapsulates the registryFile_type object */
class File_type {
  private static final Logger logger = LoggerFactory.getLogger(File_type.class);
  RegistryFile_type registryFile_type;

  /**
   * Given the @param extension, retrieve (or create if not exists) the corresponding
   * registryFile_type from the registry
   *
   * @param extension the filetype extension we're looking for (txt, yaml, toml, cvs)
   * @param restClient the restClient to access the registry.
   */
  File_type(String extension, RestClient restClient) {
    this.registryFile_type =
        (RegistryFile_type)
            restClient.getFirst(
                RegistryFile_type.class, Collections.singletonMap("extension", extension));
    if (this.registryFile_type == null) {
      this.registryFile_type =
          (RegistryFile_type) restClient.post(new RegistryFile_type(extension, extension));
      if (this.registryFile_type == null) {
        String msg = "Failed to create File_type in Registry: " + extension;
        logger.error(msg);
        throw (new RegistryException(msg));
      }
    }
  }

  String getUrl() {
    return this.registryFile_type.getUrl();
  }
}
