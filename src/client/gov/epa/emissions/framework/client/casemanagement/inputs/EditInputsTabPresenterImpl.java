package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.services.casemanagement.InputEnvtVar;
import gov.epa.emissions.framework.services.casemanagement.InputName;
import gov.epa.emissions.framework.services.casemanagement.Program;

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
        view.display(caseObj, this);
    }

    public void doSave() {
        CaseInput[] inputs = view.caseInputs();
        caseObj.setCaseInputs(inputs);
    }

    public void doAddInput(NewInputView dialog) {
        dialog.register(this);
        dialog.display(caseObj);
        if (dialog.shouldCreate())
            view.addInput(dialog.input());
    }

    public void doEditInput(CaseInput input, EditInputView inputEditor) throws EmfException {
        EditInputPresenter editInputPresenter = new EditInputPresenterImpl(inputEditor, view, session);
        editInputPresenter.display(input);
    }

    public void doAddInputFields(JComponent container, InputFieldsPanelView inputFields) throws EmfException {
        CaseInput newInput = new CaseInput();
        newInput.setRequired(true);
        newInput.setShow(true);

        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(inputFields, session);
        inputFieldsPresenter.display(newInput, container);
    }

    public void doCheckDuplicate(CaseInput input, CaseInput[] existingInputs) throws EmfException {
//        for (int i = 0; i < existingInputs.length; i++) {
//            if (input.equals(existingInputs[i]))
//                throw new EmfException("Case input: " + existingInputs[i].getInputName().getName()
//                        + " has already existed.");
//        }

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
            if (envtVar != null && envtVar.getId() != envtVars[0].getId())
                if (envtVar.equals(envtVars[i]))
                    throw new EmfException("InputEnvtVar: " + envtVar.getName() + "has already existed.");

    }

    private CaseService caseService() {
        return this.session.caseService();
    }

}
