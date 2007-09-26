package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class PersistedWaitTask implements Serializable {

    private int id;
    private int jobId;
    private int caseId;
    private int userId;
    
    public int getId() {
        return id;
    }

    public void setId(int aId) {
        this.id = aId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int aJobId) {
        this.jobId = aJobId;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int aCaseId) {
        this.caseId = aCaseId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int aUserId) {
        this.userId = aUserId;
    }

    public PersistedWaitTask() {
        super();
    }

    public PersistedWaitTask(int aId, int aJobId, int aCaseId, int aUserId) {
        super();
        this.id = aId;
        this.jobId = aJobId;
        this.caseId = aCaseId;
        this.userId = aUserId;
    }

    
}
