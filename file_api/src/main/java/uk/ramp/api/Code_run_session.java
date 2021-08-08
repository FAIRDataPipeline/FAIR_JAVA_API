package uk.ramp.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import uk.ramp.config.Config;
import uk.ramp.dataregistry.content.*;
import uk.ramp.dataregistry.restclient.RestClient;
import uk.ramp.hash.Hasher;

public class Code_run_session {
  Code_run code_run;
  Map<String, data_product_objects> data_products_to_create;
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
    this.data_products_to_create = new HashMap<String, data_product_objects>();
    this.outputComponentIdentifiers = new ArrayList<String>();
    System.out.print("FileApi.prepare_code_run(); config: ");
    System.out.println(this.config);

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
      try {
        Files.delete(configPath);
      } catch (IOException e) {
        // logger - log failure to delete configFile
      }
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
    }else{
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
      System.out.println("found existing script with the same hash; deleting the script");
      try {
        Files.delete(scriptPath);
      } catch (IOException e) {
        // logger - log failure to delete scriptPath
      }
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
    }else{
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

  public String make_identifier(String dataproduct_name, String component_name) {
    return Path.of(dataproduct_name).resolve(component_name).toString();
  }

  public void addOutput(String dataproduct_name, String component_name) {
    this.outputComponentIdentifiers.add(make_identifier(dataproduct_name, component_name));
    this.data_products_to_create.get(dataproduct_name).components.add(component_name);
  }

  public List<String> getOutputComponentIdentifiers() {
    return this.outputComponentIdentifiers;
  }

  public boolean contains_output_dp_component(String dataproduct_name, String component_name) {
    return this.outputComponentIdentifiers.contains(
        make_identifier(dataproduct_name, component_name));
  }

  public void addInput(String inputComponentUrl) {
    this.code_run.addInput(inputComponentUrl);
  }

  public List<String> getInputs() {
    return this.code_run.getInputs();
  }

  public void setStorageLocation(String dataProduct_name, Storage_location sl) {
    this.data_products_to_create.get(dataProduct_name).sl = sl;
  }

  private void create_stuff(data_product_objects data_product_to_create) {
    System.out.println("create_stuff() - " + data_product_to_create.fdpObject.getDescription());
    Storage_location sl;
    if (data_product_to_create.sl.getUrl() == null) {
      sl = (Storage_location) restClient.post(data_product_to_create.sl);
      if (sl == null)
        throw (new IllegalArgumentException(
            "Failed to create storage location " + data_product_to_create.sl.getStorage_root()));
    } else {
      sl = data_product_to_create.sl;
    }

    data_product_to_create.fdpObject.setStorage_location(sl.getUrl());
    final FDPObject o = (FDPObject) restClient.post(data_product_to_create.fdpObject);
    if (o == null)
      throw (new IllegalArgumentException(
          "Failed to create Object " + data_product_to_create.fdpObject.getDescription()));
    data_product_to_create.dp.setObject(o.getUrl());
    if (restClient.post(data_product_to_create.dp) == null) {
      throw (new IllegalArgumentException(
          "Failed to create Data_product " + data_product_to_create.dp.getName()));
    }
    data_product_to_create.components.stream()
        .forEach(
            component_name -> {
              System.out.println("\n COMPONENT \n");
              System.out.println("for obj " + o.getUrl());
              System.out.println(component_name);
              System.out.println("\n");
              if (component_name != "whole_object") {
                // whole_object is created automatically
                Object_component objComponent = new Object_component();
                // TODO: is there a way to create OC description?
                objComponent.setName(component_name);
                objComponent.setObject(o.getUrl());
                objComponent = (Object_component) restClient.post(objComponent);
                if (objComponent == null) {
                  System.out.println(
                      "CREATE COMPONENT ("
                          + component_name
                          + " under obj id "
                          + o.get_id().toString()
                          + ") FAILED\n\n");
                } else {
                  code_run.addOutput(objComponent.getUrl());
                }
              } else {
                // component == whole_object
                Map<String, String> find_whole_object =
                    new HashMap<>() {
                      {
                        put("object", o.get_id().toString());
                        put("whole_object", "true");
                      }
                    };
                Object_component objComponent =
                    (Object_component)
                        restClient.getFirst(Object_component.class, find_whole_object);

                if (objComponent == null) {
                  throw (new IllegalArgumentException(
                      "can't find the 'whole_object' component for obj " + o.get_id().toString()));
                }
                code_run.addOutput(objComponent.getUrl());
              }
            });
  }

  public void addstuff(String dataproduct_name, Storage_location sl, FDPObject o, Data_product dp) {
    data_products_to_create.put(dataproduct_name, new data_product_objects(sl, o, dp));
  }

  public void finish() {
    // create all objects
    System.out.println("Code_run_session.finish()");
    System.out.println("size of dp_to_create: " + data_products_to_create.size());
    data_products_to_create.entrySet().stream().forEach(li -> create_stuff(li.getValue()));
    if (restClient.post(code_run) == null) {
      throw (new IllegalArgumentException("failed to create code run"));
    }
  }

  private class data_product_objects {
    public Storage_location sl;
    private FDPObject fdpObject;
    private Data_product dp;
    private List<String> components;

    public data_product_objects(Storage_location sl, FDPObject fdpObject, Data_product dp) {
      this.sl = sl;
      this.fdpObject = fdpObject;
      this.dp = dp;
      this.components = new ArrayList<String>();
    }
  }
}
