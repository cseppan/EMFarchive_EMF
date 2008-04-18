package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorTabPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.List;

public interface EditInputsTabPresenter extends CaseEditorTabPresenter {

    void display() throws EmfException;
    
    void addNewInputDialog(NewInputView view, CaseInput input) throws EmfException;
    
    void addNewInput(CaseInput input) throws EmfException;

    void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException;

   // void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException;

    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException;
    
//    void doExportWithOverwrite(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose) throws EmfException;
//
//    void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose) throws EmfException;

    void exportCaseInputs(List<CaseInput> inputList, String purpose) throws EmfException;
    void exportCaseInputsWithOverwrite(List<CaseInput> inputList, String purpose) throws EmfException;
    
    void removeInputs(CaseInput[] inputs) throws EmfException;
    
    CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException;

    void copyInput(int caseId, CaseInput input, NewInputView dialog) throws Exception;
    
    public Object[] getAllCaseNameIDs() throws EmfException;
}