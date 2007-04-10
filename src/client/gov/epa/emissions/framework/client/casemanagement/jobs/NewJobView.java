package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface NewJobView {
    void display(int caseId);

    boolean shouldCreate();

    CaseJob job();
    
    void register(Object presenter);
}
