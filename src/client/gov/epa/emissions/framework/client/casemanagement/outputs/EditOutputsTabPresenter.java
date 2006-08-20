package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface EditOutputsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;

}