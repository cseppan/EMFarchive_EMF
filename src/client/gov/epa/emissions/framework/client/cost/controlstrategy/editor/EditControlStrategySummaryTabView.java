package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface EditControlStrategySummaryTabView {
    
    // update with the view contents
    void save(ControlStrategy controlStrategy) throws EmfException;
    
    void setResults();

    void stopRun();

}
