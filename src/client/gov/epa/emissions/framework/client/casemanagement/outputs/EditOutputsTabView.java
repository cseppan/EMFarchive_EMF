package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.EmfException;


public interface EditOutputsTabView {
    
    void display();

    void refresh() throws EmfException;

    void observe(EditOutputsTabPresenterImpl editOutputsTabPresenterImpl);
    
}
