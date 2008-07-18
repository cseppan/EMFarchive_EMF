package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTabView;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabView;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabView;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditCaseParametersTabView;
import gov.epa.emissions.framework.services.EmfException;

public interface CaseEditorPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;
    
    void doSaveWithoutClose() throws EmfException;
    
    void set(EditableCaseSummaryTabView summaryView);

    void set(EditInputsTabView inputsView) throws EmfException;

    void set(EditJobsTabView jobsView) throws EmfException;

    void set(EditOutputsTabView inputsView) throws EmfException;

    void set(EditCaseParametersTabView parameterview);

    void set(ShowHistoryTabView caseHistoryView);

    void doLoad(String tabTitle) throws EmfException;
    
    void checkIfLockedByCurrentUser() throws EmfException;
    
    void addSector(Sector sector);
    
}