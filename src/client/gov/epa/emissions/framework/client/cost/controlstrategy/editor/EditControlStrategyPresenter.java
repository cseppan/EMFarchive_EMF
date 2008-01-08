package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface EditControlStrategyPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void set(EditControlStrategyTabView view);

    void set(EditControlStrategySummaryTabView view);
    
    void set(EditControlStrategyOutputTabView view);

    void set(ControlStrategyMeasuresTabView view) throws EmfException;

    void set(ControlStrategyPollutantsTabView view) throws EmfException;

    void set(ControlStrategyConstraintsTabView view);

    void runStrategy(String exportDirectory) throws EmfException;

    void runStrategy(String exportDirectory, boolean useSQLApproach) throws EmfException;

    void setResults(ControlStrategy controlStrategy);

    void stopRun() throws EmfException;

    void doRefresh() throws EmfException;

    void doLoad(String tabTitle) throws EmfException;

    CostYearTable getCostYearTable() throws EmfException;
    
    void fireTracking();
    
    DatasetType getDatasetType(String name) throws EmfException;
    
    Version[] getVersions(EmfDataset dataset) throws EmfException; 

    EmfDataset[] getDatasets(DatasetType type) throws EmfException;
}
