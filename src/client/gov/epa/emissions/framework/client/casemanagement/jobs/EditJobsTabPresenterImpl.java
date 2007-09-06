package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JComponent;

public class EditJobsTabPresenterImpl implements EditJobsTabPresenter {

    private Case caseObj;

    private EditJobsTabView view;

    private EmfSession session;

    public EditJobsTabPresenterImpl(EmfSession session, EditJobsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave() {
        view.saveCaseOutputFileDir();
        view.refresh();
    }
    
    public void addNewJobDialog(NewJobView dialog) {
        dialog.register(this);
        dialog.display();
    }

    public void addNewJob(CaseJob job) throws EmfException {
        job.setCaseId(caseObj.getId());
        view.addJob(service().addCaseJob(job));
        refreshView();
    }

    private CaseService service() {
        return session.caseService();
    }

    private void refreshView() {
        view.refresh();
        // view.notifychanges();
    }

    public boolean jobsUsed(CaseJob[] jobs) throws EmfException {
        if (jobs.length == 0)
            return false;
        
        int caseId = jobs[0].getCaseId();
        CaseInput[] inputs = service().getCaseInputs(caseId);
        CaseParameter[] params = service().getCaseParameters(caseId);
        
        for (int i = 0; i < jobs.length; i++) {
            for (int j = 0; j < inputs.length; j++)
                if (inputs[j].getCaseJobID() == jobs[i].getId())
                    return true;
            
            for (int k = 0; k < params.length; k++)
                if (params[k].getJobId() == jobs[i].getId())
                    return true;
        }
        
        return false;
    }
    
    public void removeJobs(CaseJob[] jobs) throws EmfException {
        service().removeCaseJobs(jobs);
    }

    public void editJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException {
        EditJobPresenter presenter = new EditCaseJobPresenterImpl(jobEditor, view, this, session);
        presenter.display(job);
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public void addJobFields(CaseJob job, JComponent container, JobFieldsPanel jobFieldsPanel) throws EmfException {
        JobFieldsPanelPresenter jobFieldsPresenter = new JobFieldsPanelPresenter(jobFieldsPanel, session, this, caseObj);
        jobFieldsPresenter.display(job, container);
    }

    public Case getCaseObj() {
        return this.caseObj;
    }

    public void runJobs(CaseJob[] jobs) throws EmfException {
        Integer[] jobIds = new Integer[jobs.length];
        
        for (int i = 0; i < jobs.length; i++)
            jobIds[i] = new Integer(jobs[i].getId());
            
        service().runJobs(jobIds, caseObj.getId(), session.user());
    }

}
