package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.services.EmfException;

public interface EditControlStrategyPresenter {
    
    void doDisplay() throws EmfException;

    void doClose() throws EmfException;
    
    void doSave() throws EmfException;

}
