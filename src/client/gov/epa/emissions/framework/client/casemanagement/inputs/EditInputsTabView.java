package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputsTabView {

    void display(EmfSession session, Case caseObj, EditInputsTabPresenter presenter);

    CaseInput[] caseInputs();

    void addInput(CaseInput input);
    
    void refresh();
    
    String getCaseInputFileDir();

    void checkDuplicate(CaseInput input) throws EmfException;
    
    int numberOfRecord();

    void clearMessage();
    
    void notifychanges();

}
