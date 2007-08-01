package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface EditControlStrategyView extends ManagedView {

    void observe(EditControlStrategyPresenter presenter);

    void display(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults);
    
    void notifyLockFailure(ControlStrategy controlStrategy);

    public void startControlMeasuresRefresh();

    public void endControlMeasuresRefresh();
    
    void signalChanges();

}
