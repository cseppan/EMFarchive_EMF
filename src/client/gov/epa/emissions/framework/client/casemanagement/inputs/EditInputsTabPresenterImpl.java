package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.util.Date;

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
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(inputEditor, view, session);
        editInputPresenter.display(input);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields) throws EmfException {
        CaseInput newInput = new CaseInput();
        newInput.setId(view.numberOfRecord());
        newInput.setRequired(true);
        newInput.setShow(true);

        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(inputFields, session);
        inputFieldsPresenter.display(newInput, container);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

    public void doExportWithOverwrite(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose)
            throws EmfException {
        doExport(datasets, versions, folders, true, purpose);
    }

    public void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, String purpose)
            throws EmfException {
        doExport(datasets, versions, folders, false, purpose);
    }

    private void doExport(EmfDataset[] datasets, Version[] versions, String[] folders, boolean overwrite, String purpose)
            throws EmfException {
        ExImService services = session.eximService();

        for (int i = 0; i < datasets.length; i++) {
            datasets[i].setAccessedDateTime(new Date());

            if (overwrite)
                services.exportDatasetsWithOverwrite(session.user(), new EmfDataset[] { datasets[i] },
                        new Version[] { versions[i] }, mapToRemote(folders[i]), purpose);
            else
                services.exportDatasets(session.user(), new EmfDataset[] { datasets[i] },
                        new Version[] { versions[i] }, mapToRemote(folders[i]), purpose);
        }
    }

    private String mapToRemote(String dir) {
        return session.preferences().mapLocalOutputPathToRemote(dir);
    }

    public CaseInput[] getCaseInput(int caseId) throws EmfException {
        return service().getCaseInputs(caseId);
    }

}
