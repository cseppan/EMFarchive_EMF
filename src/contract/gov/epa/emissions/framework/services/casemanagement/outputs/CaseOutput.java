package gov.epa.emissions.framework.services.casemanagement.outputs;

import java.io.Serializable;

public class CaseOutput implements Serializable, Comparable<CaseOutput> {

    private int id;
    
    private int caseId;
    
    private int jobId;
    
    private int datasetId;
    
    private String name;
    
    private String datasetFile;
    
    private String path;
    
    private String pattern;
    
    private String datasetType;
    
    private String datasetName;
    
    private String status;
    
    private String message;
    
    private String execName;
    
    private String remoteUser;
    
    private boolean empty;
    
    public CaseOutput() {
        //
    }

    public CaseOutput(String name) {
        this.name = name;
    }

    public int compareTo(CaseOutput other) {
        return this.name.compareToIgnoreCase(other.getName());
    }

    public String getDatasetFile() {
        return datasetFile;
    }

    public void setDatasetFile(String datasetFile) {
        this.datasetFile = datasetFile;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
    public String getExecName() {
        return execName;
    }

    public void setExecName(String execName) {
        this.execName = execName;
    }
    
    public String getRemoteUser() {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }
    
    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

}
