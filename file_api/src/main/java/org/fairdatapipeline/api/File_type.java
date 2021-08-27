package org.fairdatapipeline.api;

import java.util.Collections;
import org.fairdatapipeline.dataregistry.content.RegistryFile_type;
import org.fairdatapipeline.dataregistry.restclient.RestClient;

class File_type {
  RegistryFile_type registryFile_type;

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
}
