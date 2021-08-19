package uk.ramp.api;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import uk.ramp.config.Config;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.RestClient;
import uk.ramp.hash.Hasher;

public class Code_run_session {
  Code_run code_run;
  RestClient restClient;
  Config config;
  List<String> outputComponentIdentifiers;
  Hasher hasher;

  Code_run_session(
      RestClient restClient,
      Config config,
      Path configPath,
      Path scriptPath,
      Storage_root storage_root) {
    this.config = config;
    this.hasher = new Hasher();
    this.restClient = restClient;
    this.code_run = new Code_run();

    // make configPath and scriptPath objects
    // config
    String confighash = hasher.fileHash(configPath.toString());
    Map<String, String> find_config_stolo =
        new HashMap<>() {
          {
            put("hash", confighash);
            put("public", "true");
            put("storage_root", storage_root.get_id().toString());
          }
        };
    Storage_location config_stolo =
        (Storage_location) restClient.getFirst(Storage_location.class, find_config_stolo);
    if (config_stolo != null) {
      // there is an already existing StorageLocation for the config; we need to delete the config
      // file.
      System.out.println("there was an existing stolo for config");
      // try {
      // Files.delete(configPath);
      // } catch (IOException e) {
      // logger - log failure to delete configFile
      // }
    } else {
      System.out.println("creating a new stolo");
      config_stolo = new Storage_location();
      config_stolo.setHash(confighash);
      config_stolo.setStorage_root(storage_root.getUrl());
      config_stolo.setIs_public(true);
      config_stolo.setPath(storage_root.getPath().relativize(configPath).toString());
      config_stolo = (Storage_location) restClient.post(config_stolo);
      if (config_stolo == null) {
        throw (new IllegalArgumentException("failed to create config StorageLocation"));
      }
    }
    File_type config_filetype =
        (File_type)
            restClient.getFirst(File_type.class, Collections.singletonMap("extension", "yaml"));
    if (config_filetype == null) {
      System.out.println("creating a new filetype for YAML");
      config_filetype = (File_type) restClient.post(new File_type("yaml", "yaml"));
    } else {
      System.out.println("filetype already existed");
    }
    FDPObject config_object = new FDPObject();
    config_object.setStorage_location(config_stolo.getUrl());
    config_object.setDescription("Working config.yaml file location in local datastore");
    config_object.setFile_type(config_filetype.getUrl());
    config_object = (FDPObject) restClient.post(config_object);
    if (config_object == null) {
      throw (new IllegalArgumentException("failed to create config Object"));
    }
    System.out.println("created the FDPObj");

    code_run.setModel_config(config_object.getUrl());

    String scripthash = hasher.fileHash(scriptPath.toString());
    Map<String, String> find_script_stolo =
        new HashMap<>() {
          {
            put("hash", scripthash);
            put("public", "true");
            put("storage_root", storage_root.get_id().toString());
          }
        };
    Storage_location script_stolo =
        (Storage_location) restClient.getFirst(Storage_location.class, find_script_stolo);
    if (script_stolo != null) {
      System.out.println("found existing script with the same hash; NOT?! deleting the script");
      // try {
      // Files.delete(scriptPath);
      // } catch (IOException e) {
      // logger - log failure to delete scriptPath
      // }
    } else {
      System.out.println("registering the script stolo");
      script_stolo = new Storage_location();
      script_stolo.setHash(scripthash);
      script_stolo.setStorage_root(storage_root.getUrl());
      script_stolo.setIs_public(true);
      script_stolo.setPath(storage_root.getPath().relativize(scriptPath).toString());
      script_stolo = (Storage_location) restClient.post(script_stolo);
      if (script_stolo == null) {
        throw (new IllegalArgumentException("failed to create script StorageLocation"));
      }
    }
    File_type script_filetype =
        (File_type)
            restClient.getFirst(File_type.class, Collections.singletonMap("extension", "sh"));
    if (script_filetype == null) {
      System.out.println("creating new filetype for SH");
      script_filetype = (File_type) restClient.post(new File_type("sh", "sh"));
    } else {
      System.out.println("found filetype for SH");
    }
    FDPObject script_object = new FDPObject();
    script_object.setStorage_location(script_stolo.getUrl());
    script_object.setDescription("Submission script location in local datastore");
    script_object.setFile_type(script_filetype.getUrl());
    script_object = (FDPObject) restClient.post(script_object);
    if (script_object == null) {
      throw (new IllegalArgumentException("failed to create script Object"));
    }
    System.out.println("created the script Object");

    code_run.setModel_config(config_object.getUrl());
    code_run.setSubmission_script(script_object.getUrl());
    code_run.setRun_date(LocalDateTime.now()); // or should this be config.openTimestamp??
    code_run.setDescription(this.config.run_metadata().description().orElse(""));
  }

  protected void addInput(String url) {
    this.code_run.addInput(url);
  }

  protected void addOutput(String url) {
    this.code_run.addOutput(url);
  }

  protected void finish() {
    if (restClient.post(this.code_run) == null) {
      throw (new IllegalArgumentException("failed to create in registry: " + this.code_run));
    }
  }
}
