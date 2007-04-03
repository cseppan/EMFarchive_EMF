package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public interface EditControlStrategyPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(EditControlStrategyTabView view);

    void set(EditControlStrategySummaryTabView view);
    
    void set(EditControlStrategyOutputTabView view);

    void set(ControlStrategyMeasuresTabView view) throws EmfException;

    void set(ControlStrategyPollutantsTabView view) throws EmfException;

    void runStrategy() throws EmfException;

    void setResults(ControlStrategy controlStrategy);

    void stopRun() throws EmfException;

    void doRefresh() throws EmfException;

    void doLoad(String tabTitle) throws EmfException;
}
