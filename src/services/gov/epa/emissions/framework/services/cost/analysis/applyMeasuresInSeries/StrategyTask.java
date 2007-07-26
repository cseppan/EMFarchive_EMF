package gov.epa.emissions.framework.services.cost.analysis.applyMeasuresInSeries;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends AbstractStrategyTask {

    public StrategyTask(ControlStrategy strategy, User user, DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(strategy, user, 
                dbServerFactory, sessionFactory);
    }

    public void run() {
//
//        OptimizedQuery optimizedQuery = null;//sourceQuery(inputDataset);
//        String status = "";
//        try {
//            StrategyLoader loader = new StrategyLoader(/*creator.outputTableName()*/"", tableFormat, sessionFactory, 
//                    dbServer, result, controlStrategy);
//            loader.load(optimizedQuery);
//            recordCount = loader.getRecordCount(); 
//            status = "Completed. Input dataset: " + inputDataset.getName() + ".";
//            result.setRunStatus(status);
//        } catch (Exception e) {
//            status = "Failed. Error processing input dataset: " + inputDataset.getName() + ". " + result.getRunStatus();
//            e.printStackTrace();
//            throw new EmfException(e.getMessage());
//        } finally {
//            result.setCompletionTime(new Date());
//            result.setRunStatus(status);
//            saveResults();
//            disconnectDbServer();
//        }
    }
}