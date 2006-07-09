package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface EditControlStrategySummaryTabPresenter extends EditControlStrategyTabPresenter {
    
    void setResults(ControlStrategy controlStrategy);
    
    void doRefresh();
}