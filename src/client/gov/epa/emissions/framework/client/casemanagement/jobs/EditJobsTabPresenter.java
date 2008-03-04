package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface EditJobsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    void addNewJobDialog(NewJobView view) throws EmfException;
    
    CaseJob addNewJob(CaseJob job) throws EmfException;

    void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException;
    
    void copyJob(CaseJob job, EditCaseJobView jobEditor) throws Exception;

    void removeJobs(CaseJob[] jobs) throws EmfException;
    
    CaseJob[] getCaseJobs() throws EmfException;
    
    void runJobs(CaseJob[] jobs) throws EmfException;
    
    boolean jobsUsed(CaseJob[] jobs) throws EmfException;
    
    String getJobsStatus(CaseJob[] jobs) throws EmfException;

    String validateJobs(CaseJob[] jobs) throws EmfException;
    
    void checkIfLockedByCurrentUser() throws EmfException;
}