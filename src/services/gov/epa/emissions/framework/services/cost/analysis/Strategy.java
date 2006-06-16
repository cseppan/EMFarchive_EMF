package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.data.StrategyResult;

public interface Strategy {
    void run() throws EmfException;
    
    StrategyResult getResult();
    
    ControlStrategy getControlStrategy();
}
