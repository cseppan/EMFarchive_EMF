package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;

public interface ControlStrategyService extends EMFService {

    ControlStrategy[] getControlStrategies() throws EmfException;
    
    StrategyType[] getStrategyTypes() throws EmfException;
    
    void addControlStrategy(ControlStrategy element) throws EmfException;
    
    void removeControlStrategies(ControlStrategy[] elements) throws EmfException;

    ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException;

    ControlStrategy releaseLocked(ControlStrategy locked) throws EmfException;

    ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException;
    
    ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException;
    
    void runStrategy (User user, ControlStrategy strategy) throws EmfException;
}
