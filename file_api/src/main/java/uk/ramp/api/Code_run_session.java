package uk.ramp.api;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import uk.ramp.config.Config;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.RestClient;
import uk.ramp.hash.Hasher;

class Code_run_session {
  RegistryCode_run registryCode_run;
  RestClient restClient;
  Config config;
  List<String> outputComponentIdentifiers;
  Hasher hasher;

  Code_run_session(
      RestClient restClient,
      Config config,
      Path configPath,
      Path scriptPath,
      RegistryStorage_root registryStorage_root) {
    this.config = config;
    this.hasher = new Hasher();
    this.restClient = restClient;
    this.registryCode_run = new RegistryCode_run();

    // make configPath and scriptPath objects
    // config
    String confighash = hasher.fileHash(configPath.toString());
    Map<String, String> find_config_stolo =
        new HashMap<>() {
          {
            put("hash", confighash);
            put("public", "true");
            put("storage_root", registryStorage_root.get_id().toString());
          }
        };
    RegistryStorage_location config_stolo =
        (RegistryStorage_location)
            restClient.getFirst(RegistryStorage_location.class, find_config_stolo);
    if (config_stolo != null) {
      // there is an already existing StorageLocation for the config; we need to delete the config
      // file.
      // try {
      // Files.delete(configPath);
      // } catch (IOException e) {
      // logger - log failure to delete configFile
      // }
    } else {
      config_stolo = new RegistryStorage_location();
      config_stolo.setHash(confighash);
      config_stolo.setStorage_root(registryStorage_root.getUrl());
      config_stolo.setIs_public(true);
      config_stolo.setPath(registryStorage_root.getPath().relativize(configPath).toString());
      config_stolo = (RegistryStorage_location) restClient.post(config_stolo);
      if (config_stolo == null) {
        throw (new IllegalArgumentException("failed to create config StorageLocation"));
      }
    }
    File_type config_filetype = new File_type("yaml", restClient);
    RegistryObject config_object = new RegistryObject();
    config_object.setStorage_location(config_stolo.getUrl());
    config_object.setDescription("Working config.yaml file location in local datastore");
    config_object.setFile_type(config_filetype.registryFile_type.getUrl());
    config_object = (RegistryObject) restClient.post(config_object);
    if (config_object == null) {
      throw (new IllegalArgumentException("failed to create config Object"));
    }

    registryCode_run.setModel_config(config_object.getUrl());

    String scripthash = hasher.fileHash(scriptPath.toString());
    Map<String, String> find_script_stolo =
        new HashMap<>() {
          {
            put("hash", scripthash);
            put("public", "true");
            put("storage_root", registryStorage_root.get_id().toString());
          }
        };
    RegistryStorage_location script_stolo =
        (RegistryStorage_location)
            restClient.getFirst(RegistryStorage_location.class, find_script_stolo);
    if (script_stolo != null) {
      // try {
      // Files.delete(scriptPath);
      // } catch (IOException e) {
      // logger - log failure to delete scriptPath
      // }
    } else {
      script_stolo = new RegistryStorage_location();
      script_stolo.setHash(scripthash);
      script_stolo.setStorage_root(registryStorage_root.getUrl());
      script_stolo.setIs_public(true);
      script_stolo.setPath(registryStorage_root.getPath().relativize(scriptPath).toString());
      script_stolo = (RegistryStorage_location) restClient.post(script_stolo);
      if (script_stolo == null) {
        throw (new IllegalArgumentException("failed to create script StorageLocation"));
      }
    }
    File_type script_filetype = new File_type("sh", restClient);
    RegistryObject script_object = new RegistryObject();
    script_object.setStorage_location(script_stolo.getUrl());
    script_object.setDescription("Submission script location in local datastore");
    script_object.setFile_type(script_filetype.registryFile_type.getUrl());
    script_object = (RegistryObject) restClient.post(script_object);
    if (script_object == null) {
      throw (new IllegalArgumentException("failed to create script Object"));
    }

    registryCode_run.setModel_config(config_object.getUrl());
    registryCode_run.setSubmission_script(script_object.getUrl());
    registryCode_run.setRun_date(LocalDateTime.now()); // or should this be config.openTimestamp??
    registryCode_run.setDescription(this.config.run_metadata().description().orElse(""));
  }

  protected void addInput(String url) {
    this.registryCode_run.addInput(url);
  }

  protected void addOutput(String url) {
    this.registryCode_run.addOutput(url);
  }

  protected void finish() {
    if (restClient.post(this.registryCode_run) == null) {
      throw (new IllegalArgumentException(
          "failed to create in registry: " + this.registryCode_run));
    }
  }
}
