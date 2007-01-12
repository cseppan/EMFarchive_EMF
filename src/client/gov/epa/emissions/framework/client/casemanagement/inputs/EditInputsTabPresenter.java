package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface EditInputsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;

    void doAddInput(NewInputView view) throws EmfException;

    void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException;

    void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException;

    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset);
    
    void doExportWithOverwrite(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose) throws EmfException;

    void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose) throws EmfException;

    void removeInputs(CaseInput[] inputs) throws EmfException;
    
    CaseInput[] getCaseInput(int caseId) throws EmfException;
}