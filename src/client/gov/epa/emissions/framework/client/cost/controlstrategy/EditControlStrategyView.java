package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface EditControlStrategyView extends ManagedView {

    void observe(EditControlStrategyPresenter presenter);

    void display(ControlStrategy controlStrategy);
    
    void notifyLockFailure(ControlStrategy controlStrategy);

}
