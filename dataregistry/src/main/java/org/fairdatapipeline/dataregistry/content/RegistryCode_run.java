package org.fairdatapipeline.dataregistry.content;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

@XmlRootElement
public class RegistryCode_run extends Registry_Updateable {

  @XmlElement private LocalDateTime run_date;

  @XmlElement private String description;

  @XmlElement private APIURL code_repo;

  @XmlElement private APIURL model_config;

  @XmlElement private APIURL submission_script;

  @XmlElement private List<APIURL> inputs;

  @XmlElement private List<APIURL> outputs;

  @XmlElement private String uuid;

  @XmlElement private APIURL prov_report;

  public RegistryCode_run() {
    methods_allowed = List.of("GET", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
  }

  public LocalDateTime getRun_date() {
    return this.run_date;
  }

  public String getDescription() {
    return this.description;
  }

  public APIURL getCode_repo() {
    return this.code_repo;
  }

  public APIURL getModel_config() {
    return this.model_config;
  }

  public APIURL getSubmission_script() {
    return this.submission_script;
  }

  public List<APIURL> getInputs() {
    // return (this.inputs == null) ? new ArrayList<>() {} : new ArrayList<>(this.inputs);
    // we're more likely to be able to use the PATCH method if unset items are null.
    return (this.inputs == null) ? null : new ArrayList<>(this.inputs);
  }

  public List<APIURL> getOutputs() {
    // return (this.outputs == null) ? new ArrayList<>() {} : new ArrayList<>(this.outputs);
    // we're more likely to be able to use the PATCH method if unset items are null.
    return (this.outputs == null) ? null : new ArrayList<>(this.outputs);
  }

  public String getUuid() {
    return this.uuid;
  }

  public APIURL getProv_report() {
    return this.prov_report;
  }

  public void setRun_date(LocalDateTime run_date) {
    this.run_date = run_date;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCode_repo(APIURL code_repo) {
    this.code_repo = code_repo;
  }

  public void setModel_config(APIURL model_config) {
    this.model_config = model_config;
  }

  public void setSubmission_script(APIURL submission_script) {
    this.submission_script = submission_script;
  }

  public void setInputs(List<APIURL> inputs) {
    this.inputs = (inputs == null) ? null : new ArrayList<>(inputs);
  }

  public void setOutputs(List<APIURL> outputs) {
    this.outputs = (outputs == null) ? null : new ArrayList<>(outputs);
  }

  public void addOutput(APIURL output) {
    if (this.outputs == null) {
      this.outputs = new ArrayList<>();
    }
    this.outputs.add(output);
  }

  public void addInput(APIURL input) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<>();
    }
    this.inputs.add(input);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setProv_report(APIURL prov_report) {
    this.prov_report = prov_report;
  }
}
