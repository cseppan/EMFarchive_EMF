package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class DependentJob implements Serializable {

    private int jobId;
    
    public DependentJob(){
        super();
    }
    
    public int getJobId() {
        return jobId;
    }
    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
}
