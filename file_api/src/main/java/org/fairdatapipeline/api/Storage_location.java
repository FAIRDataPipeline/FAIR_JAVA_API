package org.fairdatapipeline.api;

import org.fairdatapipeline.dataregistry.content.RegistryStorage_location;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Storage_location {
    RegistryStorage_location registryStorage_location;

    Storage_location(URL remote_repo, String latest_commit, Coderun coderun) {
        String[] split_repo = Storage_root.url_to_root(remote_repo);
        Storage_root storage_root = new Storage_root(split_repo[0], coderun.restClient);
        create_storageroot(latest_commit, storage_root, coderun, split_repo[1], null);
    }

    Storage_location(Path filePath, Storage_root storage_root, Coderun coderun, boolean delete_if_hash_exists) {
        String hash = coderun.hasher.fileHash(filePath.toString());

        Path relativePath = Path.of(storage_root.getPath()).relativize(filePath);
        Path filePath_to_delete = null;
        if(delete_if_hash_exists) filePath_to_delete = filePath;
        create_storageroot(hash, storage_root, coderun, relativePath.toString(), filePath_to_delete);
    }

    void create_storageroot(String hash, Storage_root storage_root, Coderun coderun, String path, Path filePath_to_delete_if_hash_exists) {
        Map<String, String> find_stolo_from_hash =
                new HashMap<>() {
                    {
                        put("hash", hash);
                        put("public", "true");
                        put("storage_root", storage_root.registryStorage_root.get_id().toString());
                    }
                };
        this.registryStorage_location =
                (RegistryStorage_location)
                        coderun.restClient.getFirst(RegistryStorage_location.class, find_stolo_from_hash);
        if (this.registryStorage_location != null) {
            // there is an already existing StorageLocation for this hash; we may need to delete the file
            if(filePath_to_delete_if_hash_exists != null) {
                try {
                    Files.delete(filePath_to_delete_if_hash_exists);
                } catch (IOException e) {
                    // logger - log failure to delete configFile
                }
            }
        } else {
            // create the registryStorage_location
            this.registryStorage_location = new RegistryStorage_location();
            this.registryStorage_location.setHash(hash);
            this.registryStorage_location.setStorage_root(storage_root.getUrl());
            this.registryStorage_location.setIs_public(true);
            this.registryStorage_location.setPath(path);
            this.registryStorage_location = (RegistryStorage_location) coderun.restClient.post(this.registryStorage_location);
            if (this.registryStorage_location == null) {
                throw (new IllegalArgumentException("failed to create StorageLocation for " + path));
            }
        }

    }

    String getUrl(){
        return this.registryStorage_location.getUrl();
    }
}
