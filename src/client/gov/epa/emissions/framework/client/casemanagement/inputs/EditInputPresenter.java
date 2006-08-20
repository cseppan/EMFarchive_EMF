package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputPresenter {
    
    void display(CaseInput input) throws EmfException;
    
    void doSave() throws EmfException;
    
    void doCheckDuplicate(CaseInput input) throws EmfException;
}
