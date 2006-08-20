package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;

public class EditOutputsTabPresenterImpl implements EditOutputsTabPresenter {

    private Case caseObj;

    private EditOutputsTabView view;
    
    public EditOutputsTabPresenterImpl(EmfSession session, EditOutputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
    }

    public void display() {
        view.display(caseObj, this);
    }

    public void doSave() {
        //
    }

}
