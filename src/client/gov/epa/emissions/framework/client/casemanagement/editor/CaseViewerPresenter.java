package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.casemanagement.history.ViewableHistoryTab;
import gov.epa.emissions.framework.client.casemanagement.inputs.ViewableInputsTab;
import gov.epa.emissions.framework.client.casemanagement.jobs.ViewableJobsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.ViewableOutputsTab;
import gov.epa.emissions.framework.client.casemanagement.parameters.ViewableParametersTab;
import gov.epa.emissions.framework.services.EmfException;

public interface CaseViewerPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void set(ViewableCaseSummaryTab summaryView);

    void set(ViewableInputsTab inputsView) throws EmfException;

    void set(ViewableJobsTab jobsView) throws EmfException;

    void set(ViewableOutputsTab inputsView) throws EmfException;

    void set(ViewableParametersTab parameterview);

    void set(ViewableHistoryTab caseHistoryView);

    void doLoad(String tabTitle) throws EmfException;

}