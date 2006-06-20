package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface Strategy {
    void run() throws EmfException;
    
    ControlStrategy getControlStrategy();
}
