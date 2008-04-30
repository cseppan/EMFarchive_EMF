package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.inputs.InputFieldsPanelPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.SetInputFieldsPanel;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

import javax.swing.JPanel;

public class SetCasePresenterImpl implements SetCasePresenter {
    
    private SetCaseView view;
    private CaseManagerPresenter managerPresenter;
    private InputFieldsPanelPresenter inputFieldsPresenter;
    private EmfSession session;
    private int defaultPageSize = 20;
    
    private Case caseObj; 
    
    public SetCasePresenterImpl(Case caseObj, SetCaseView view, 
           EmfSession session, CaseManagerPresenter managerPresenter) {
        this.view = view;
        this.session = session;
        this.caseObj = caseObj;

        this.managerPresenter = managerPresenter;    }

    public void display() throws EmfException {
        view.observe(this, managerPresenter);
        //Case b4locked = service().reloadCase(caseObj.getId());
        caseObj = service().obtainLocked(session.user(), caseObj);

        if (!caseObj.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(caseObj);
            return;
        }
        view.display(caseObj); //display(caseObj, jobSummaryMsg);

//        if (b4locked.isLocked() && !b4locked.isLocked(session.user()))
//            view.showLockingMsg("Lock acquired from an expired one (by user " + b4locked.getLockOwner() + ").");
   
    }

    public void doAddInputFields(CaseInput input, JPanel container, SetInputFieldsPanel setInputFieldsPanel) throws EmfException {
        inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj.getId(), setInputFieldsPanel, session);
        inputFieldsPresenter.display(input, container);
    }

    public CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException {
        return service().getCaseInputs(defaultPageSize, caseId, sector, showAll);
    }

    public void checkIfLockedByCurrentUser() throws EmfException {
        throw new EmfException("under construction");
    }

    public void doSaveParam(CaseParameter param) throws EmfException {
        session.caseService().updateCaseParameter(session.user(), param);
    }
    
    public void doSaveInput(CaseInput input) throws EmfException {
        session.caseService().updateCaseInput(session.user(), input);
    }

    public Case getCaseObj() {
        return caseObj;
    }

    public CaseParameter[] getCaseParameters(int caseId, Sector sector, boolean showAll) throws EmfException {
        return service().getCaseParameters(defaultPageSize, caseId, sector, showAll);
    }
    
    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), caseObj);
        closeView();
    }
    
    private void closeView() {
        view.disposeView();
    }
    
    private CaseService service() {
        return session.caseService();
    }

    public EmfSession getSession() {
        return this.session;
    }

    public void doSave() {
        // NOTE Auto-generated method stub
        
    }
    
    public String getJobName(int jobId) throws EmfException {
        if (jobId == 0)
            return "All jobs for sector";
        
        CaseJob job = session.caseService().getCaseJob(jobId);
        if (job == null)
            throw new EmfException("Cannot retrieve job (id = " + jobId + ").");
        return job.getName();
    }

}