package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;

public class EditOutputsTabPresenterImpl implements EditOutputsTabPresenter {

    private Case caseObj;

    private EditOutputsTabView view;
    
    private EmfSession session;
    
    public EditOutputsTabPresenterImpl(EmfSession session, EditOutputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.observe(this);
        view.display();
    }

    public void doSave() throws EmfException {
        // NOTE Auto-generated method stub
        if (false)
            throw new EmfException("");
    }

    public CaseOutput[] getCaseOutputs(int caseId, int jobId) throws EmfException {
        return service().getCaseOutputs(caseId, jobId);
    }

    private CaseService service() {
        return session.caseService();
    }

    public CaseJob[] getCaseJobs() throws EmfException {
        return service().getCaseJobs(caseObj.getId());
    }

    public Case getCaseObj() {
        return this.caseObj;
    }
}
