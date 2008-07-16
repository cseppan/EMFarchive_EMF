package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

import java.util.Date;

public class EditableCaseSummaryTabPresenterImpl implements EditableCaseSummaryTabPresenter {

    private EditableCaseSummaryTabView view;

    private Case caseObj;

    public EditableCaseSummaryTabPresenterImpl(Case caseObj, EditableCaseSummaryTabView view) {
        this.caseObj = caseObj;
        this.view = view;
    }

    public void doSave() throws EmfException {
        caseObj.setLastModifiedDate(new Date());
        view.save(caseObj);
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }
    
    public void checkIfLockedByCurrentUser() {
        //
    }

    public void resetSectors() {
        view.resetSectors();
        
    }

}
