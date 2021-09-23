package org.fairdatapipeline.dataregistry.content;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegistryCode_run extends Registry_Updateable {

  @XmlElement private LocalDateTime run_date;

  @XmlElement private String description;

  @XmlElement private URL code_repo;

  @XmlElement private URL model_config;

  @XmlElement private URL submission_script;

  @XmlElement private List<URL> inputs;

  @XmlElement private List<URL> outputs;

  @XmlElement private String uuid;

  @XmlElement private URL prov_report;

  public RegistryCode_run() {
    methods_allowed = List.of("GET", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
  }

  public LocalDateTime getRun_date() {
    return this.run_date;
  }

  public String getDescription() {
    return this.description;
  }

  public URL getCode_repo() {
    return this.code_repo;
  }

  public URL getModel_config() {
    return this.model_config;
  }

  public URL getSubmission_script() {
    return this.submission_script;
  }

  public List<URL> getInputs() {
    // return (this.inputs == null) ? new ArrayList<>() {} : new ArrayList<>(this.inputs);
    // we're more likely to be able to use the PATCH method if unset items are null.
    return (this.inputs == null) ? null : new ArrayList<>(this.inputs);
  }

  public List<URL> getOutputs() {
    // return (this.outputs == null) ? new ArrayList<>() {} : new ArrayList<>(this.outputs);
    // we're more likely to be able to use the PATCH method if unset items are null.
    return (this.outputs == null) ? null : new ArrayList<>(this.outputs);
  }

  public String getUuid() {
    return this.uuid;
  }

  public URL getProv_report() {
    return this.prov_report;
  }

  public void setRun_date(LocalDateTime run_date) {
    this.run_date = run_date;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCode_repo(URL code_repo) {
    this.code_repo = code_repo;
  }

  public void setModel_config(URL model_config) {
    this.model_config = model_config;
  }

  public void setSubmission_script(URL submission_script) {
    this.submission_script = submission_script;
  }

  public void setInputs(List<URL> inputs) {
    this.inputs = (inputs == null) ? null : new ArrayList<>(inputs);
  }

  public void setOutputs(List<URL> outputs) {
    this.outputs = (outputs == null) ? null : new ArrayList<>(outputs);
  }

  public void addOutput(URL output) {
    if (this.outputs == null) {
      this.outputs = new ArrayList<>();
    }
    this.outputs.add(output);
  }

  public void addInput(URL input) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<>();
    }
    this.inputs.add(input);
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public void setProv_report(URL prov_report) {
    this.prov_report = prov_report;
  }
}
