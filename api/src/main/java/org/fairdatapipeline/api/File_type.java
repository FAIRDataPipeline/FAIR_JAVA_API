package org.fairdatapipeline.api;

import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryFile_type;
import org.fairdatapipeline.dataregistry.restclient.RestClient;

/** Encapsulates the registryFile_type object */
class File_type {
  RegistryFile_type registryFile_type;

  /**
   * Given the @param extension, retrieve (or create if not exists) the corresponding
   * registryFile_type from the registry
   *
   * @param extension
   * @param restClient
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
        throw (new IllegalArgumentException("failed to register file_type " + extension));
      }
    }
  }

  String getUrl() {
    return this.registryFile_type.getUrl();
  }
}
