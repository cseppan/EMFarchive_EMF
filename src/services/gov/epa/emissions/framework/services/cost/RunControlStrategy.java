package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class RunControlStrategy {

    private static Log log = LogFactory.getLog(RunControlStrategy.class);

    private StrategyFactory factory;

    private HibernateSessionFactory sessionFactory;

    private PooledExecutor threadPool;

    private Services services;

    public RunControlStrategy(StrategyFactory factory, HibernateSessionFactory sessionFactory, PooledExecutor threadPool) {
        this.factory = factory;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
        this.services = services();
    }

    public void run(User user, ControlStrategy controlStrategy, ControlStrategyService service) throws EmfException {
        try {
            Strategy strategy = factory.create(controlStrategy);
            StrategyTask task = new StrategyTask(strategy, user, services, service, sessionFactory);
            threadPool.execute(new GCEnforcerTask("Run Strategy: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            log.error("Error running control strategy: " + controlStrategy.getName(), e);

            if (controlStrategy.getStrategyType().getName().equalsIgnoreCase("Least Cost"))
                throw new EmfException("Least Cost Analysis is not supported.");
            throw new EmfException(e.getMessage());
        }
    }

    public void stop() {
        threadPool.shutdownNow();
    }

    private Services services() {
        Services services = new Services();
        services.setLoggingService(new LoggingServiceImpl(sessionFactory));
        services.setStatusService(new StatusDAO(sessionFactory));
        services.setDataService(new DataServiceImpl(sessionFactory));

        return services;
    }
}
