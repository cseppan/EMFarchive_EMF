package gov.epa.emissions.framework.services.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CaseJob implements Serializable, Comparable<CaseJob> {

    private int id;
    
    private String name;
    
    private String purpose;
    
    private float jobNo;
    
    private String idInQueue;
    
    private Sector sector;
    
    private Executable executable;
    
    private String args;
    
    private int order;
    
    private int version;
    
    private JobRunStatus runstatus;
    
    private int caseId;
    
    private Date runStartDate;

    private Date runCompletionDate;
    
    private User user;
    
    private String runNotes;
    
    private String runLog;
    
    private Host host;
    
    private String queOptions;
    
    private String path;
    
    private String jobkey;
    
    private List<DependentJob> dependentJobs;

    public CaseJob() {
        this("");
    }
    
    public CaseJob(String name) {
        this.name = name;
        this.dependentJobs = new ArrayList<DependentJob>();
    }
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getIdInQueue() {
        return idInQueue;
    }

    public void setIdInQueue(String idInQueue) {
        this.idInQueue = idInQueue;
    }

    public float getJobNo() {
        return jobNo;
    }

    public void setJobNo(float jobNo) {
        this.jobNo = jobNo;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return this.name;
    }

    public int compareTo(CaseJob other) {
        if (this.id == other.getId())
            return 0;
        
        String thisJob = this.name;
        String otherJob = other.getName();
        return thisJob.compareToIgnoreCase(otherJob);
    }
    
    public int hashCode() {
        return this.name.hashCode();
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getQueOptions() {
        return queOptions;
    }

    public void setQueOptions(String queOptions) {
        this.queOptions = queOptions;
    }

    public Date getRunCompletionDate() {
        return runCompletionDate;
    }

    public void setRunCompletionDate(Date runCompletionDate) {
        this.runCompletionDate = runCompletionDate;
    }

    public String getRunLog() {
        return runLog;
    }

    public void setRunLog(String runLog) {
        this.runLog = runLog;
    }

    public String getRunNotes() {
        return runNotes;
    }

    public void setRunNotes(String runNotes) {
        this.runNotes = runNotes;
    }

    public Date getRunStartDate() {
        return runStartDate;
    }

    public void setRunStartDate(Date runStartDate) {
        this.runStartDate = runStartDate;
    }

    public JobRunStatus getRunstatus() {
        return runstatus;
    }

    public void setRunstatus(JobRunStatus runstatus) {
        this.runstatus = runstatus;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
     
    public String getJobkey(){
        return this.jobkey;
    }

    public void setJobkey(String jobKey){
        this.jobkey = jobKey;
    }
    
    public DependentJob[] getDependentJobs() {
        return dependentJobs.toArray(new DependentJob[0]);
    }

    public void setDependentJobs(DependentJob[] dependentJobs) {
        this.dependentJobs = Arrays.asList(dependentJobs);
    }

}
