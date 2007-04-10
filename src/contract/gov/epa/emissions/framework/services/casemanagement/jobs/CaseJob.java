package gov.epa.emissions.framework.services.casemanagement.jobs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;
import java.util.Date;

public class CaseJob implements Serializable, Comparable<CaseJob> {

    private int id;
    
    private String name;
    
    private String description;
    
    private Sector sector;
    
    private Executable executable;
    
    private String args;
    
    private int order;
    
    private int version;
    
    private CaseRunStatus runstatus;
    
    private int caseId;
    
    private Date runStartDate;

    private Date runCompletionDate;
    
    private User user;
    
    private String runNotes;
    
    private String runLog;
    
    private Host host;
    
    private String queOptions;
    
    private String path;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        
        String thisJob = this.name + this.executable.getName() + this.args;
        String otherJob = other.getName() + other.getExecutable().getName() + other.getArgs();
        return thisJob.compareTo(otherJob);
    }
    
    public int hashCode() {
        String unique = this.name + this.executable.getName() + this.args;
        return unique.hashCode();
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public Executable getExecutable() {
        return executable;
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

    public CaseRunStatus getRunstatus() {
        return runstatus;
    }

    public void setRunstatus(CaseRunStatus runstatus) {
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
  
}
