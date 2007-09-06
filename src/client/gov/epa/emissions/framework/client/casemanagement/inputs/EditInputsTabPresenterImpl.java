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

public class EditInputsTabPresenterImpl implements EditInputsTabPresenter {

    private Case caseObj;

    private EditInputsTabView view;

    private EmfSession session;

    public EditInputsTabPresenterImpl(EmfSession session, EditInputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
        this.session = session;
    }

    public void display() {
        view.display(session, caseObj, this);
    }

    public void doSave() {
        String caseInputDir = view.getCaseInputFileDir();
        if (caseInputDir != null)
            caseObj.setInputFileDir(caseInputDir);
        view.refresh();
    }

    public void addNewInputDialog(NewInputView dialog) {
        dialog.register(this);
        dialog.display(caseObj.getId());
    }

    public void addNewInput(CaseInput input) throws EmfException {
        input.setCaseID(caseObj.getId());
        view.addInput(service().addCaseInput(input));
        refreshView();
    }

    private CaseService service() {
        return session.caseService();
    }

    private void refreshView() {
        view.refresh();
        // view.notifychanges();
    }

    public void removeInputs(CaseInput[] inputs) throws EmfException {
        service().removeCaseInputs(inputs);
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(caseObj.getId(), inputEditor, view,
                session);
        editInputPresenter.display(input);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields) throws EmfException {
        CaseInput newInput = new CaseInput();
        newInput.setId(view.numberOfRecord());
        newInput.setRequired(true);
        newInput.setShow(true);

        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(caseObj.getId(), inputFields,
                session);
        inputFieldsPresenter.display(newInput, container);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
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

}
