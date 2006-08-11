package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.services.EmfException;

public interface CaseEditorPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(EditableCaseSummaryTabView summaryView);

    void set(EditInputsTabView inputsView) throws EmfException;

}