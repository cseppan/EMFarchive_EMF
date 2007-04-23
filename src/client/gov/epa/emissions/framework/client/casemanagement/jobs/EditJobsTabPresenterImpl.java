package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

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

    public void removeJobs(CaseJob[] jobs) throws EmfException {
        service().removeCaseJobs(jobs);
    }

    public void doEditJob(CaseJob job, EditCaseJobView jobEditor) throws EmfException {
        throw new EmfException("Under construction...");
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public void addJobFields(CaseJob job, JComponent container, JobFieldsPanel jobFieldsPanel) throws EmfException {
        JobFieldsPanelPresenter jobFieldsPresenter = new JobFieldsPanelPresenter(jobFieldsPanel, session, this);
        jobFieldsPresenter.display(job, container);
    }

    public Case getCaseObj() {
        return this.caseObj;
    }


}
