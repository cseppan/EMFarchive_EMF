package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public class EditInputsTabPresenterImpl implements EditInputsTabPresenter {

    private Case caseObj;

    private EditInputsTabView view;

    public EditInputsTabPresenterImpl(EmfSession session, EditInputsTabView view, Case caseObj) {
        this.caseObj = caseObj;
        this.view = view;
    }

    public void display() {
        view.display(caseObj, this);
    }

    public void doSave() {
        CaseInput[] additions = view.additions();
        caseObj.setCaseInputs(additions);
    }

    public void doAddInput(NewInputView dialog) {
        dialog.display(caseObj);
        if (dialog.shouldCreate())
            view.addInput(dialog.input());
    }

    public void doViewInput(CaseInput input, InputView window) {
        window.display(input);
    }
}
