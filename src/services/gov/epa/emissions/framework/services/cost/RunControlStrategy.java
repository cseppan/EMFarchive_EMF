package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.basic.LoggingServiceImpl;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.EmfDataset;
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

    private DbServer dbServer;

    public RunControlStrategy(StrategyFactory factory, HibernateSessionFactory sessionFactory, DbServer dbServer, PooledExecutor threadPool) {
        this.factory = factory;
        this.sessionFactory = sessionFactory;
        this.threadPool = threadPool;
        this.dbServer = dbServer;
        this.services = services();
    }

    public void run(User user, ControlStrategy controlStrategy, ControlStrategyService service) throws EmfException {
        currentLimitations(controlStrategy);
        try {
            Strategy strategy = factory.create(controlStrategy, user, sessionFactory, dbServer);
            StrategyTask task = new StrategyTask(strategy, user, services, service);
            threadPool.execute(new GCEnforcerTask("Run Strategy: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            log.error("Error running control strategy: " + controlStrategy.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private void currentLimitations(ControlStrategy controlStrategy) throws EmfException {
        if (controlStrategy.getStrategyType().getName().equalsIgnoreCase("Least Cost"))
            throw new EmfException("Least Cost Analysis is not supported.");

        EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();
        if (inputDatasets.length == 0)
            return;
        
        DatasetType datasetType = controlStrategy.getDatasetType();
        int indexOfNonpoint = datasetType.getName().indexOf("Nonpoint");
        int indexOfOnroad = datasetType.getName().indexOf("Onroad");
        if (indexOfNonpoint == -1 && indexOfOnroad == -1)
            throw new EmfException("The dataset type '" + datasetType.getName() + "' is not supported yet.");

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
