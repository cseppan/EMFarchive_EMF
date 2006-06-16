package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlStrategyServiceImpl extends EmfServiceImpl implements ControlStrategyService {

    private static Log LOG = LogFactory.getLog(ControlStrategyServiceImpl.class);

    private PooledExecutor threadPool;
    
    private HibernateSessionFactory sessionFactory;
    
    private RunControlStrategy runStrategy;

    private ControlStrategyDAO dao;
    
    private DataSource datasource;

    public ControlStrategyServiceImpl() throws Exception {
        super("Control Strategy Service");
        init(dbServer, HibernateSessionFactory.get());
    }

    public ControlStrategyServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory) throws Exception {
        super(datasource, dbServer);
        this.datasource = datasource;
        init(dbServer, sessionFactory);
    }
    
    private void init(DbServer dbServer, HibernateSessionFactory sessionFactory) throws EmfException {
        this.sessionFactory = sessionFactory;
        dao = new ControlStrategyDAO();
        threadPool = createThreadPool();
        
        StrategyFactory factory;
        try {
            factory = new StrategyFactory(dbServer, new CostServiceImpl(datasource, dbServer, sessionFactory), 
                    batchSize());
        } catch (Exception e) {
            LOG.error("could not access control measure service.");
            throw new EmfException("could not access control measure service.");
        }
        runStrategy = new RunControlStrategy(factory, sessionFactory, threadPool);
    }
    
    protected void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }
    
    private PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public ControlStrategy[] getControlStrategies() throws EmfException {
        try {
            List cs = dao.all(sessionFactory.getSession());
            return (ControlStrategy[]) cs.toArray(new ControlStrategy[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve all control strategies.");
            throw new EmfException("could not retrieve all control strategies.");
        }
    }

    public void addControlStrategy(ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.add(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy: " + element, e);
            throw new EmfException("Could not add Control Strategy: " + element);
        }
    }

    public ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlStrategy locked = dao.obtainLocked(owner, element, session);
            session.close();

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Strategy: " + element + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Strategy: " + element + " by owner: "
                    + owner.getUsername());
        }
    }

    public ControlStrategy releaseLocked(ControlStrategy locked) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            ControlStrategy released = dao.releaseLocked(locked, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Control Strategy : " + locked + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Control Strategy: " + locked + " by owner: "
                    + locked.getLockOwner());
        }
    }

    public ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategy released = dao.update(element, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        }
    }

    public ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            
            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");
            
            ControlStrategy released = dao.updateWithLock(element, session);
            session.close();
            
            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        }
    }

    public void removeControlStrategies(ControlStrategy[] elements) throws EmfException {
        try {
            for (int i = 0; i < elements.length; i++)
                remove(elements[i]);

        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + elements, e);
            throw new EmfException("Could not update ControlStrategy: " + elements);
        }
    }

    private void remove(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        if (!dao.canUpdate(element, session))
            throw new EmfException("Control Strategy name already in use");

        dao.remove(element, session);
        session.close();
    }

    public void runStrategy(User user, ControlStrategy strategy) throws EmfException {
        runStrategy.run(user, strategy, this);
    }

    public StrategyType[] getStrategyTypes() throws EmfException {
        try {
            List st = dao.getAllStrategyTypes(sessionFactory.getSession());
            return (StrategyType[]) st.toArray(new StrategyType[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve all control strategy types. " + e.getMessage());
            throw new EmfException("could not retrieve all control strategy types. " + e.getMessage());
        }
    }
    
    private int batchSize() {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("export-batch-size", session);
            return Integer.parseInt(property.getValue());
        } finally {
            session.close();
        }
    }

}
