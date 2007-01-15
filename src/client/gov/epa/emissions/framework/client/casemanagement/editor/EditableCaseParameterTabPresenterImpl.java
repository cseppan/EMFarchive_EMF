package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

import java.util.Date;

public class EditableCaseParameterTabPresenterImpl implements EditableCaseParameterTabPresenter {

    private EditableCaseParameterTabView view;

    private Case caseObj;

    public EditableCaseParameterTabPresenterImpl(Case caseObj, EditableCaseParameterTabView view) {
        this.caseObj = caseObj;
        this.view = view;
    }
    
    public void display() {
        view.display();
    }

    public void doSave() throws EmfException {
        caseObj.setLastModifiedDate(new Date());
        view.save(caseObj);
    }

}