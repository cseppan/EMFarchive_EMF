package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.StrategyResult;

public interface Strategy {
    void run() throws EmfException;
    
    StrategyResult getResult();
    
    ControlStrategy getControlStrategy();
}
