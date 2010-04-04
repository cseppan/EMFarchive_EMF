package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

public class StrategyTask extends LeastCostAbstractStrategyTask {

    private StrategyLoader loader;
    
    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy);
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
            ControlStrategyResult result = null;
            try {
                result = loader.loadStrategyResult(controlStrategyInputDataset);
                recordCount = loader.getRecordCount();
                result.setRecordCount(recordCount);
                status = "Completed.";
            } catch (Exception e) {
                e.printStackTrace();
                status = "Failed. Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". " + e.getMessage();
                setStatus(status);
            } finally {
                if (result != null) {
                    result.setCompletionTime(new Date());
                    result.setRunStatus(status);
                    saveControlStrategyResult(result);
                    strategyResultList.add(result);
                    addStatus(controlStrategyInputDataset);
                }
            }
            
            //now create the measure summary result based on the results from the strategy run...
            generateStrategyMeasureSummaryResult();

        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
                updateVersionInfo();
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
        super.finalizeCMWorksheetResult();

        //now create the county summary result based on the results from the strategy run...
        generateStrategyCountySummaryResult(strategyResultList.toArray(new ControlStrategyResult[0]));
        
    }

    public void beforeRun() throws EmfException {
        //populate the Sources Table
        populateSourcesTable();

//        ControlStrategyResult[] results = getControlStrategyResults();
        //create the worksheet (strat result), if needed, maybe they don't want to recreate these...
//        if (controlStrategy.getDeleteResults() || results.length == 0) {
            leastCostCMWorksheetResult = loader.loadLeastCostCMWorksheetResult();
//        } else {
//            for (ControlStrategyResult result : results ) {
//                if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheet)) {
//                    leastCostCMWorksheetResult = result;
//                    break;
//                }
//            }
//        }
        //create just in case these don't exist, maybe the strategy type was changed...
        if (leastCostCMWorksheetResult == null) leastCostCMWorksheetResult = loader.loadLeastCostCMWorksheetResult();
        mergeInventoryDatasets();
    }
}