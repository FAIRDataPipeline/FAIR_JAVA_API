package org.fairdatapipeline.dataregistry.content;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.fairdatapipeline.dataregistry.restclient.APIURL;

/** A code run along with its associated, code repo, configuration, input and outputs. */
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

  @XmlElement private APIURL ro_crate;


  /** Empty Constructor. */
  public RegistryCode_run() {
    methods_allowed = List.of("GET", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");
  }

  /**
   * Datetime of the CodeRun.
   *
   * @return Datetime of the CodeRun.
   */
  public LocalDateTime getRun_date() {
    return this.run_date;
  }

  /**
   * (optional): Free text description of the CodeRun.
   *
   * @return Free text description of the CodeRun.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * (optional): Reference to the RegistryObject associated with the StorageLocation where the code
   * repository is stored.
   *
   * @return Reference to the RegistryObject associated with the StorageLocation where the code
   *     repository is stored.
   */
  public APIURL getCode_repo() {
    return this.code_repo;
  }

  /**
   * Reference to the RegistryObject for the YAML configuration used for the CodeRun.
   *
   * @return Reference to the RegistryObject for the YAML configuration used for the CodeRun.
   */
  public APIURL getModel_config() {
    return this.model_config;
  }

  /**
   * Reference to the RegistryObject for the submission script used for the CodeRun.
   *
   * @return Reference to the RegistryObject for the submission script used for the CodeRun.
   */
  public APIURL getSubmission_script() {
    return this.submission_script;
  }

  /**
   * List of RegistryObject_component that the CodeRun used as inputs.
   *
   * @return List of RegistryObject_component that the CodeRun used as inputs.
   */
  public List<APIURL> getInputs() {
    // used to return empty arraylist when inputs==null, but we are more likely to be able to use
    // the PATCH method if unset items are null.
    return (this.inputs == null) ? null : new ArrayList<>(this.inputs);
  }

  /**
   * List of RegistryObject_component that the CodeRun produced as outputs.
   *
   * @return List of RegistryObject_component that the CodeRun produced as outputs.
   */
  public List<APIURL> getOutputs() {
    // used to return empty arraylist when outputs==null, but we are more likely to be able to use
    // the PATCH method if unset items are null.
    return (this.outputs == null) ? null : new ArrayList<>(this.outputs);
  }

  /**
   * (optional): UUID of the CodeRun. If not specified a UUID is generated automatically.
   *
   * @return UUID of the CodeRun.
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * (read only): Reference to the PROV report for this Coderun.
   *
   * @return Reference to the PROV report for this Coderun.
   */
  public APIURL getProv_report() {
    return this.prov_report;
  }

  /**
   * APIURL reference to the RO CRATE for this Data product
   *
   * @return APIURL reference to the RO Crate for this Data product
   */
  public APIURL getRo_crate() {
    return this.ro_crate;
  }

  /** @param run_date Datetime of the CodeRun. */
  public void setRun_date(LocalDateTime run_date) {
    this.run_date = run_date;
  }

  /** @param description (optional): Free text description of the CodeRun. */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @param code_repo (optional): Reference to the RegistryObject associated with the
   *     StorageLocation where the code repository is stored.
   */
  public void setCode_repo(APIURL code_repo) {
    this.code_repo = code_repo;
  }

  /**
   * @param model_config Reference to the RegistryObject for the YAML configuration used for the
   *     CodeRun.
   */
  public void setModel_config(APIURL model_config) {
    this.model_config = model_config;
  }

  /**
   * @param submission_script Reference to the RegistryObject for the submission script used for the
   *     CodeRun.
   */
  public void setSubmission_script(APIURL submission_script) {
    this.submission_script = submission_script;
  }

  /** @param inputs List of RegistryObject_component that the CodeRun used as inputs. */
  public void setInputs(List<APIURL> inputs) {
    this.inputs = (inputs == null) ? null : new ArrayList<>(inputs);
  }

  /** @param outputs List of RegistryObject_component that the CodeRun produced as outputs. */
  public void setOutputs(List<APIURL> outputs) {
    this.outputs = (outputs == null) ? null : new ArrayList<>(outputs);
  }

  /**
   * Add an output
   *
   * @param output Reference to a RegistryObject_component to add as an output of this Code_run
   */
  public void addOutput(APIURL output) {
    if (this.outputs == null) {
      this.outputs = new ArrayList<>();
    }
    this.outputs.add(output);
  }

  /**
   * Add an input
   *
   * @param input Reference to a RegistryObject_component to add as an input of this Code_run
   */
  public void addInput(APIURL input) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<>();
    }
    this.inputs.add(input);
  }

  /**
   * @param uuid (optional): UUID of the CodeRun. If not specified a UUID is generated
   *     automatically.
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
