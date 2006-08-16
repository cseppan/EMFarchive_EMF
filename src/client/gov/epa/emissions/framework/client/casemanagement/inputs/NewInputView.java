package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;

public interface NewInputView {
    void display(Case caseObj);

    boolean shouldCreate();

    CaseInput input();
    
    void register(Object presenter);
}
