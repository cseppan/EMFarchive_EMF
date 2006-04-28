package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.services.EmfException;

public interface ControlStrategiesManagerPresenter {

    void display() throws EmfException;

    void doRefresh() throws EmfException;

    void doClose();

    void doNew(ControlStrategyView view);

}