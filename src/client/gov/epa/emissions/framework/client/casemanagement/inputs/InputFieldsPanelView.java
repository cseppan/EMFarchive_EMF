package gov.epa.emissions.framework.client.casemanagement.inputs;

import javax.swing.JComponent;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface InputFieldsPanelView {

    void observe(InputFieldsPanelPresenter presenter);

    void display(CaseInput input, JComponent container) throws EmfException;

    void populateFields();
    
    void setFields() throws EmfException;
    
    CaseInput getInput();
}
