package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

public interface EditJobsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    void addNewJobDialog(NewJobView view) throws EmfException;
    
    void addNewJob(CaseJob job) throws EmfException;

    void doEditJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException;

    void removeJobs(CaseJob[] jobs) throws EmfException;
    
    CaseJob[] getCaseJobs() throws EmfException;
    
    Case getCaseObj();
}