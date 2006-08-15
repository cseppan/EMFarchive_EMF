package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class StrategyFactory {
    private static Log log = LogFactory.getLog(StrategyFactory.class);

    private int batchSize;

    public StrategyFactory(int batchSize) {
        this.batchSize = batchSize;
    }

    public Strategy create(ControlStrategy controlStrategy, HibernateSessionFactory sessionFactory) throws EmfException {
        try {
            return doCreate(controlStrategy,sessionFactory);
        } catch (Exception e) {
            log.error("Failed to create strategy. Cause: " + e.getMessage());
            throw new EmfException("Failed to create strategy." + e.getMessage());
        }
    }

    private Strategy doCreate(ControlStrategy controlStrategy, HibernateSessionFactory sessionFactory) throws Exception {
        String strategyClassName = controlStrategy.getStrategyType().getStrategyClassName();
        Class strategyClass = Class.forName(strategyClassName);
        DbServer dbServer = dbServer();
        Class[] classParams = new Class[] { ControlStrategy.class, DbServer.class, Integer.class, HibernateSessionFactory.class };
        Object[] params = new Object[] { controlStrategy, dbServer, new Integer(batchSize),sessionFactory };
        Constructor strategyConstructor = strategyClass.getDeclaredConstructor(classParams);

        return (Strategy) strategyConstructor.newInstance(params);
    }

    private DbServer dbServer() throws Exception {
        return new EmfDbServer();
    }

}
