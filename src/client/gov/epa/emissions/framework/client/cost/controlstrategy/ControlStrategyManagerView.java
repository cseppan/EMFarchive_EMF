package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ControlStrategyManagerView extends ManagedView {

    void display(ControlStrategy[] controlStrategies);

    void observe(ControlStrategiesManagerPresenterImpl presenter);

    void refresh(ControlStrategy[] controlStrategies);
}
