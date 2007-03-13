package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface EditOutputsTabView {

    void display(Case caseObj, EditOutputsTabPresenter presenter, EmfSession session);

    void refresh();
    
    void saveCaseOutputFileDir();

}
