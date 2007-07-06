package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.GenerateSccControlMeasuresMap;
import gov.epa.emissions.framework.services.cost.controlStrategy.SccControlMeasuresMap;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

public class MaxEmsRedStrategy extends AbstractStrategyTask {

    public MaxEmsRedStrategy(ControlStrategy strategy, User user, DbServerFactory dbServerFactory, Integer batchSize,
            HibernateSessionFactory sessionFactory) throws EmfException {
        super(strategy, user, dbServerFactory, batchSize, sessionFactory);
    }

    public void run() throws EmfException {
        String status = "";
        try {
            GenerateSccControlMeasuresMap mapGenerator = new GenerateSccControlMeasuresMap(dbServer,
                    emissionTableName(inputDataset), controlStrategy, sessionFactory);
            SccControlMeasuresMap map = mapGenerator.create();
            StrategyLoader loader = new StrategyLoader(creator.outputTableName(), tableFormat, sessionFactory, dbServer, result, map,
                    controlStrategy);
            loader.load(optimizedQuery);
            status = "Completed. Input dataset: " + inputDataset.getName() + ".";
            result.setRunStatus(status);
        } catch (Exception e) {
            status = "Failed. Error processing input dataset: " + inputDataset.getName() + ". " + result.getRunStatus();
            throw new EmfException(e.getMessage());
        } finally {
            closeOptimizedQuery();
            result.setCompletionTime(new Date());
            result.setRunStatus(status);
            saveResults();
            disconnectDbServer();
        }
    }
}
