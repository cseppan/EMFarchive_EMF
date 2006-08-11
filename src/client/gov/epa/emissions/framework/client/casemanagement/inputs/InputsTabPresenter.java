package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public class InputsTabPresenter {

    private Case caseObj;

    public InputsTabPresenter(Case caseObj) {
        this.caseObj = caseObj;
    }

    public void display(InputsTabView view) {
        view.display(caseObj.getCaseInputs(), this);
    }

    public void doViewNote(CaseInput note, InputView view) {
        view.display(note);
    }
}
