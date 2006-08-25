package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.Program;
import gov.epa.emissions.framework.services.data.EmfDataset;

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
        view.saveCaseInputFileDir();
        CaseInput[] inputs = view.caseInputs();
        caseObj.setCaseInputs(inputs);
        view.refresh();
    }

    public void doAddInput(NewInputView dialog) {
        dialog.register(this);
        dialog.display(caseObj);
        if (dialog.shouldCreate())
            view.addInput(dialog.input());
        refreshView();
    }

    private void refreshView() {
        view.refresh();
        view.notifychanges();
    }

    public void doEditInput(CaseInput input, EditCaseInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditCaseInputPresenterImpl(inputEditor, view, session);
        editInputPresenter.display(input);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields) throws EmfException {
        CaseInput newInput = new CaseInput();
        newInput.setRecordID(view.numberOfRecord());
        newInput.setRequired(true);
        newInput.setShow(true);

        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(inputFields, session);
        inputFieldsPresenter.display(newInput, container);
    }

    public void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException {
        for (int i = 0; i < existingInputs.length; i++) {
            if (input.getRecordID() != existingInputs[i].getRecordID())
                if (input.equals(existingInputs[i]))
                    throw new EmfException("The combination of 'Input Name', 'Sector', and 'Program' " +
                            "should be unique.");
        }

        InputName[] names = caseService().getInputNames();
        InputName inputName = input.getInputName();
        for (int i = 0; i < names.length; i++) {
            if (inputName == null)
                throw new EmfException("InputName cannot be null.");

            if (inputName.getId() != names[i].getId())
                if (inputName.equals(names[i]))
                    throw new EmfException("InputName: " + inputName.getName() + "has already existed.");
        }

        Program[] prgs = caseService().getPrograms();
        Program prg = input.getProgram();
        for (int i = 0; i < prgs.length; i++)
            if (prg != null && prg.getId() != prgs[i].getId())
                if (prg.equals(prgs[i]))
                    throw new EmfException("Program: " + prg.getName() + "has already existed.");

        InputEnvtVar[] envtVars = caseService().getInputEnvtVars();
        InputEnvtVar envtVar = input.getEnvtVars();
        for (int i = 0; i < envtVars.length; i++)
            if (envtVar != null && envtVar.getId() != envtVars[i].getId())
                if (envtVar.equals(envtVars[i]))
                    throw new EmfException("InputEnvtVar: " + envtVar.getName() + "has already existed.");

    }

    private CaseService caseService() {
        return this.session.caseService();
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }

}
