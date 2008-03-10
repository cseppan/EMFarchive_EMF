package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends AbstractStrategyTask {

    private StrategyLoader loader;
    
    private ControlStrategyResult leastCostCMWorksheetResult;

    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory, Boolean useSQLApproach) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public void run() throws EmfException {
        super.run(loader);
    }

    public void postRun() throws EmfException {
        //finalize the result, update completion time and run status...
        leastCostCMWorksheetResult.setCompletionTime(new Date());
        leastCostCMWorksheetResult.setRunStatus("Completed.");
        setSummaryResultCount(leastCostCMWorksheetResult);
        saveControlStrategySummaryResult(leastCostCMWorksheetResult);
        runSummaryQASteps(leastCostCMWorksheetResult.getInputDataset(), 0);
    }

    public void preRun() throws EmfException {
        //create the worksheet (strat result)
        leastCostCMWorksheetResult = loader.loadLeastCostCMWorksheetResult();
    }
}