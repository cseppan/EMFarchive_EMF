package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseEditorTabPresenter {

    void doSave() throws EmfException;
    
    Case getCaseObj();
}
