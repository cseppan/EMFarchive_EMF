package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputsTabView {

    void display(Case caseObj, EditInputsTabPresenter presenter);

    CaseInput[] caseInputs();

    void addInput(CaseInput input);
    
    void refresh();

    void checkDuplicate(CaseInput input) throws EmfException;

}
