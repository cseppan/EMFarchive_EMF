package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface ControlStrategyConstraintsTabView extends EditControlStrategyTabView {
 //type holder now
    void observe(EditControlStrategyConstraintsTabPresenter presenter);

    void display(ControlStrategy strategy);
}
