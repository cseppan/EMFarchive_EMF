package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseObjectManager;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.jobs.JobRunStatus;

import java.util.ArrayList;
import java.util.List;

public class ViewableJobsTabPresenterImpl implements EditJobsTabPresenter{

    private Case caseObj;

    private ViewableJobsTab view;

    private EmfSession session;

    public ViewableJobsTabPresenterImpl(EmfSession session, ViewableJobsTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave(CaseJob[] jobs) throws EmfException {
        for (CaseJob job : jobs)
            service().updateCaseJobStatus(job);
    }


    public CaseJob addNewJob(CaseJob job) {
        return null;
    }

    private CaseService service() {
        return session.caseService();
    }

    public boolean jobsUsed(CaseJob[] jobs) {
        return false;
    }

    public void removeJobs(CaseJob[] jobs) {
        //service().removeCaseJobs(jobs);
    }

    public void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException {
        EditJobPresenter presenter = new EditCaseJobPresenterImpl(jobEditor,this, session);
        presenter.display(job);
    }

    public void copyJob2CurrentCase(int caseId, CaseJob job, EditCaseJobView jobEditor) throws Exception {
 //
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

    public void runJobs(CaseJob[] jobs) throws EmfException {
        Integer[] jobIds = new Integer[jobs.length];

        for (int i = 0; i < jobs.length; i++) {
            if (jobs[i].getExecutable() == null)
                throw new EmfException("Job " + jobs[i].getName() + " doesn't have a valid executable file.");
            jobIds[i] = new Integer(jobs[i].getId());
        }

        System.out.println("Start running jobs");
        service().runJobs(jobIds, caseObj.getId(), session.user());
        System.out.println("Finished running jobs");
    }

    public String getJobsStatus(CaseJob[] jobs) throws EmfException {
        List<String> ok = new ArrayList<String>();
        List<String> cancel = new ArrayList<String>();
        List<String> warning = new ArrayList<String>();
        
        for (int i = 0; i < jobs.length; i++) {
            String status = service().getCaseJob(jobs[i].getId()).getRunstatus().getName();

            if (status == null || status.trim().isEmpty())
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Not Started"))
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Quality Assured"))
                ok.add(status);

            if (status != null && status.equalsIgnoreCase("Completed"))
                warning.add(status);

            if (status != null && status.equalsIgnoreCase("Failed"))
                warning.add(status);

            if (status != null && status.equalsIgnoreCase("Running"))
                cancel.add(status);

            if (status != null && status.equalsIgnoreCase("Submitted"))
                cancel.add(status);

            if (status != null && status.equalsIgnoreCase("Exporting"))
                cancel.add(status);

            if (status != null && status.equalsIgnoreCase("Waiting"))
                cancel.add(status);
        }
        
        if (ok.size() == jobs.length)
            return "OK";

        if (cancel.size() > 0)
            return "CANCEL";

        return "WARNING";
    }

    public String validateJobs(CaseJob[] jobs) throws EmfException {
        List<Integer> ids = new ArrayList<Integer>();
        
        for (CaseJob job : jobs)
            ids.add(new Integer(job.getId()));
//      System.out.println("Validating input datasets of jobs");
        String msg = service().validateJobs(ids.toArray(new Integer[0]));
//        System.out.println("Finished validating case jobs.");
        return msg;
    }

    public void addNewJobDialog(NewJobView view) {
        // NOTE Auto-generated method stub
        
    }

    public synchronized JobRunStatus[] getRunStatuses() throws EmfException {
        return CaseObjectManager.getCaseObjectManager(session).getJobRunStatuses();
    }

    public void doSave() {
        // NOTE Auto-generated method stub
        
    }

    public void checkIfLockedByCurrentUser() {
        // NOTE Auto-generated method stub
        
    }

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }

    public void copyJobs(int caseId, List<CaseJob> jobs) throws Exception {
        if (caseId == this.caseObj.getId()) {
            for (CaseJob job : jobs)
                copyJob2CurrentCase(caseId, job, null);
        } else {
            CaseJob[] jobsArray = jobs.toArray(new CaseJob[0]);
            User user = session.user();

            for (int i = 0; i < jobs.size(); i++) {
                jobsArray[i].setParentCaseId(this.caseObj.getId());
                jobsArray[i].setRunJobUser(null); // not running at this moment
                jobsArray[i].setUser(user); // job owner changes
            }

            service().addCaseJobs(user, caseId, jobsArray);
        }
    }

    public void addNewSectorToSummary(CaseJob job) {
        // NOTE Auto-generated method stub
        
    }

    public void display(CaseEditorPresenter parentPresenter) {
        // NOTE Auto-generated method stub
        
    }

    public void refreshJobList() {
        // NOTE Auto-generated method stub
        
    }

    
}
