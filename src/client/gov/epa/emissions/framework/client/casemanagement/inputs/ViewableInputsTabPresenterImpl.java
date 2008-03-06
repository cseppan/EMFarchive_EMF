package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.List;

import javax.swing.JComponent;

public class ViewableInputsTabPresenterImpl {

    private Case caseObj;

    private ViewableInputsTab view;

    private EmfSession session;

    public ViewableInputsTabPresenterImpl(EmfSession session, ViewableInputsTab view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void addNewInputDialog(NewInputView dialog, CaseInput newInput) {
        dialog.register(this);
        dialog.display(caseObj.getId(), newInput);
    }

    private CaseService service() {
        return session.caseService();
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(caseObj.getId(), inputEditor,
                session);
        editInputPresenter.display(input);
    }
    
    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields, CaseInput newInput) throws EmfException {
        newInput.setId(view.numberOfRecord());
        
        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj.getId(), inputFields,
                session);
        inputFieldsPresenter.display(newInput, container);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public CaseInput[] getCaseInput(int caseId) throws EmfException {
        return service().getCaseInputs(caseId);
    }

    private void doExport(List<CaseInput> caseInputs, boolean overwrite, String purpose) throws EmfException {
        CaseService services = session.caseService();
        Integer[] caseInputIds = new Integer[caseInputs.size()];

        for (int i = 0; i < caseInputIds.length; i++) {
            caseInputIds[i] = new Integer(caseInputs.get(i).getId());
        }

        if (overwrite)
            services.exportCaseInputsWithOverwrite(session.user(), caseInputIds, purpose);

        else
            services.exportCaseInputs(session.user(), caseInputIds, purpose);
    }

    public void exportCaseInputs(List<CaseInput> inputList, String purpose) throws EmfException {
        doExport(inputList, false, purpose);
    }

    public void exportCaseInputsWithOverwrite(List<CaseInput> inputList, String purpose) throws EmfException {
        doExport(inputList, true, purpose);
    }
    
    public Case getCaseObj() {
        return this.caseObj;
    }
    
}
