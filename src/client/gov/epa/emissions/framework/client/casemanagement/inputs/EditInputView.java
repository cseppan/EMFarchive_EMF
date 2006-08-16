package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputView {
    void display(CaseInput input) throws EmfException;
    
    void observe(EditInputPresenterImpl presenter);

    void loadInput() throws EmfException;
    
    void populateFields();
}
