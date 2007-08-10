package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface ControlStrategyService extends EMFService {

    ControlStrategy[] getControlStrategies() throws EmfException;
    
    ControlStrategyResult[] getControlStrategyResults(int controlStrategyId) throws EmfException;
    
    StrategyType[] getStrategyTypes() throws EmfException;
    
    int addControlStrategy(ControlStrategy element) throws EmfException;
    
//    void removeControlStrategies(ControlStrategy[] elements, User user) throws EmfException;

    void removeControlStrategies(int[] ids, User user) throws EmfException;

    ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException;

//    void releaseLocked(ControlStrategy locked) throws EmfException;

    void releaseLocked(int id) throws EmfException;

    ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException;
    
    ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException;
    
    void runStrategy (User user, ControlStrategy strategy) throws EmfException;
    
    void stopRunStrategy() throws EmfException;

    void createInventory(User user, ControlStrategy controlStrategy, ControlStrategyInputDataset controlStrategyInputDataset) throws EmfException;
    
    ControlStrategyResult getControlStrategyResult(int controlStrategyId, int inputDatasetId) throws EmfException;
    
    String controlStrategyRunStatus(int id) throws EmfException;

    int isDuplicateName(String name) throws EmfException;

    int copyControlStrategy(int id, User creator) throws EmfException;

    ControlStrategy getById(int id) throws EmfException;

    //StrategyType[] getEquaitonTypes();
}
