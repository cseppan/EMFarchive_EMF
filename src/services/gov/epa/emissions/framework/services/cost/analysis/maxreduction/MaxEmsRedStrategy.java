package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class MaxEmsRedStrategy extends AbstractStrategyTask {
    
    private StrategyLoader loader;
    
    public MaxEmsRedStrategy(ControlStrategy strategy, User user, 
            DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(strategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public void run() throws EmfException {
        super.run(loader);
//        String status = "";
//        try {
//
//            //process/load each input dataset
//            EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();
//            for (int i = 0; i < inputDatasets.length; i++) {
//                ControlStrategyResult result = new ControlStrategyResult();
//                try {
//                    result = loader.loadStrategyResult(inputDatasets[i]);
//                    recordCount += loader.getRecordCount();
//                    status = "Completed.";
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    status = "Failed. Error processing input dataset: " + inputDatasets[i].getName() + ". " + result.getRunStatus();
//                } finally {
//                    result.setCompletionTime(new Date());
//                    result.setRunStatus(status);
//                    saveControlStrategyResult(result);
//                }
//            }
//            
//        } catch (Exception e) {
//            status = "Failed. Error processing input dataset";
//            e.printStackTrace();
//            throw new EmfException(e.getMessage());
//        } finally {
//            loader.disconnectDbServer();
//            disconnectDbServer();
//        }
    }
}
