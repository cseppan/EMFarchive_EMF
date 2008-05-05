package gov.epa.emissions.framework.services.cost.analysis.annotateinventory;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

public class StrategyTask extends AbstractStrategyTask {

    private StrategyLoader loader;
    
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
            ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
                ControlStrategyResult result = null;
                try {
                    result = loader.loadStrategyResult(controlStrategyInputDatasets[i]);
                    recordCount = loader.getRecordCount();
                    result.setRecordCount(recordCount);
                    status = "Completed.";
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "Failed. Error processing input dataset: " + controlStrategyInputDatasets[i].getInputDataset().getName() + ". " + e.getMessage();
                    setStatus(status);
                } finally {
                    if (result != null) {
                        result.setCompletionTime(new Date());
                        result.setRunStatus(status);
                        saveControlStrategyResult(result);
                        strategyResultList.add(result);
                        addStatus(controlStrategyInputDatasets[i]);
                    }
                    //make sure somebody hasn't cancelled this run.
                    if (isRunStatusCancelled()) {
//                        status = "Cancelled. Strategy run was cancelled: " + controlStrategy.getName();
//                        setStatus(status);
                        throw new EmfException("Strategy run was cancelled.");
                    }
                }
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

    public void afterRun() {
        //
    }

    public void beforeRun() {
        //
    }
}