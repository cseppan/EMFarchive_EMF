package gov.epa.emissions.framework.client.cost.controlstrategy.viewer;

import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;

public interface ViewControlStrategyConstraintsTabPresenter extends ViewControlStrategyTabPresenter {

    void doDisplay();

    ControlStrategyConstraint getConstraint();
}