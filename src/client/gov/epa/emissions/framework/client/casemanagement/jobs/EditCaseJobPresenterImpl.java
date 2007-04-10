package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;

import javax.swing.JComponent;

public class EditCaseJobPresenterImpl implements EditJobPresenter {

    private EditCaseJobView view;
    
    private EditJobsTabView parentView;
    
    private EmfSession session;
    
    private JobFieldsPanelPresenter jobFieldsPresenter;
    
    private CaseJob job;

    public EditCaseJobPresenterImpl(EditCaseJobView view, 
            EditJobsTabView parentView, EmfSession session) {
        this.view = view;
        this.parentView = parentView;
        this.session = session;
    }
    
    public void display(CaseJob job) throws EmfException {
        this.job = job;
        view.observe(this);
        view.display(job);
        view.populateFields();
    }
    
    public void doAddJobFields(JComponent container, 
            JobFieldsPanelView inputFields) throws EmfException {
        jobFieldsPresenter = new JobFieldsPanelPresenter(inputFields, session);
        jobFieldsPresenter.display(job, container);
    }
    
    public void doSave() throws EmfException {
        jobFieldsPresenter.doSave();
        parentView.refresh();
    }

}
