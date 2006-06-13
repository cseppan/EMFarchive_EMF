package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.EmfException;

public interface Strategy {
    void run() throws EmfException;
    
    StrategyResult getResult() throws EmfException;
}
