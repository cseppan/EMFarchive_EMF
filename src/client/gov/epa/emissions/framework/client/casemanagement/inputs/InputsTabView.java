package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface InputsTabView {

    void display(CaseInput[] inputs, InputsTabPresenter presenter);

}
