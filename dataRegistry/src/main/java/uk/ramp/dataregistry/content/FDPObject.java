package uk.ramp.dataregistry.content;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;


@XmlRootElement
public class FDPObject extends FDP_Updateable{
    @XmlElement
    private String description;

    @XmlElement
    private String storage_location;

    @XmlElement
    private String file_type;

    @XmlElement
    private List<String> issues;

    @XmlElement
    private List<String> components;

    @XmlElement
    private String data_product;

    @XmlElement
    private String code_repo_release;

    @XmlElement
    private String external_object;

    @XmlElement
    private String quality_control;

    @XmlElement
    private List<String> authors;

    @XmlElement
    private List<String> licences;

    @XmlElement
    private List<String> keywords;

    public FDPObject() {
    }

    public String getDescription() { return description; }
    public String getStorage_location() { return storage_location; }
    public String getFile_type() { return file_type; }
    public List<String> getIssues() { return new ArrayList<String>(issues); }
    public List<String> getComponents() { return new ArrayList<String>(components); }
    public String getData_product() { return data_product; }
    public String getCode_repo_release() { return code_repo_release; }
    public String getExternal_object() { return external_object; }
    public String getQuality_control() { return quality_control; }
    public List<String> getAuthors() { return new ArrayList<String>(authors); }
    public List<String> getLicences() { return new ArrayList<String>(licences); }
    public List<String> getKeywords() { return new ArrayList<String>(keywords); }

    public void setDescription(String description) {this.description = description; }
    public void setStorage_location(String storage_location) { this.storage_location = storage_location; }
    public void setFile_type(String file_type) { this.file_type = file_type; }
    public void setIssues(List<String> issues) { this.issues = new ArrayList<String>(issues); }
    public void setComponents(List<String> components) { this.components = new ArrayList<>(components); }
    public void setData_product(String data_product) { this.data_product = data_product; }
    public void setCode_repo_release(String code_repo_release) { this.code_repo_release = code_repo_release; }
    public void setExternal_object(String external_object) { this.external_object = external_object; }
    public void setQuality_control(String quality_control) { this.quality_control = quality_control; }
    public void setAuthors(List<String> authors) { this.authors = new ArrayList<>(authors); }
    public void setLicences(List<String> licences) { this.licences = new ArrayList<>(licences); }
    public void setKeywords(List<String> keywords) { this.keywords = new ArrayList<>(keywords); }

}
