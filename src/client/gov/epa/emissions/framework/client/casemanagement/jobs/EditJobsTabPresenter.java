package gov.epa.emissions.framework.client.casemanagement.jobs;

import java.util.List;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditJobsTabPresenter extends CaseEditorTabPresenter {

    void display(CaseEditorPresenter parentPresenter) throws EmfException;
    
    void addNewJobDialog(NewJobView view) throws EmfException;
    
    CaseJob addNewJob(CaseJob job) throws EmfException;
    
//    void addNewSectorToSummary(CaseJob job);
//    
//    void addNewRegionToSummary(CaseJob job);
    
    void refreshJobList() throws EmfException;

    void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException;
    
    List<CaseJob> copyJobs2CurrentCase(int caseId, List<CaseJob> jobs) throws Exception;

    void copyJobs(int caseId, List<CaseJob> jobs) throws Exception;
    
    CaseJob setJobValuesB4Copy(int caseId, CaseJob job) throws Exception;
    
    void removeJobs(CaseJob[] jobs) throws EmfException;
    
    CaseJob[] getCaseJobs() throws EmfException;
    
    void runJobs(CaseJob[] jobs) throws EmfException;
    
    boolean jobsUsed(CaseJob[] jobs) throws EmfException;
    
    String getJobsStatus(CaseJob[] jobs) throws EmfException;

    String validateJobs(CaseJob[] jobs) throws EmfException;
    
    void checkIfLockedByCurrentUser() throws EmfException;
    
    Object[] getAllCaseNameIDs() throws EmfException;
    
    JobRunStatus[] getRunStatuses() throws EmfException;
    
    void doSave(CaseJob[] jobs) throws EmfException;

    String cancelJobs(List<CaseJob> jobs) throws EmfException;

    void modifyJobs(ModifyJobsDialog dialog);
    
    GeoRegion[] getGeoregion(List<CaseJob> jobs);

}