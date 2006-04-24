package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.services.EmfException;

public interface CaseEditorPresenter {

    public abstract void doDisplay() throws EmfException;

    public abstract void doClose() throws EmfException;

    public abstract void doSave() throws EmfException;

    public abstract void set(EditableCaseSummaryTabView summaryView);

}