package gov.epa.emissions.framework.services.cost.analysis;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface Strategy {
    
    void run(User user) throws EmfException;
    
    ControlStrategy getControlStrategy();

    void close() throws EmfException;
}
