package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.Arrays;
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

    public void addNewInputDialog(NewInputView dialog, CaseInput newInput) {
        dialog.register(this);
        dialog.display(newInput.getCaseID(), newInput);
    }

    public void addNewInput(CaseInput input) throws EmfException {
        CaseInput loaded = service().addCaseInput(session.user(), input);

        if (input.getCaseID() == caseObj.getId()) {
            view.addInput(loaded);
            refreshView();
        }
    }

    private CaseService service() {
        return session.caseService();
    }

    private void refreshView() {
        view.refresh();
    }

    public void removeInputs(CaseInput[] inputs) throws EmfException {
        service().removeCaseInputs(inputs);
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(caseObj.getId(), inputEditor, view,
                session);
        editInputPresenter.display(input);
    }

    public void copyInput(CaseInput input, NewInputView dialog) throws Exception {
        CaseInput newInput = (CaseInput) DeepCopy.copy(input);
        addNewInputDialog(dialog, newInput);
    }

    public void copyInput(int caseId, List<CaseInput> inputs) throws Exception {
        service().addCaseInputs(session.user(), caseId, inputs.toArray(new CaseInput[0]));
    }

    public void copyInput(int caseId, CaseInput input) throws Exception {
        CaseInput newInput = (CaseInput) DeepCopy.copy(input);
        newInput.setCaseID(caseId);
        addNewInput(newInput);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields, CaseInput newInput)
            throws EmfException {
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

    public CaseInput[] getCaseInput(int caseId, Sector sector, boolean showAll) throws EmfException {
        if (sector == null)
            return new CaseInput[0];

        if (sector.compareTo(new Sector("All", "All")) == 0)
            sector = null; // to trigger select all on the server side

        return service().getCaseInputs(caseId, sector, showAll);
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

    public void checkIfLockedByCurrentUser() throws EmfException {
        Case reloaded = session.caseService().reloadCase(caseObj.getId());

        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current case object expired. User " + reloaded.getLockOwner()
                    + " has it now.");
    }

    public Sector[] getAllSetcors() throws EmfException {
        List<Sector> all = new ArrayList<Sector>();
        all.add(new Sector("All", "All"));
        all.addAll(Arrays.asList(session.dataCommonsService().getSectors()));

        return all.toArray(new Sector[0]);
    }

    public Object[] getAllCaseNameIDs() throws EmfException {
        return service().getAllCaseNameIDs();
    }

}
