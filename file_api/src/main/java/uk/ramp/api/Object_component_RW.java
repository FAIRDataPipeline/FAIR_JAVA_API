package uk.ramp.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ramp.dataregistry.content.Issue;
import uk.ramp.dataregistry.content.Object_component;
import uk.ramp.file.CleanableFileChannel;

public abstract class Object_component_RW {
  protected String component_name;
  protected boolean whole_object = false;
  protected Data_product_RW dp;
  protected Object_component object_component;
  protected boolean been_used = false;
  protected List<Issue> issues;


  public Object_component_RW(Data_product_RW dp, String component_name) {
    this(dp, component_name, false);
  }

  public Object_component_RW(Data_product_RW dp) {
    this(dp, "whole_object", true);
  }

  protected Object_component_RW(Data_product_RW dp, String component_name, boolean whole_object) {
    this.dp = dp;
    this.whole_object = whole_object;
    this.component_name = component_name;
    this.populate_component();
    this.issues = new ArrayList<>();
  }

  protected CleanableFileChannel getFileChannel() throws IOException {
    this.been_used = true;
    return this.dp.getFilechannel();
  }

  protected abstract void populate_component();

  protected Object_component retrieveObject_component() {
    Map<String, String> objcompmap;
    if (this.whole_object) {
      objcompmap =
          new HashMap<>() {
            {
              put("object", dp.fdpObject.get_id().toString());
              put("whole_object", "true");
            }
          };
    } else {
      objcompmap =
          new HashMap<>() {
            {
              put("object", dp.fdpObject.get_id().toString());
              put("name", component_name);
            }
          };
    }
    return (Object_component) dp.fileApi.restClient.getFirst(Object_component.class, objcompmap);
  }

  Object_component getObject_component() {
    return this.object_component;
  }

  protected void raise_issue(String issue, int severity) {
    this.issues.add(new Issue(issue, severity));
  }

  public void register_my_issues() {
    issues.forEach(issue -> {
      issue.addComponent_issue(this.object_component.getUrl());
      if(dp.fileApi.restClient.post(issue) == null) {
        throw(new IllegalArgumentException("failed to create in registry: issue " + issue.getDescription()));
      }
    });
  }

  protected abstract void register_me_in_registry();

  protected abstract void register_me_in_code_run_session(Code_run_session crs);
}
