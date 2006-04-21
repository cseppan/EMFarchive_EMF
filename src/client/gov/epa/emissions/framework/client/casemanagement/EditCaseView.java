package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface EditCaseView extends ManagedView {

    void observe(EditCasePresenter presenter);

    void display(Case caseObj);

}