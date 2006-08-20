package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.casemanagement.Case;

public interface EditOutputsTabView {

    void display(Case caseObj, EditOutputsTabPresenter presenter);

    void refresh();

}
