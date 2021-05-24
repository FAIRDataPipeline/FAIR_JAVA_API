package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@XmlRootElement
public class Code_run extends FDP_Updateable{
    @XmlElement
    private LocalDateTime run_date;

    @XmlElement
    private String description;

    @XmlElement
    private String code_repo;

    @XmlElement
    private String model_config;

    @XmlElement
    private String submission_script;

    @XmlElement
    private List<String> inputs;

    @XmlElement
    private List<String> outputs;



    public LocalDateTime getRun_date() { return this.run_date; }
    public String getDescription() { return this.description; }
    public String getCode_repo() { return this.code_repo; }
    public String getModel_config() { return this.model_config; }
    public String getSubmission_script() { return this.submission_script; }
    public List<String> getInputs() { return this.inputs; }
    public List<String> getOutputs() { return this.outputs; }

    public void setRun_date(LocalDateTime run_date) { this.run_date = run_date;  }
    public void setDescription(String description) { this.description = description; }
    public void setCode_repo(String code_repo) { this.code_repo = code_repo; }
    public void setModel_config(String model_config) { this.model_config = model_config; }
    public void setSubmission_script(String submission_script) { this.submission_script = submission_script; }
    public void setInputs(List<String> inputs) {this.inputs = new ArrayList<String>(inputs); }
    public void setOutputs(List<String> outputs) { this.outputs = new ArrayList<String>(outputs); }


}