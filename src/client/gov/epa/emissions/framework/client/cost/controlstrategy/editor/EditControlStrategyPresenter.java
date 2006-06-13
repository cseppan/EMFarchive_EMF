package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface EditControlStrategyPresenter {
    
    void doDisplay() throws EmfException;

    void doClose() throws EmfException;
    
    void doSave() throws EmfException;

    void set(EditControlStrategySummaryTabView view);
    
    void runStrategy() throws EmfException;

    void setResults(ControlStrategy controlStrategy);

    void stopRun();

}
