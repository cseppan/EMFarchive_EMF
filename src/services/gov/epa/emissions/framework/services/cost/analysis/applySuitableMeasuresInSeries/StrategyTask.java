package gov.epa.emissions.framework.services.cost.analysis.applySuitableMeasuresInSeries;

import java.sql.SQLException;
import java.util.Date;

import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class StrategyTask extends AbstractStrategyTask {

    public StrategyTask(ControlStrategy strategy, User user, DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(strategy, user, dbServerFactory, batchSize, sessionFactory);
    }

    public void run() throws EmfException {
//
        OptimizedQuery optimizedQuery = sourceQuery(inputDataset);
        String status = "";
        try {
            StrategyLoader loader = new StrategyLoader(creator.outputTableName(), tableFormat, sessionFactory, 
                    dbServer, result, controlStrategy);
            loader.load(optimizedQuery);
            status = "Completed. Input dataset: " + inputDataset.getName() + ".";
            result.setRunStatus(status);
        } catch (Exception e) {
            status = "Failed. Error processing input dataset: " + inputDataset.getName() + ". " + result.getRunStatus();
            throw new EmfException(e.getMessage());
        } finally {
            close(optimizedQuery);
            result.setCompletionTime(new Date());
            result.setRunStatus(status);
            saveResults();
            try {
                dbServer.disconnect();
            } catch (SQLException e) {
                throw new EmfException("Could not disconnect DbServer -" + e.getMessage());
            }
        }
    }

    public void close() throws EmfException {
        try {
            dbServer.disconnect();
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        }

    }
    
    private void close(OptimizedQuery optimizedQuery) throws EmfException {
        try {
            optimizedQuery.close();
        } catch (SQLException e) {
            throw new EmfException("Could not close optimized query -" + e.getMessage());
        }
    }
}
