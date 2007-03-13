package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;

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
        view.display(caseObj, this, session);
    }

    public void doSave() {
        view.saveCaseOutputFileDir();
    }

}
