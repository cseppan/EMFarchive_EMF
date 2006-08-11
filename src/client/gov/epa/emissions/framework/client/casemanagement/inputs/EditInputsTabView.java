package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface EditInputsTabView {

    void display(Case caseObj, EditInputsTabPresenter presenter);

    CaseInput[] additions();

    void addInput(CaseInput input);

}
