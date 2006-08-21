package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;

    void doAddInput(NewInputView view) throws EmfException;

    void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException;

    void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException;

}