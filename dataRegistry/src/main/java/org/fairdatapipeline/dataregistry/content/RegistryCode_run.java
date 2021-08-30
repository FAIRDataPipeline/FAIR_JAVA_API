package org.fairdatapipeline.dataregistry.content;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryCode_run extends Registry_Updateable {

  @XmlElement private LocalDateTime run_date;

  @XmlElement private String description;

  @XmlElement private String code_repo;

  @XmlElement private String model_config;

  @XmlElement private String submission_script;

  @XmlElement private List<String> inputs;

  @XmlElement private List<String> outputs;

  @XmlElement private String uuid;

  @XmlElement private String prov_report;

  public RegistryCode_run() {
    methods_allowed = List.of("GET", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
  }

  public LocalDateTime getRun_date() {
    return this.run_date;
  }

  public String getDescription() {
    return this.description;
  }

  public String getCode_repo() {
    return this.code_repo;
  }

  public String getModel_config() {
    return this.model_config;
  }

  public String getSubmission_script() {
    return this.submission_script;
  }

  public List<String> getInputs() {
    // return (this.inputs == null) ? new ArrayList<>() {} : new ArrayList<>(this.inputs);
    // we're more likely to be able to use the PATCH method if unset items are null.
    return (this.inputs == null) ? null : new ArrayList<>(this.inputs);
  }

  public List<String> getOutputs() {
    // return (this.outputs == null) ? new ArrayList<>() {} : new ArrayList<>(this.outputs);
    // we're more likely to be able to use the PATCH method if unset items are null.
    return (this.outputs == null) ? null : new ArrayList<>(this.outputs);
  }

  public String getUuid() {
    return this.uuid;
  }

  public String getProv_report() {
    return this.prov_report;
  }

  public void setRun_date(LocalDateTime run_date) {
    this.run_date = run_date;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCode_repo(String code_repo) {
    this.code_repo = code_repo;
  }

  public void setModel_config(String model_config) {
    this.model_config = model_config;
  }

  public void setSubmission_script(String submission_script) {
    this.submission_script = submission_script;
  }

  public void setInputs(List<String> inputs) {
    this.inputs = (inputs == null) ? null : new ArrayList<String>(inputs);
  }

  public void setOutputs(List<String> outputs) {
    this.outputs = (outputs == null) ? null : new ArrayList<String>(outputs);
  }

  public void addOutput(String output) {
    if (this.outputs == null) {
      this.outputs = new ArrayList<>();
    }
    this.outputs.add(output);
  }

  public void addInput(String input) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<>();
    }
    this.inputs.add(input);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setProv_report(String prov_report) {
    this.prov_report = prov_report;
  }
}
