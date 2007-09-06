package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface EditJobsTabView {

    void display(EmfSession session, Case caseObj, EditJobsTabPresenter presenter);

    CaseJob[] caseJobs();

    void addJob(CaseJob job);
    
    void refresh();
    
    int numberOfRecord();

    void clearMessage();
    
    void saveCaseOutputFileDir();
    
//    void notifychanges();

}
