package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseEditorPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;
    
    void doSaveWithoutClose() throws EmfException;
    
    void doExport(User user, String dirName, String purpose, boolean overWrite, Case caseToExport) throws EmfException;

    void set(EditableCaseSummaryTabView summaryView);

    void set(EditInputsTabView inputsView) throws EmfException;

    void set(EditOutputsTabView inputsView) throws EmfException;

    void set(EditableCaseParameterTab parameterview);

}