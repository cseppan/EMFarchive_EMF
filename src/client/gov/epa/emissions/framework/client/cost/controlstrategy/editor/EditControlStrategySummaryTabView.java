package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface EditControlStrategySummaryTabView  extends EditControlStrategyTabView{
    
    void setRunMessage(ControlStrategy controlStrategy);

    void stopRun();

    void doRefresh();

}
