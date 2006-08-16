package gov.epa.emissions.framework.client.casemanagement.inputs;

import javax.swing.JComponent;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

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
        EditInputPresenter editInputPresenter = new EditInputPresenterImpl(inputEditor,
                view, session);
        editInputPresenter.display(input);
    }
    
    public void doAddInputFields(JComponent container, 
            InputFieldsPanelView inputFields) throws EmfException {
        CaseInput newInput = new CaseInput();
        newInput.setRequired(true);
        newInput.setShow(true);
        
        InputFieldsPanelPresenter inputFieldsPresenter = new InputFieldsPanelPresenter(inputFields, session);
        inputFieldsPresenter.display(newInput, container);
    }
    
}
