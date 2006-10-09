package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public interface EditControlStrategyTabPresenter {

    void doSave() throws EmfException;

    void doRefresh(ControlStrategyResult result);
}
