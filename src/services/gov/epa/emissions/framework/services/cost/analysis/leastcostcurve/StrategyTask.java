package gov.epa.emissions.framework.services.cost.analysis.leastcostcurve;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.leastcost.LeastCostAbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends LeastCostAbstractStrategyTask {

    private StrategyLoader loader;
    
    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, batchSize,
                sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory, Boolean useSQLApproach) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, batchSize,
                sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public void run() throws EmfException {
//        super.run(loader);
        
        //get rid of strategy results
        deleteStrategyResults();

        //run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        String status = "";
        try {
            //process/load each input dataset
            ControlStrategyInputDataset controlStrategyInputDataset = getInventory();
            try {
                loader.loadStrategyResult(controlStrategyInputDataset);
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". " + e.getMessage();
                setStatus(status);
            } finally {
                addStatus(controlStrategyInputDataset);
            }
            
        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
            } catch (Exception e) {
                status = "Failed. Error processing input dataset";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                loader.disconnectDbServer();
                disconnectDbServer();
            }
        }
    }

    public void afterRun() throws EmfException {
        //finalize the result, update completion time and run status...
        super.finalizeCMWorksheetResult();

        super.finalizeCostCuveSummaryResult();
    }

    public void beforeRun() throws EmfException {
        //populate the Sources Table
        populateSourcesTable();

        //create the worksheet (strat result), if needed, maybe they don't want to recreate these...
        ControlStrategyResult[] results = loader.getControlStrategyResults();
        if (controlStrategy.getDeleteResults() || results.length == 0) {
            leastCostCMWorksheetResult = loader.loadLeastCostCMWorksheetResult();
            leastCostCurveSummaryResult = loader.loadLeastCostCurveSummaryResult();
        } else {
            for (ControlStrategyResult result : results) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheetResult)) {
                    leastCostCMWorksheetResult = result;
                } else if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostCurveSummaryResult)) {
                    leastCostCurveSummaryResult = result;
                }
            }
        }
        //create just in case these don't exist, maybe the strategy type was changed...
        if (leastCostCMWorksheetResult == null) leastCostCMWorksheetResult = loader.loadLeastCostCMWorksheetResult();
        if (leastCostCurveSummaryResult == null) leastCostCurveSummaryResult = loader.loadLeastCostCurveSummaryResult();
        
        //if there is more than one input inventory, then merge these into one dataset, 
        //then we use that as the input to the strategy run
        mergeInventoryDatasets();
    }
}