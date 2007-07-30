package gov.epa.emissions.framework.services.cost.analysis.common;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

import java.sql.ResultSet;

public interface StrategyLoader {

    //implement code that is specific to the strategy type
    abstract void doBatchInsert(ResultSet resultSet) throws Exception;

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset inputDataset) throws Exception;

    public void disconnectDbServer() throws EmfException;

    public long getRecordCount();

}
