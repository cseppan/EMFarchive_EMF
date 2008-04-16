package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface EditControlStrategyTabView {

    //  update with the view contents
    void save(ControlStrategy controlStrategy) throws EmfException;
    
    void refresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults);

    void notifyStrategyTypeChange(StrategyType strategyType);
}
