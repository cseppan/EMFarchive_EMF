package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;

public interface EditParametersTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    void addNewParameterDialog(NewCaseParameterView view) throws EmfException;
    
    void addNewParameter(CaseParameter input) throws EmfException;

    void editParameter(CaseParameter input, EditCaseParameterView inputEditor) throws EmfException;

   // void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException;

    CaseParameter[] getCaseParameters(int caseId) throws EmfException;

    void removeParameters(CaseParameter[] params) throws EmfException;
}