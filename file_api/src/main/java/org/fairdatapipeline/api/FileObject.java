package org.fairdatapipeline.api;

import org.fairdatapipeline.dataregistry.content.RegistryObject;

import java.util.List;

public class FileObject {
    RegistryObject o;

    FileObject(File_type file_type, Storage_location storage_location, String description, List<String> authors, Coderun coderun) {
        this.o = new RegistryObject();
        this.o.setStorage_location(storage_location.getUrl());
        this.o.setDescription(description);
        if(file_type != null) this.o.setFile_type(file_type.getUrl());
        this.o.setAuthors(authors);
        this.o = (RegistryObject) coderun.restClient.post(this.o);
        if (this.o == null) {
            throw (new IllegalArgumentException("failed to create FileObject for " + storage_location.getUrl()));
        }
    }

    FileObject(Storage_location storage_location, String description, List<String> authors, Coderun coderun) {
        this(null, storage_location, description, authors, coderun);
    }



    String getUrl() {
        return this.o.getUrl();
    }
}
