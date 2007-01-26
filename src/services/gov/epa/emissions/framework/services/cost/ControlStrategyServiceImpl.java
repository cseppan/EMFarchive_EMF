package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlStrategyServiceImpl implements ControlStrategyService {

    private static Log LOG = LogFactory.getLog(ControlStrategyServiceImpl.class);

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    private ControlStrategyDAO dao;

    public ControlStrategyServiceImpl() throws Exception {
        init(HibernateSessionFactory.get());
    }

    public ControlStrategyServiceImpl(HibernateSessionFactory sessionFactory) throws Exception {
        init(sessionFactory);
    }

    private void init(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new ControlStrategyDAO();
        threadPool = createThreadPool();

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
        Session session = sessionFactory.getSession();
        try {
            List cs = dao.all(session);
            return (ControlStrategy[]) cs.toArray(new ControlStrategy[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all control strategies.");
            throw new EmfException("Could not retrieve all control strategies.");
        } finally {
            session.close();
        }
    }

    public void addControlStrategy(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.add(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy: " + element, e);
            throw new EmfException("Could not add Control Strategy: " + element);
        } finally {
            session.close();
        }
    }

    public ControlStrategy obtainLocked(User owner, ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategy locked = dao.obtainLocked(owner, element, session);

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Strategy: " + element + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Strategy: " + element + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public void releaseLocked(ControlStrategy locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(locked, session);
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Control Strategy : " + locked + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Control Strategy: " + locked + " by owner: "
                    + locked.getLockOwner());
        } finally {
            session.close();
        }
    }

    public ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("The Control Strategy name is already in use");

            ControlStrategy released = dao.update(element, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        } finally {
            session.close();
        }
    }

    public ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategy csWithLock = dao.updateWithLock(element, session);

            return csWithLock;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        } finally {
            session.close();
        }
    }

    public void removeControlStrategies(ControlStrategy[] elements, User user) throws EmfException {
        try {
            for (int i = 0; i < elements.length; i++) {
                if (!user.equals(elements[i].getCreator()))
                    throw new EmfException("Only the creator of " + elements[i].getName()
                            + " can remove it from the database.");
                remove(elements[i]);
            }

        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + elements, e);
            throw new EmfException("Could not update ControlStrategy: " + elements);
        }
    }

    private void remove(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategyResult result = controlStrategyResults(element);
            if (result != null)
                dao.remove(result, session);

            dao.remove(element, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not remove control strategy: " + element, e);
            throw new EmfException("Could not remove control strategy: " + element.getName());
        }
    }

    public void runStrategy(User user, ControlStrategy strategy) throws EmfException {
        StrategyFactory factory = new StrategyFactory(batchSize());
        DbServer dbServer = null;
        try {
            dbServer = dbServer();
        } catch (Exception e) {
            LOG.error("Could not get the db connection.", e);
            throw new EmfException("Could not get the db connection." + e.getMessage());
        }
        RunControlStrategy runStrategy = new RunControlStrategy(factory, sessionFactory, dbServer, threadPool);
        runStrategy.run(user, strategy, this);
    }

    public void stopRunStrategy() {
        // TODO:
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

    public void createInventory(User user, ControlStrategy controlStrategy) throws EmfException {
        DbServer dbServer = dbServer();//the connection is closed at the end of the thread
        try {
            ControlStrategyInventoryOutputTask task= new ControlStrategyInventoryOutputTask(user, controlStrategy,sessionFactory, dbServer);
            if(task.shouldProceed())
                threadPool.execute(new GCEnforcerTask("Create Inventory: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            LOG.error("Error running control strategy: " + controlStrategy.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    private DbServer dbServer() throws EmfException {
        try {
            return new EmfDbServer();
        } catch (Exception e) {
            LOG.error("Could not get database connection: " + e.getMessage());
            throw new EmfException("Could not get database connection: " +e.getMessage());
        }
    }

    public ControlStrategyResult controlStrategyResults(ControlStrategy controlStrategy) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyResult controlStrategyResult = dao.controlStrategyResult(controlStrategy, session);
            return controlStrategyResult;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve ControlStrategy Result", e);
            throw new EmfException("Could not retrieve ControlStrategy Result");
        } finally {
            session.close();
        }
    }

    public String controlStrategyRunStatus(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.controlStrategyRunStatus(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve ControlStrategy Status", e);
            throw new EmfException("Could not retrieve ControlStrategy Status");
        } finally {
            session.close();
        }
    }

    public ControlMeasureClass[] getControlMeasureClasses(int controlStrategyId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getControlMeasureClasses(controlStrategyId, session);
            return (ControlMeasureClass[]) all.toArray(new ControlMeasureClass[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control measure classes.", e);
            throw new EmfException("Could not retrieve control measure classes.");
        } finally {
            session.close();
        }
    }
}
